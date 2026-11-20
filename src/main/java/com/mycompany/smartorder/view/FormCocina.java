/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartorder.view;

/**
 *
 * @author angel
 */

import com.mycompany.smartorder.model.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class FormCocina extends JFrame {
    private JTabbedPane tabEstadosCocina;
    private JTable tablaPendientes, tablaEnPreparacion, tablaListos, tablaEntregados;
    private DefaultTableModel modeloPendientes, modeloEnPreparacion, modeloListos, modeloEntregados;
    private JButton btnActualizar;
    private JLabel lblResumenCocina;

    private static final Color COLOR_FONDO = new Color(245, 247, 250);
    private static final Color COLOR_BLANCO = Color.WHITE;
    private static final Color COLOR_NARANJA = new Color(230, 126, 34);
    private static final Color COLOR_VERDE = new Color(46, 204, 113);
    private static final Color COLOR_AZUL = new Color(52, 152, 219);
    private static final Color COLOR_OSCURO = new Color(33, 43, 54);

    public FormCocina() {
        setTitle("SmartOrder - Estación de Cocina");
        setSize(1040, 660);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setJMenuBar(crearBarraNavegacion(true));
        getContentPane().setBackground(COLOR_FONDO);

        // --- 1. ENCABEZADO SUPERIOR ---
        JPanel panelTop = new JPanel(new BorderLayout());
        panelTop.setBackground(COLOR_OSCURO);
        panelTop.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JPanel panelTitulos = new JPanel();
        panelTitulos.setLayout(new BoxLayout(panelTitulos, BoxLayout.Y_AXIS));
        panelTitulos.setBackground(COLOR_OSCURO);

        JLabel lblTitulo = new JLabel("Estación de Cocina");
        lblTitulo.setForeground(COLOR_BLANCO);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));

        lblResumenCocina = new JLabel("Pedidos registrados: 0");
        lblResumenCocina.setForeground(new Color(200, 210, 220));
        lblResumenCocina.setFont(new Font("Arial", Font.PLAIN, 12));

        panelTitulos.add(lblTitulo);
        panelTitulos.add(Box.createVerticalStrut(2));
        panelTitulos.add(lblResumenCocina);

        btnActualizar = new JButton("Actualizar Pedidos");
        btnActualizar.setBackground(COLOR_NARANJA);
        btnActualizar.setForeground(COLOR_BLANCO);
        btnActualizar.setFont(new Font("Arial", Font.BOLD, 12));
        btnActualizar.setFocusPainted(false);
        btnActualizar.setPreferredSize(new Dimension(165, 36));
        btnActualizar.addActionListener(e -> cargarPedidosEnTablas());

        panelTop.add(panelTitulos, BorderLayout.WEST);
        panelTop.add(btnActualizar, BorderLayout.EAST);
        add(panelTop, BorderLayout.NORTH);

        // --- 2. MODELOS Y PESTAÑAS DE ESTADOS ---
        String[] columnas = {"# de Pedido", "Mesa / Tipo", "Detalle de Productos", "Estado"};

        modeloPendientes = crearModeloNoEditable(columnas);
        modeloEnPreparacion = crearModeloNoEditable(columnas);
        modeloListos = crearModeloNoEditable(columnas);
        modeloEntregados = crearModeloNoEditable(columnas);

        tablaPendientes = estilizarTabla(new JTable(modeloPendientes));
        tablaEnPreparacion = estilizarTabla(new JTable(modeloEnPreparacion));
        tablaListos = estilizarTabla(new JTable(modeloListos));
        tablaEntregados = estilizarTabla(new JTable(modeloEntregados));

        tabEstadosCocina = new JTabbedPane();
        tabEstadosCocina.setFont(new Font("Arial", Font.BOLD, 13));
        tabEstadosCocina.setBackground(COLOR_BLANCO);
        tabEstadosCocina.setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));

        tabEstadosCocina.add(
            "Pendientes",
            crearPanelEstado(tablaPendientes, "En preparación", "Marcar En Preparación", COLOR_NARANJA)
        );
        tabEstadosCocina.add(
            "En Preparación",
            crearPanelEstado(tablaEnPreparacion, "Listo", "Marcar Listo", COLOR_AZUL)
        );
        tabEstadosCocina.add(
            "Listos",
            crearPanelEstado(tablaListos, "Entregado", "Marcar Entregado", COLOR_VERDE)
        );

        JPanel panelEntregados = new JPanel(new BorderLayout());
        panelEntregados.setBackground(COLOR_BLANCO);
        panelEntregados.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panelEntregados.add(new JScrollPane(tablaEntregados), BorderLayout.CENTER);
        tabEstadosCocina.add("Entregados", panelEntregados);

        tabEstadosCocina.addChangeListener(e -> cargarPedidosEnTablas());

        add(tabEstadosCocina, BorderLayout.CENTER);

        cargarPedidosEnTablas();
    }

    private JMenuBar crearBarraNavegacion(boolean esCocina) {
        JMenuBar menuBar = new JMenuBar();
        JMenu menuNavegacion = new JMenu("Cambiar Vista");

        JMenuItem itemCliente = new JMenuItem("Vista Cliente (Pedidos)");
        JMenuItem itemCocina = new JMenuItem("Vista Cocina");
        JMenuItem itemAdmin = new JMenuItem("Vista Administración");

        itemCliente.addActionListener(e -> {
            if (esCocina) {
                FormClientePedido cliente = new FormClientePedido();
                cliente.setVisible(true);
                cliente.toFront();
                dispose();
            }
        });

        itemCocina.addActionListener(e -> {
            if (!esCocina) {
                FormCocina cocina = new FormCocina();
                cocina.cargarPedidosEnTablas();
                cocina.setVisible(true);
                cocina.toFront();
                dispose();
            }
        });

        itemAdmin.addActionListener(e -> {
            FormAdmin admin = new FormAdmin();
            admin.setVisible(true);
            admin.toFront();
            dispose();
        });

        itemCliente.setEnabled(esCocina);
        itemCocina.setEnabled(!esCocina);

        menuNavegacion.add(itemCliente);
        menuNavegacion.add(itemCocina);
        menuNavegacion.add(itemAdmin);
        menuBar.add(menuNavegacion);
        return menuBar;
    }

    private JTable estilizarTabla(JTable tabla) {
        tabla.setRowHeight(28);
        tabla.setFont(new Font("Arial", Font.PLAIN, 13));
        tabla.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        tabla.getTableHeader().setBackground(new Color(235, 239, 244));
        tabla.getTableHeader().setForeground(COLOR_OSCURO);
        tabla.setGridColor(new Color(225, 230, 236));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return tabla;
    }

    private DefaultTableModel crearModeloNoEditable(String[] columnas) {
        return new DefaultTableModel(new Object[][]{}, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JPanel crearPanelEstado(JTable tabla, String siguienteEstado, String textoBoton, Color colorBoton) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(COLOR_BLANCO);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(COLOR_BLANCO);
        panel.add(scroll, BorderLayout.CENTER);

        JButton btnAvanzar = new JButton(textoBoton);
        btnAvanzar.setBackground(colorBoton);
        btnAvanzar.setForeground(COLOR_BLANCO);
        btnAvanzar.setFont(new Font("Arial", Font.BOLD, 12));
        btnAvanzar.setFocusPainted(false);
        btnAvanzar.setPreferredSize(new Dimension(190, 35));
        btnAvanzar.addActionListener(e -> avanzarEstadoPedido(tabla, siguienteEstado));

        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelSur.setBackground(COLOR_BLANCO);
        panelSur.add(btnAvanzar);
        panel.add(panelSur, BorderLayout.SOUTH);

        return panel;
    }

    private void avanzarEstadoPedido(JTable tabla, String siguienteEstado) {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Seleccione un pedido de la tabla para cambiar su estado.",
                "Aviso",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int idPedido = Integer.parseInt(tabla.getValueAt(fila, 0).toString());

        for (Pedido p : GestorPedidos.getPedidos()) {
            if (p.getId() == idPedido) {
                p.setEstado(siguienteEstado);
                break;
            }
        }

        cargarPedidosEnTablas();
    }

    public void cargarPedidosEnTablas() {
        if (modeloPendientes == null || modeloEnPreparacion == null
                || modeloListos == null || modeloEntregados == null) {
            return;
        }

        modeloPendientes.setRowCount(0);
        modeloEnPreparacion.setRowCount(0);
        modeloListos.setRowCount(0);
        modeloEntregados.setRowCount(0);

        for (Pedido p : GestorPedidos.getPedidos()) {
            StringBuilder detalleTexto = new StringBuilder();
            for (DetallePedido det : p.getDetalles()) {
                String nombreProd = (det.getProducto() != null && det.getProducto().getNombre() != null)
                        ? det.getProducto().getNombre()
                        : "Producto";

                detalleTexto.append(det.getCantidad()).append("x ").append(nombreProd);
                if (det.getObservacion() != null && !det.getObservacion().isBlank()) {
                    detalleTexto.append(" [").append(det.getObservacion()).append("]");
                }
                detalleTexto.append(" | ");
            }

            String textoDetalle = detalleTexto.length() > 3
                    ? detalleTexto.substring(0, detalleTexto.length() - 3)
                    : "";

            String tipoPedido = (p.getTipoPedido() != null && !p.getTipoPedido().isBlank())
                    ? p.getTipoPedido().trim()
                    : "Local";

            String textoMesa = (p.getMesa() != null)
                    ? "Mesa #" + String.format("%02d", p.getMesa().getNumeroMesa()) + " (" + tipoPedido + ")"
                    : "Sin mesa (" + tipoPedido + ")";

            Object[] fila = {
                p.getId(),
                textoMesa,
                textoDetalle,
                p.getEstado()
            };

            String estado = (p.getEstado() != null) ? p.getEstado().trim() : "Recibido";
            if ("Recibido".equalsIgnoreCase(estado) || "Pendiente".equalsIgnoreCase(estado)) {
                modeloPendientes.addRow(fila);
            } else if ("En preparación".equalsIgnoreCase(estado) || "En preparacion".equalsIgnoreCase(estado)) {
                modeloEnPreparacion.addRow(fila);
            } else if ("Listo".equalsIgnoreCase(estado)) {
                modeloListos.addRow(fila);
            } else if ("Entregado".equalsIgnoreCase(estado)) {
                modeloEntregados.addRow(fila);
            }
        }

        if (lblResumenCocina != null) {
            lblResumenCocina.setText("Pedidos registrados: " + GestorPedidos.getPedidos().size());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FormCocina().setVisible(true);
        });
    }
}