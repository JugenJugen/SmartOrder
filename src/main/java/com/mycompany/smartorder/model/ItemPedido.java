/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartorder.model;

/**
 *
 * @author angel
 */
public class ItemPedido {
    private String nombre;
    private double precioUnitario;
    private int cantidad;

    public ItemPedido(String nombre, double precioUnitario, int cantidad) {
        setNombre(nombre);
        setPrecioUnitario(precioUnitario);
        setCantidad(cantidad);
    }

    public double getSubtotal() {
        return this.precioUnitario * this.cantidad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = (nombre != null) ? nombre.trim() : "";
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = Math.max(0.0, precioUnitario);
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = Math.max(0, cantidad);
    }
}
