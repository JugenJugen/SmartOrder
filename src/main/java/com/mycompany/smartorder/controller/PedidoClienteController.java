package com.mycompany.smartorder.controller;

import com.mycompany.smartorder.client.ClientConnector;
import com.mycompany.smartorder.client.Response;
import com.mycompany.smartorder.excepciones.ConexionException;
import com.mycompany.smartorder.model.ItemPedido;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 *
 * @author joshar
 */
public class PedidoClienteController {
    private final ClientConnector conexion;
    
    public PedidoClienteController(String host, int port, String tipo, int numero) throws ConexionException {
        this.conexion = new ClientConnector(host, port);
        try {
            this.conexion.conectarComoPunto(tipo, numero);
        } catch (ConexionException e) {
            this.conexion.close();
            throw e;
        }
    }
    
    public void setOnEvento(Consumer<Response> listener) {
        conexion.setOnEvento(listener);
    }
    
    public int enviarPedido(int mesa, String tipoPedido, String observacion, List<ItemPedido> items) throws ConexionException {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (ItemPedido it : items) {
            Map<String, Object> m = new HashMap<>();
            m.put("producto", it.getNombre());
            m.put("precio", it.getPrecioUnitario());
            m.put("cantidad", it.getCantidad());
            lista.add(m);
        }
        
        Map<String, Object> p = new HashMap<>();
        p.put("mesa", mesa);
        p.put("tipoPedido", tipoPedido);
        p.put("observacion", observacion);
        p.put("lista", lista);
        
        Response r = conexion.enviar("crearPedido", p, 5000);
        if (!r.isSuccess()) {
           throw new ConexionException(r.getMessage());
        }
        return (Integer) r.getData();
    }
    
    public void cerrar() {
        conexion.close();
    }
    
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listarPedidos() throws ConexionException {
        Response r = conexion.enviar("listarPedidos", new HashMap<>(), 5000);
        if (!r.isSuccess()) {
            throw new ConexionException(r.getMessage());
        }
        return (List<Map<String, Object>>) r.getData();
    }

    public void actualizarEstado(int pedidoId, String nuevoEstado) throws ConexionException {
        Map<String, Object> p = new HashMap<>();
        p.put("pedidoId", pedidoId);
        p.put("nuevoEstado", nuevoEstado);
        Response r = conexion.enviar("actualizarEstado", p, 5000);
        if (!r.isSuccess()) {
            throw new ConexionException(r.getMessage());
        }
    }
}
