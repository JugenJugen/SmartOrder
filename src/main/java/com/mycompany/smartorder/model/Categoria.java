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

public class Categoria extends Entida {
    private String nombre;
    private String descripcion;

    public Categoria(int id, String nombre) {
        this.id = id;
        setNombre(nombre);
        this.descripcion = "";
    }

    public Categoria(int id, String nombre, String descripcion) {
        this.id = id;
        setNombre(nombre);
        setDescripcion(descripcion);
    }

    @Override
    public void validar() throws ValidacionException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ValidacionException("El nombre de la categoría no puede estar vacío.");
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

    @Override
    public String toString() {
        return nombre != null ? nombre : "";
    }
}