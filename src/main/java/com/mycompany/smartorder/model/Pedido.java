/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartorder.model;

/**
 *
 * @author angel
 */

import com.mycompany.smartorder.excepciones.ValidacionException;
import java.util.ArrayList;
import java.util.List;

public class Pedido extends Entida {
    private Mesa mesa;
    private Usuario usuario;
    private String estado; // Recibido, En preparación, Listo, Entregado, Cancelado
    private String tipoPedido; // Local o Llevar
    private double total;
    private List<DetallePedido> detalles;

    public Pedido(int id, Mesa mesa, Usuario usuario, String tipoPedido) {
        this.id = id;
        this.mesa = mesa;
        this.usuario = usuario;
        this.tipoPedido = (tipoPedido != null) ? tipoPedido.trim() : "";
        this.estado = "Recibido";
        this.detalles = new ArrayList<>();
    }

    public void agregarDetalle(DetallePedido detalle) {
        if (detalle != null) {
            this.detalles.add(detalle);
            calcularTotal();
        }
    }

    public void eliminarDetalle(DetallePedido detalle) {
        if (detalle != null && this.detalles.remove(detalle)) {
            calcularTotal();
        }
    }

    public void limpiarDetalles() {
        this.detalles.clear();
        this.total = 0.0;
    }

    public double calcularTotal() {
        this.total = 0.0;
        for (DetallePedido detalle : detalles) {
            this.total += detalle.getSubtotal();
        }
        return this.total;
    }

    @Override
    public void validar() throws ValidacionException {
        if (id <= 0) {
            throw new ValidacionException("El identificador del pedido debe ser mayor que cero.");
        }
        if (detalles.isEmpty()) {
            throw new ValidacionException("No se puede confirmar un pedido sin productos seleccionados.");
        }
        if (tipoPedido == null || tipoPedido.trim().isEmpty()) {
            throw new ValidacionException("Debe indicar si el pedido es para consumir en el local o para llevar.");
        }
        for (DetallePedido detalle : detalles) {
            detalle.validar();
        }
    }

    public Mesa getMesa() {
        return mesa;
    }

    public void setMesa(Mesa mesa) {
        this.mesa = mesa;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        if (estado != null && !estado.trim().isEmpty()) {
            this.estado = estado.trim();
        }
    }

    public String getTipoPedido() {
        return tipoPedido;
    }

    public void setTipoPedido(String tipoPedido) {
        this.tipoPedido = (tipoPedido != null) ? tipoPedido.trim() : "";
    }

    public double getTotal() {
        return total;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }
}