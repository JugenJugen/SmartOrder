/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartorder.model;

/**
 *
 * @author angel
 */

import java.util.ArrayList;
import java.util.List;

public class GestorPedidos {
    private static final List<Pedido> pedidos = new ArrayList<>();

    public static void agregarPedido(Pedido pedido) {
        if (pedido != null) {
            pedidos.add(pedido);
        }
    }

    public static List<Pedido> getPedidos() {
        return pedidos;
    }

    public static Pedido buscarPorId(int id) {
        for (Pedido p : pedidos) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    public static void limpiarPedidos() {
        pedidos.clear();
    }
}