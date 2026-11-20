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

public class DetallePedido {
    private Producto producto;
    private int cantidad;
    private double precioUnitario;
    private String observacion;

    public DetallePedido(Producto producto, int cantidad, String observacion) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = (producto != null) ? producto.getPrecio() : 0.0;
        setObservacion(observacion);
    }

    public double getSubtotal() {
        return this.cantidad * this.precioUnitario;
    }

    public void validar() throws ValidacionException {
        if (producto == null) {
            throw new ValidacionException("El detalle debe estar asociado a un producto válido.");
        }
        if (cantidad <= 0) {
            throw new ValidacionException("La cantidad del producto debe ser mayor que cero.");
        }
        if (precioUnitario < 0) {
            throw new ValidacionException("El precio unitario no puede ser negativo.");
        }
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
        if (producto != null) {
            this.precioUnitario = producto.getPrecio();
        }
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = (observacion != null) ? observacion.trim() : "";
    }
}