package com.mycompany.smartorder.server;

import com.mycompany.smartorder.client.Request;
import com.mycompany.smartorder.client.Response;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerMock {

    private static final int PORT = 5555;
    private static final AtomicInteger siguienteId = new AtomicInteger(1);
    private static final Map<Integer, String> estados = new ConcurrentHashMap<>();
    private static final Map<Integer, Map<String, Object>> pedidos = new ConcurrentHashMap<>();
    private static final List<ManejadorCliente> clientes = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        ExecutorService pool = Executors.newCachedThreadPool();
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Servidor de prueba escuchando en puerto " + PORT);
            while (true) {
                Socket s = server.accept();
                System.out.println("Cliente conectado: " + s.getRemoteSocketAddress());
                pool.submit(new ManejadorCliente(s));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
        }
    }

    static void difundir(Response evento) {
        for (ManejadorCliente c : clientes) {
            try {
                c.enviar(evento);
            } catch (IOException e) {
                clientes.remove(c);
            }
        }
    }

    static Response evento(String tipo, String mensaje, Object data) {
        return new Response(null, true, tipo, data); 
    }

    static class ManejadorCliente implements Runnable {
        private final Socket socket;
        private ObjectOutputStream out;
        private String identidad = "desconocido";
        private String tipoPunto = "DESCONOCIDO";
        private int numeroPunto = 0;

        ManejadorCliente(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) { 
                out = new ObjectOutputStream(socket.getOutputStream());                    
                clientes.add(this);

                Object obj;
                while ((obj = in.readObject()) != null) {
                    if (obj instanceof Request) {
                        enviar(atender((Request) obj));
                    }
                }
            } catch (EOFException e) {
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                clientes.remove(this);
                System.out.println("Desconectado: " + identidad);
                try { socket.close(); } catch (IOException ignored) { }
            }
        }

        void enviar(Response r) throws IOException {
            synchronized (out) {
                out.writeObject(r);
                out.flush();
            }
        }

        private Response atender(Request req) {
            String id = req.getRequestId();
            Map<String, Object> p = req.getPayload();
            try {
                switch (req.getAction()) {
                    case "conectar": {
                        tipoPunto = String.valueOf(p.get("tipo"));
                        numeroPunto = (Integer) p.get("numero");
                        identidad = tipoPunto + " " + numeroPunto;
                        System.out.println("Punto identificado: " + identidad);
                        return new Response(id, true, "Conectado como " + identidad, null);
                    }
                    case "obtenerMesas": {
                        List<Integer> mesas = new ArrayList<>(List.of(1, 2, 3, 4, 5));
                        return new Response(id, true, "Mesas", mesas);
                    }
                    case "crearPedido": {
                        int nuevo = siguienteId.getAndIncrement();
                        Map<String, Object> pedido = new ConcurrentHashMap<>();
                        pedido.put("id", nuevo);
                        pedido.put("punto", identidad);
                        pedido.put("mesa", "MESA".equals(tipoPunto) ? numeroPunto : 0);
                        pedido.put("tipoPedido", p.get("tipoPedido"));
                        pedido.put("observacion", p.get("observacion"));
                        pedido.put("items", p.get("items"));
                        pedido.put("estado", "Recibido");
                        pedidos.put(nuevo, pedido);
                        difundir(evento("pedidoNuevo", null, pedido));
                        return new Response(id, true, "Pedido creado", nuevo);
                    }
                    case "actualizarEstado": {
                        int pedidoId = (Integer) p.get("pedidoId");
                        String nuevoEstado = (String) p.get("nuevoEstado");
                        Map<String, Object> pedido = pedidos.get(pedidoId);
                        if (pedido == null) {
                            return new Response(id, false, "Pedido no existe", null);
                        }
                        pedido.put("estado", nuevoEstado);
                        difundir(evento("estadoActualizado", null, pedido));
                        return new Response(id, true, "Estado actualizado", null);
                    }
                    case "listarPedidos":
                        return new Response(id, true, "Pedidos", new ArrayList<>(pedidos.values()));
                    default:
                        return new Response(id, false, "Acción no permitida: " + req.getAction(), null);
                }
            } catch (ClassCastException | NullPointerException e) {
                return new Response(id, false, "Payload inválido", null);
            }
        }
    }
}