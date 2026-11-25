package com.mycompany.smartorder.server;

import com.mycompany.smartorder.client.Request;
import com.mycompany.smartorder.client.Response;
import com.mycompany.smartorder.dao.MesaDAO;
import com.mycompany.smartorder.dao.PedidoDAO;
import com.mycompany.smartorder.excepciones.DAOException;
import com.mycompany.smartorder.model.ItemPedido;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Una instancia por conexión; los datos de sesión no se comparten. */
public class ManejadorCliente implements Runnable {
    private static final Logger LOG = Logger.getLogger(ManejadorCliente.class.getName());
    private final Socket socket;
    private final PedidoDAO pedidos;
    private final MesaDAO mesas;
    private String tipoPunto;
    private int numeroPunto;
    // Evita reprocesar el mismo requestId exitoso en esta sesión, hasta 256 IDs.
    private final Map<String, Response> confirmados = new LinkedHashMap<>();

    public ManejadorCliente(Socket socket, PedidoDAO pedidos, MesaDAO mesas) {
        this.socket = socket;
        this.pedidos = pedidos;
        this.mesas = mesas;
    }

    @Override
    public void run() {
        try (Socket cliente = socket) {
            cliente.setSoTimeout(300000);
            // ClientConnector abre primero su ObjectOutputStream.
            try (ObjectInputStream in = new ObjectInputStream(cliente.getInputStream());
                 ObjectOutputStream out = new ObjectOutputStream(cliente.getOutputStream())) {
                out.flush();
                // Restringir clases recibidas. No sustituye autenticación ni TLS.
                in.setObjectInputFilter(ObjectInputFilter.Config.createFilter(
                    "maxdepth=16;maxarray=10000;maxrefs=100000;maxbytes=8388608;"
                    + "com.mycompany.smartorder.client.Request;java.lang.String;"
                    + "java.lang.Integer;java.lang.Double;java.lang.Number;java.lang.Object;"
                    + "java.util.HashMap;java.util.LinkedHashMap;java.util.ArrayList;"
                    + "java.util.Map$Entry;!*"));
                while (true) {
                    Object objeto = in.readObject();
                    if (!(objeto instanceof Request)) break;
                    Request req = (Request) objeto;
                    Response respuesta = atender(req);
                    out.reset();
                    out.writeObject(respuesta);
                    out.flush();
                }
            }
        } catch (EOFException e) {
            // Desconexión normal: solamente termina este hilo.
        } catch (IOException | ClassNotFoundException e) {
            LOG.log(Level.WARNING, "Conexión cerrada: " + identidad());
        } finally {
            System.out.println(Thread.currentThread().getName() + " - Desconectado: " + identidad());
        }
    }

    private Response atender(Request req) {
        String id = req.getRequestId();
        try {
            if (id == null || id.isBlank() || req.getAction() == null || req.getPayload() == null)
                throw new IllegalArgumentException("Solicitud incompleta");
            Map<String, Object> p = req.getPayload();
            switch (req.getAction()) {
                case "conectar":
                    if (tipoPunto != null) throw new IllegalArgumentException("Sesión ya identificada");
                    String tipo = texto(p.get("tipo")).toUpperCase(Locale.ROOT);
                    int numero = enteroPositivo(p.get("numero"));
                    if (!tipo.equals("MESA") && !tipo.equals("BARRA"))
                        throw new IllegalArgumentException("Tipo de punto inválido");
                    tipoPunto = tipo;
                    numeroPunto = numero;
                    System.out.println(Thread.currentThread().getName() + " - Conectado: " + identidad());
                    return new Response(id, true, "Conectado como " + identidad(), null);
                case "obtenerMesas":
                    exigirConexion();
                    return new Response(id, true, "Mesas", new ArrayList<>(mesas.listarNumerosDisponibles()));
                case "crearPedido":
                    exigirConexion();
                    if (confirmados.containsKey(id)) return confirmados.get(id);
                    return crearPedido(id, p);
                case "listarPedidos":
                case "actualizarEstado":
                    return new Response(id, false, "Operación reservada: pendiente autenticación de personal", null);
                default:
                    return new Response(id, false, "Acción no permitida", null);
            }
        } catch (IllegalArgumentException e) {
            return new Response(id, false, e.getMessage(), null);
        } catch (DAOException e) {
            LOG.log(Level.WARNING, "DAO: " + identidad(), e);
            return new Response(id, false, "No se pudo completar la operación. Revise el pedido y la conexión a BD.", null);
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Error inesperado", e);
            return new Response(id, false, "Error interno del servidor", null);
        }
    }

    private Response crearPedido(String id, Map<String, Object> p) throws DAOException {
        String tipo = texto(p.get("tipoPedido"));
        if (!(tipo.equalsIgnoreCase("Local") || tipo.equalsIgnoreCase("Llevar")
                || tipo.equalsIgnoreCase("Para llevar")))
            throw new IllegalArgumentException("Tipo de pedido inválido");
        if (tipoPunto.equals("MESA") && p.get("mesa") != null
                && enteroPositivo(p.get("mesa")) != numeroPunto)
            throw new IllegalArgumentException("La mesa no coincide con la sesión");
        String obs = p.get("observacion") == null ? "" : textoOpcional(p.get("observacion"));
        // El controlador real usa lista; items es el nombre del servidor de prueba.
        Object datos = p.containsKey("lista") ? p.get("lista") : p.get("items");
        if (!(datos instanceof List<?>) || ((List<?>) datos).isEmpty())
            throw new IllegalArgumentException("Debe seleccionar productos");
        List<ItemPedido> items = new ArrayList<>();
        Set<String> nombres = new HashSet<>();
        for (Object o : (List<?>) datos) {
            if (!(o instanceof Map<?, ?>)) throw new IllegalArgumentException("Producto inválido");
            Map<?, ?> m = (Map<?, ?>) o;
            String nombre = texto(m.get("producto"));
            int cantidad = enteroPositivo(m.get("cantidad"));
            if (!nombres.add(nombre.toLowerCase(Locale.ROOT)))
                throw new IllegalArgumentException("Agrupe las cantidades del mismo producto");
            // El DAO obtiene el precio real de SQL Server, no del cliente.
            items.add(new ItemPedido(nombre, 0.0, cantidad));
        }
        System.out.println(Thread.currentThread().getName() + " - Procesando: " + identidad());
        int nuevoId = pedidos.crearPedido(numeroPunto, tipoPunto, tipo, obs, items, null);
        Response respuesta = new Response(id, true, "Pedido creado", nuevoId);
        if (confirmados.size() >= 256) confirmados.remove(confirmados.keySet().iterator().next());
        confirmados.put(id, respuesta);
        System.out.println(Thread.currentThread().getName() + " - Pedido #" + nuevoId + " confirmado: " + identidad());
        return respuesta;
    }

    private void exigirConexion() {
        if (tipoPunto == null) throw new IllegalArgumentException("Identifique el punto primero");
    }
    private String identidad() { return tipoPunto == null ? "sin identificar" : tipoPunto + " " + numeroPunto; }
    private static int enteroPositivo(Object o) {
        if (!(o instanceof Integer) || (Integer) o <= 0)
            throw new IllegalArgumentException("Se requiere un entero positivo");
        return (Integer) o;
    }
    private static String texto(Object o) {
        String s = textoOpcional(o);
        if (s.isBlank()) throw new IllegalArgumentException("Texto obligatorio");
        return s;
    }
    private static String textoOpcional(Object o) {
        if (!(o instanceof String)) throw new IllegalArgumentException("Texto inválido");
        return ((String) o).trim();
    }
}
