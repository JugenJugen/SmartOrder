package com.mycompany.smartorder.server;

import com.mycompany.smartorder.controller.PedidoClienteController;
import com.mycompany.smartorder.dao.MesaDAO;
import com.mycompany.smartorder.dao.PedidoDAO;
import com.mycompany.smartorder.excepciones.ConexionException;
import com.mycompany.smartorder.excepciones.DAOException;
import com.mycompany.smartorder.model.ItemPedido;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Ejecutar con Run File. Sin argumentos: DAO simulado. Con real: servidor JDBC ya iniciado. */
public class PruebaServidorMultihilo {
    public static void main(String[] args) throws Exception {
        boolean real = args.length > 0 && args[0].equals("real");
        if (real) {
            probar(5555);
            return;
        }
        AtomicInteger ids = new AtomicInteger(100);
        CyclicBarrier dentroDAO = new CyclicBarrier(3);
        PedidoDAO simulado = new PedidoDAO() {
            @Override
            public int crearPedido(Integer n, String t, String tipo, String obs,
                    List<ItemPedido> items, Integer usuario) throws DAOException {
                try {
                    // La prueba falla si no entran tres hilos simultáneamente al DAO.
                    dentroDAO.await(3, TimeUnit.SECONDS);
                    return ids.incrementAndGet();
                } catch (Exception e) { throw new DAOException("No hubo concurrencia", e); }
            }
        };
        try (ServerSocket socket = new ServerSocket(0, 20, InetAddress.getByName("127.0.0.1"))) {
            Thread server = new Thread(() -> {
                try { Servidor.escuchar(socket, simulado, new MesaDAO()); }
                catch (java.io.IOException e) { if (!socket.isClosed()) throw new RuntimeException(e); }
            });
            server.setDaemon(true);
            server.start();
            probar(socket.getLocalPort());
            server.join(100);
        }
    }

    private static void probar(int puerto) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(3);
        CountDownLatch listos = new CountDownLatch(3);
        CountDownLatch salir = new CountDownLatch(1);
        List<Future<Integer>> resultados = new ArrayList<>();
        try {
            for (int i = 0; i < 3; i++) {
                final int n = i == 2 ? 1 : i + 1;
                final String tipo = i == 2 ? "BARRA" : "MESA";
                resultados.add(pool.submit(() -> {
                    PedidoClienteController c = new PedidoClienteController("127.0.0.1", puerto, tipo, n);
                    try {
                        listos.countDown();
                        if (!salir.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("Tiempo agotado");
                        int id = c.enviarPedido(n, "Local", "Prueba multihilo",
                                List.of(new ItemPedido("Limonada Natural", 1500, 1)));
                        try {
                            c.enviarPedido(n, "Local", "Prueba inválida",
                                    List.of(new ItemPedido("Limonada Natural", 1500, 0)));
                            throw new AssertionError("Se aceptó cantidad cero");
                        } catch (ConexionException esperado) {
                            System.out.println(tipo + " " + n + " - Cantidad cero rechazada: " + esperado.getMessage());
                        }
                        return id;
                    } finally { c.cerrar(); }
                }));
            }
            if (!listos.await(8, TimeUnit.SECONDS)) throw new IllegalStateException("No conectaron los tres clientes");
            salir.countDown();
            Set<Integer> ids = new HashSet<>();
            for (Future<Integer> f : resultados) {
                int id = f.get(10, TimeUnit.SECONDS);
                if (id <= 0 || !ids.add(id)) throw new AssertionError("ID inválido o repetido");
            }
            System.out.println("PRUEBA EXITOSA: tres pedidos, IDs distintos " + ids);
        } finally { salir.countDown(); pool.shutdownNow(); }
    }
}
