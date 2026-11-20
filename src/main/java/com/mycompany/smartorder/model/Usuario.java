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


public class Usuario extends Entida {
    private String nombre;
    private String correo;
    private String contraseña;
    private String rol; // Administrador, Cocina, Atención
    private boolean estado;

    public Usuario(int id, String nombre, String correo, String contraseña, String rol, boolean estado) {
        this.id = id;
        setNombre(nombre);
        setCorreo(correo);
        this.contraseña = contraseña;
        setRol(rol);
        this.estado = estado;
    }

    @Override
    public void validar() throws ValidacionException {
        if (id <= 0) {
            throw new ValidacionException("La identificación del usuario debe ser mayor que cero.");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ValidacionException("El nombre del usuario no puede estar vacío.");
        }
        String correoLimpio = (correo != null) ? correo.trim() : "";
        if (correoLimpio.isEmpty() || !correoLimpio.contains("@")) {
            throw new ValidacionException("Debe proporcionar un correo electrónico válido.");
        }
        if (contraseña == null || contraseña.trim().isEmpty()) {
            throw new ValidacionException("La contraseña no puede estar vacía.");
        }
        if (rol == null || rol.trim().isEmpty()) {
            throw new ValidacionException("El usuario debe tener un rol asignado.");
        }
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = (nombre != null) ? nombre.trim() : null;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = (correo != null) ? correo.trim() : null;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = (rol != null) ? rol.trim() : null;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }
}