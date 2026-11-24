package com.mycompany.smartorder.server;

import com.mycompany.smartorder.dao.ConexionBD;
import com.mycompany.smartorder.dao.MesaDAO;
import com.mycompany.smartorder.dao.PedidoDAO;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/** Servidor concurrente SmartOrder. Uso académico local. */
public class Servidor {
    public static final int PUERTO = 5555;

    public static void main(String[] args) {
        if (!ConexionBD.probarConexion()) {
            System.err.println("SQL Server no disponible. Revise db.properties.");
            return;
        }
        // Solo localhost: no exponer la serialización Java a Internet.
        try (ServerSocket servidor = new ServerSocket(PUERTO, 50,
                InetAddress.getByName("127.0.0.1"))) {
            escuchar(servidor, new PedidoDAO(), new MesaDAO());
        } catch (IOException e) {
            System.err.println("Error del servidor: " + e.getMessage());
        }
    }

    static void escuchar(ServerSocket servidor, PedidoDAO pedidos, MesaDAO mesas)
            throws IOException {
        // Hasta 20 sesiones; cada conexión recibe su propia tarea Runnable.
        ExecutorService pool = new ThreadPoolExecutor(0, 20, 60, TimeUnit.SECONDS,
                new SynchronousQueue<>(), Executors.defaultThreadFactory());
        System.out.println("SmartOrder escuchando en puerto " + servidor.getLocalPort());
        try {
            while (!servidor.isClosed()) {
                Socket cliente = servidor.accept();
                try {
                    pool.execute(new ManejadorCliente(cliente, pedidos, mesas));
                } catch (RejectedExecutionException e) {
                    cliente.close();
                    System.err.println("Servidor ocupado: conexión rechazada");
                }
            }
        } finally {
            pool.shutdownNow();
        }
    }
}
