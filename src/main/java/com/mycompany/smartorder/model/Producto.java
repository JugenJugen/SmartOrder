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

public class Producto extends Entida {
    private String nombre;
    private String descripcion;
    private double precio;
    private boolean disponibilidad;
    private int stock;
    private Categoria categoria;

    public Producto(String nombre, double precio) {
        setNombre(nombre);
        this.descripcion = "";
        this.precio = precio;
        this.disponibilidad = true;
        this.stock = 0;
        this.categoria = null;
    }

    @Override
    public void validar() throws ValidacionException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ValidacionException("El nombre del producto no puede estar vacío.");
        }
        if (precio <= 0) {
            throw new ValidacionException("El precio del producto debe ser mayor a cero.");
        }
        if (stock < 0) {
            throw new ValidacionException("El stock del producto no puede ser negativo.");
        }
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = (nombre != null) ? nombre.trim() : null;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = (descripcion != null) ? descripcion.trim() : "";
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public boolean isDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(boolean disponibilidad) {
        this.disponibilidad = disponibilidad;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }
}
