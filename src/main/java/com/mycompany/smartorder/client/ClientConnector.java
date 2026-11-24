package com.mycompany.smartorder.client;

import com.mycompany.smartorder.excepciones.ConexionException;
import java.io.IOException;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 *
 * @author joshar
 */
public class ClientConnector implements AutoCloseable {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    
    private final Map<String, CompletableFuture<Response>> pendientes = new ConcurrentHashMap<>();
    private volatile Consumer<Response> onEvento = r -> {};
    private volatile boolean activo = true;

    public ClientConnector(String host, int port) throws ConexionException {
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException  e) {
            throw new ConexionException("No se pudo conectar a " + host + ":" + port, e);
        }
        
        Thread lector = new Thread(this::escuchar, "lector-servidor");
        lector.setDaemon(true);
        lector.start();
    }
    
    public void setOnEvento(Consumer<Response> listener) {
        this.onEvento = listener;
    }
    
    public Response conectarComoPunto(String tipo, int numero) throws ConexionException {
        Map<String, Object> p = new HashMap<>();
        p.put("tipo", tipo);
        p.put("numero", numero);
        Response r = enviar("conectar", p, 5000);
        if(!r.isSuccess()) {
            throw new ConexionException(r.getMessage());
        }
        return r;
    }
    
    public Response enviar(String action, Map<String, Object> payload, long timeoutMs) throws ConexionException {
        String id = UUID.randomUUID().toString();
        CompletableFuture<Response> futuro = new CompletableFuture<>();
        pendientes.put(id, futuro);
        try {
            synchronized (out) {
                out.writeObject(new Request(id, action, payload));
                out.flush();
            }
            return futuro.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new ConexionException("El servidor no respondió a '" + action + "'", e);
        } catch (ExecutionException e) {
            throw new ConexionException("Error de conexión: " + e.getCause().getMessage(), e.getCause());
        } catch (IOException e) {
            throw new ConexionException("Error al enviar '" + action + "'", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConexionException("Operación interrumpida", e);
        } finally {
            pendientes.remove(id);
        }
    }
    
    public void escuchar() {
        try {
            while (activo) {
                Object obj = in.readObject();
                if(!(obj instanceof Response)) continue;
                
                Response r = (Response) obj;
                if(r.getRequestId() == null) {
                    onEvento.accept(r);
                } else {
                    CompletableFuture<Response> f = pendientes.remove(r.getRequestId());
                    if(f != null) f.complete(r);
               }
            }
        } catch (Exception e) {
            if (activo) System.err.println("Conexión perdida: " + e.getMessage());
        } finally {
            activo = false;
            pendientes.values().forEach(f -> f.completeExceptionally((new IOException("Conexión cerrada"))));
        }
    }
    
    @Override
    public void close() {
        activo = false;
        try {
            socket.close();
        } catch (IOException ignored) {}
    }
    
    
    
}
