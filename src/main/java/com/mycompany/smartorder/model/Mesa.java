/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartorder.model;

/**
 *
 * @author angel
 */

import com.mycompany.smartorder.model.Entida;
import com.mycompany.smartorder.excepciones.ValidacionException;
import java.util.ArrayList;
import java.util.List;

public class Mesa extends Entida {
    private int numeroMesa;
    private int capacidad;
    private String estado;
    private List<ItemPedido> productosPedido;

    public Mesa(int numeroMesa, int capacidad, String estado) {
        this.id = numeroMesa;
        this.numeroMesa = numeroMesa;
        this.capacidad = capacidad;
        this.estado = (estado != null && !estado.trim().isEmpty()) ? estado.trim() : "Disponible";
        this.productosPedido = new ArrayList<>();
    }

    @Override
    public void validar() throws ValidacionException {
        if (numeroMesa <= 0) {
            throw new ValidacionException("El número de mesa debe ser mayor que cero.");
        }
        if (capacidad <= 0) {
            throw new ValidacionException("La capacidad de la mesa debe ser mayor que cero.");
        }
        if (estado == null || estado.trim().isEmpty()) {
            throw new ValidacionException("El estado de la mesa no puede estar vacío.");
        }
    }

    public int getNumeroMesa() {
        return numeroMesa;
    }

    public void setNumeroMesa(int numeroMesa) {
        this.numeroMesa = numeroMesa;
        this.id = numeroMesa;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = (estado != null && !estado.trim().isEmpty()) ? estado.trim() : "Disponible";
    }

    public List<ItemPedido> getProductosPedido() {
        return productosPedido;
    }

    public void agregarProducto(String nombre, double precio) {
        if (nombre == null || nombre.trim().isEmpty() || precio < 0) {
            return;
        }
        String nombreLimpio = nombre.trim();
        for (ItemPedido item : productosPedido) {
            if (item.getNombre().equalsIgnoreCase(nombreLimpio)) {
                item.setCantidad(item.getCantidad() + 1);
                return;
            }
        }
        productosPedido.add(new ItemPedido(nombreLimpio, precio, 1));
    }

    public double calcularTotal() {
        double total = 0.0;
        for (ItemPedido item : productosPedido) {
            total += item.getSubtotal();
        }
        return total;
    }

    public void limpiarPedido() {
        productosPedido.clear();
    }
}