/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartorder.view;

/**
 *
 * @author angel
 */

import com.mycompany.smartorder.excepciones.ValidacionException;
import com.mycompany.smartorder.model.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class FormClientePedido extends JFrame {
    private JTable tablaResumen;
    private DefaultTableModel modeloResumen;
    private JButton btnContinuar;
    private JButton btnBebidas, btnEntradas, btnPlatos, btnPostres;
    private JPanel panelCards;
    private CardLayout cardLayout;
    private JLabel lblTotalPagar;
    private JComboBox<String> cmbTipoPedido;
    private JTextField txtObservacion;
    private final List<JLabel> etiquetasMesa = new ArrayList<>();
    private int mesaSeleccionadaIndex = 0;

    public FormClientePedido() {
        setTitle("SmartOrder - Vista de Clientes / Toma de Pedidos");
        setSize(1050, 770);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setJMenuBar(crearBarraNavegacion(false));

        List<Mesa> mesasIniciales = obtenerMesasDisponibles();
        if (mesasIniciales.size() > 2) {
            mesaSeleccionadaIndex = 2; // Mesa #03 por defecto si existe
        }

        // --- 1. PANEL LATERAL IZQUIERDO (Menú / Categorías) ---
        JPanel panelLateral = new JPanel();
        panelLateral.setPreferredSize(new Dimension(180, 0));
        panelLateral.setBackground(new Color(245, 247, 250));
        panelLateral.setLayout(new GridLayout(6, 1, 5, 5));

        JLabel lblTituloCat = new JLabel("  Menú / Categorías");
        lblTituloCat.setFont(new Font("Arial", Font.BOLD, 12));

        btnBebidas = new JButton("Bebidas");
        btnEntradas = new JButton("Entradas");
        btnPlatos = new JButton("Platos Fuertes");
        btnPostres = new JButton("Postres");

        panelLateral.add(lblTituloCat);
        panelLateral.add(btnBebidas);
        panelLateral.add(btnEntradas);
        panelLateral.add(btnPlatos);
        panelLateral.add(btnPostres);

        add(panelLateral, BorderLayout.WEST);

        // --- 2. PANEL CENTRAL (Catálogo con CardLayout desde FormAdmin) ---
        cardLayout = new CardLayout();
        panelCards = new JPanel(cardLayout);
        panelCards.setBackground(Color.WHITE);

        panelCards.add(crearPanelCategoria(obtenerDatosPorCategoria("Bebidas")), "BEBIDAS");
        panelCards.add(crearPanelCategoria(obtenerDatosPorCategoria("Entradas")), "ENTRADAS");
        panelCards.add(crearPanelCategoria(obtenerDatosPorCategoria("Platos Fuertes")), "PLATOS");
        panelCards.add(crearPanelCategoria(obtenerDatosPorCategoria("Postres")), "POSTRES");

        btnBebidas.addActionListener(e -> {
            cardLayout.show(panelCards, "BEBIDAS");
            resaltarBotonActivo(btnBebidas);
        });
        btnEntradas.addActionListener(e -> {
            cardLayout.show(panelCards, "ENTRADAS");
            resaltarBotonActivo(btnEntradas);
        });
        btnPlatos.addActionListener(e -> {
            cardLayout.show(panelCards, "PLATOS");
            resaltarBotonActivo(btnPlatos);
        });
        btnPostres.addActionListener(e -> {
            cardLayout.show(panelCards, "POSTRES");
            resaltarBotonActivo(btnPostres);
        });

        cardLayout.show(panelCards, "ENTRADAS");
        resaltarBotonActivo(btnEntradas);

        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.add(panelCards, BorderLayout.CENTER);

        // --- 3. SECCIÓN INFERIOR: RESUMEN, TIPO, OBSERVACIÓN Y ENVÍO ---
        JPanel panelInferior = new JPanel();
        panelInferior.setLayout(new BoxLayout(panelInferior, BoxLayout.Y_AXIS));
        panelInferior.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        panelInferior.setBackground(Color.WHITE);

        JLabel lblResumen = new JLabel("Resumen del Pedido (Mesa Actual)");
        lblResumen.setFont(new Font("Arial", Font.BOLD, 16));
        panelInferior.add(lblResumen);
        panelInferior.add(Box.createVerticalStrut(5));

        String[] colResumen = {"Nombre", "Precio Unitario", "Cantidad", "Subtotal", "Controles (+ / - / Eliminar)"};
        modeloResumen = new DefaultTableModel(new Object[][]{}, colResumen) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };
        tablaResumen = new JTable(modeloResumen);

        // Controles en la tabla de resumen: restar, sumar o eliminar
        tablaResumen.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tablaResumen.rowAtPoint(e.getPoint());
                int columna = tablaResumen.columnAtPoint(e.getPoint());

                if (columna == 4 && fila != -1) {
                    Mesa mesaActual = obtenerMesaActual();
                    List<ItemPedido> productosMesa = mesaActual.getProductosPedido();

                    if (fila < productosMesa.size()) {
                        ItemPedido item = productosMesa.get(fila);
                        Rectangle rect = tablaResumen.getCellRect(fila, columna, true);
                        int xClick = e.getX() - rect.x;

                        if (xClick < rect.width / 3) {
                            if (item.getCantidad() > 1) {
                                item.setCantidad(item.getCantidad() - 1);
                            } else {
                                productosMesa.remove(fila);
                            }
                        } else if (xClick < (rect.width * 2) / 3) {
                            item.setCantidad(item.getCantidad() + 1);
                        } else {
                            productosMesa.remove(fila);
                        }
                        actualizarTablaResumen();
                    }
                }
            }
        });

        JScrollPane scrollResumen = new JScrollPane(tablaResumen);
        scrollResumen.setPreferredSize(new Dimension(0, 120));
        panelInferior.add(scrollResumen);
        panelInferior.add(Box.createVerticalStrut(8));

        // Fila de Tipo de Pedido y Observaciones
        JPanel panelOpcionesPedido = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelOpcionesPedido.setBackground(Color.WHITE);

        JLabel lblTipo = new JLabel("Tipo de Pedido:");
        lblTipo.setFont(new Font("Arial", Font.BOLD, 12));
        cmbTipoPedido = new JComboBox<>(new String[]{"Local", "Llevar"});
        cmbTipoPedido.setBackground(Color.WHITE);

        JLabel lblObs = new JLabel("Observaciones:");
        lblObs.setFont(new Font("Arial", Font.BOLD, 12));
        txtObservacion = new JTextField(28);

        panelOpcionesPedido.add(lblTipo);
        panelOpcionesPedido.add(cmbTipoPedido);
        panelOpcionesPedido.add(Box.createHorizontalStrut(10));
        panelOpcionesPedido.add(lblObs);
        panelOpcionesPedido.add(txtObservacion);

        panelInferior.add(panelOpcionesPedido);
        panelInferior.add(Box.createVerticalStrut(10));

        JPanel panelInferiorBotones = new JPanel(new BorderLayout());
        panelInferiorBotones.setBackground(Color.WHITE);

        lblTotalPagar = new JLabel("Total a Pagar: ₡0.00");
        lblTotalPagar.setFont(new Font("Arial", Font.BOLD, 14));

        btnContinuar = new JButton("Continuar / Enviar Pedido");
        btnContinuar.setBackground(new Color(230, 126, 34));
        btnContinuar.setForeground(Color.WHITE);
        btnContinuar.setFont(new Font("Arial", Font.BOLD, 12));
        btnContinuar.setPreferredSize(new Dimension(190, 35));
        btnContinuar.addActionListener(e -> confirmarYEnviarPedido());

        panelInferiorBotones.add(lblTotalPagar, BorderLayout.WEST);
        panelInferiorBotones.add(btnContinuar, BorderLayout.EAST);

        panelInferior.add(panelInferiorBotones);
        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);
        add(panelPrincipal, BorderLayout.CENTER);

        actualizarEtiquetasMesa();
        actualizarTablaResumen();
    }

    private List<Mesa> obtenerMesasDisponibles() {
        List<Mesa> mesas = FormAdmin.getMesas();
        if (mesaSeleccionadaIndex < 0 || mesaSeleccionadaIndex >= mesas.size()) {
            mesaSeleccionadaIndex = 0;
        }
        return mesas;
    }

    private Mesa obtenerMesaActual() {
        List<Mesa> mesas = obtenerMesasDisponibles();
        if (mesas.isEmpty()) {
            Mesa respaldo = new Mesa(1, 4, "Disponible");
            mesas.add(respaldo);
            mesaSeleccionadaIndex = 0;
        }
        return mesas.get(mesaSeleccionadaIndex);
    }

    private int generarSiguienteIdPedido() {
        int maxId = 0;
        for (Pedido p : GestorPedidos.getPedidos()) {
            if (p.getId() > maxId) {
                maxId = p.getId();
            }
        }
        return maxId + 1;
    }

    private Object[][] obtenerDatosPorCategoria(String nombreCategoria) {
        List<Object[]> filas = new ArrayList<>();
        for (Producto p : FormAdmin.getProductos()) {
            String cat = (p.getCategoria() != null && p.getCategoria().getNombre() != null)
                    ? p.getCategoria().getNombre()
                    : "";
            if (cat.equalsIgnoreCase(nombreCategoria) && p.isDisponibilidad()) {
                filas.add(new Object[]{
                    p.getNombre(),
                    p.getDescripcion() != null ? p.getDescripcion() : "",
                    p.getPrecio()
                });
            }
        }
        return filas.toArray(new Object[0][]);
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

    private void confirmarYEnviarPedido() {
        Mesa mesaActual = obtenerMesaActual();

        if (mesaActual.getProductosPedido().isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "La Mesa #" + mesaActual.getNumeroMesa() + " no tiene productos en el resumen.",
                "Pedido vacío",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String tipoPedido = cmbTipoPedido.getSelectedItem().toString();
        String observacion = txtObservacion.getText().trim();

        int nuevoId = generarSiguienteIdPedido();
        Pedido nuevoPedido = new Pedido(nuevoId, mesaActual, null, tipoPedido);

        for (ItemPedido item : mesaActual.getProductosPedido()) {
            Producto producto = new Producto(item.getNombre(), item.getPrecioUnitario());
            producto.setId(nuevoId);
            nuevoPedido.agregarDetalle(new DetallePedido(producto, item.getCantidad(), observacion));
        }

        try {
            nuevoPedido.validar();
            GestorPedidos.agregarPedido(nuevoPedido);

            mesaActual.limpiarPedido();
            txtObservacion.setText("");
            cmbTipoPedido.setSelectedIndex(0);
            actualizarTablaResumen();

            JOptionPane.showMessageDialog(
                this,
                "Pedido #" + nuevoId + " (" + tipoPedido + ") enviado a cocina para la Mesa #"
                        + mesaActual.getNumeroMesa() + ".",
                "Enviado",
                JOptionPane.INFORMATION_MESSAGE
            );
        } catch (ValidacionException ex) {
            JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Error de validación",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private JPanel crearPanelCategoria(Object[][] datosProductos) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);

        JPanel panelMesa = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelMesa.setBackground(Color.WHITE);

        Mesa mesaActual = obtenerMesaActual();
        JLabel lblMesa = new JLabel("Mesa #" + String.format("%02d", mesaActual.getNumeroMesa()));
        lblMesa.setFont(new Font("Arial", Font.BOLD, 16));
        etiquetasMesa.add(lblMesa);

        JButton btnCambiarMesa = new JButton("Cambiar Mesa");
        btnCambiarMesa.setBackground(new Color(230, 126, 34));
        btnCambiarMesa.setForeground(Color.WHITE);
        btnCambiarMesa.setFocusPainted(false);
        btnCambiarMesa.addActionListener(e -> mostrarSelectorMesas());

        panelMesa.add(lblMesa);
        panelMesa.add(btnCambiarMesa);
        panel.add(panelMesa, BorderLayout.NORTH);

        String[] colProductos = {"Nombre", "Descripción", "Precio (₡)"};
        DefaultTableModel modeloCat = new DefaultTableModel(datosProductos, colProductos) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tabla = new JTable(modeloCat);

        JButton btnAgregarRapido = new JButton("Agregar al Pedido de la Mesa");
        btnAgregarRapido.setBackground(new Color(46, 204, 113));
        btnAgregarRapido.setForeground(Color.WHITE);

        btnAgregarRapido.addActionListener(e -> {
            int filaSeleccionada = tabla.getSelectedRow();
            if (filaSeleccionada != -1) {
                String nombre = (String) tabla.getValueAt(filaSeleccionada, 0);
                double precio = ((Number) tabla.getValueAt(filaSeleccionada, 2)).doubleValue();

                Mesa mActual = obtenerMesaActual();
                mActual.agregarProducto(nombre, precio);
                actualizarTablaResumen();
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Por favor, seleccione un producto de la tabla.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
                );
            }
        });

        JPanel panelBotonTabla = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotonTabla.setBackground(Color.WHITE);
        panelBotonTabla.add(btnAgregarRapido);

        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(panelBotonTabla, BorderLayout.SOUTH);

        return panel;
    }

    private void actualizarEtiquetasMesa() {
        Mesa mesaActual = obtenerMesaActual();
        String texto = "Mesa #" + String.format("%02d", mesaActual.getNumeroMesa())
                + " (Cap. " + mesaActual.getCapacidad() + ")";
        for (JLabel lbl : etiquetasMesa) {
            lbl.setText(texto);
        }
    }

    private void actualizarTablaResumen() {
        modeloResumen.setRowCount(0);
        Mesa mesaActual = obtenerMesaActual();

        for (ItemPedido item : mesaActual.getProductosPedido()) {
            modeloResumen.addRow(new Object[]{
                item.getNombre(),
                "₡" + String.format("%.2f", item.getPrecioUnitario()),
                item.getCantidad(),
                "₡" + String.format("%.2f", item.getSubtotal()),
                "[  -  ]   [  +  ]   [  🗑  ]"
            });
        }
        lblTotalPagar.setText(
            String.format("Total a Pagar (Mesa #%02d): ₡%.2f", mesaActual.getNumeroMesa(), mesaActual.calcularTotal())
        );
    }

    private void mostrarSelectorMesas() {
        List<Mesa> mesas = obtenerMesasDisponibles();
        String[] nombresMesas = new String[mesas.size()];
        for (int i = 0; i < mesas.size(); i++) {
            Mesa m = mesas.get(i);
            nombresMesas[i] = "Mesa " + String.format("%02d", m.getNumeroMesa()) + " (Cap. " + m.getCapacidad() + ")";
        }

        String seleccion = (String) JOptionPane.showInputDialog(
            this,
            "Seleccione la mesa:",
            "Cambiar Mesa",
            JOptionPane.QUESTION_MESSAGE,
            null,
            nombresMesas,
            nombresMesas[mesaSeleccionadaIndex]
        );

        if (seleccion != null) {
            for (int i = 0; i < nombresMesas.length; i++) {
                if (nombresMesas[i].equals(seleccion)) {
                    mesaSeleccionadaIndex = i;
                    break;
                }
            }
            actualizarEtiquetasMesa();
            actualizarTablaResumen();
        }
    }

    private void resaltarBotonActivo(JButton botonActivo) {
        JButton[] botones = {btnBebidas, btnEntradas, btnPlatos, btnPostres};
        for (JButton b : botones) {
            b.setBackground(UIManager.getColor("Button.background"));
            b.setForeground(Color.BLACK);
            b.setOpaque(true);
        }
        botonActivo.setBackground(new Color(33, 43, 54));
        botonActivo.setForeground(Color.WHITE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FormClientePedido().setVisible(true);
        });
    }
}