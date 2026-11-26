/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartorder.view;

/**
 *
 * @author angel
 */
import com.mycompany.smartorder.client.Response;
import com.mycompany.smartorder.controller.PedidoClienteController;
import com.mycompany.smartorder.dao.MesaDAO;
import com.mycompany.smartorder.dao.ProductoDAO;
import com.mycompany.smartorder.model.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class FormClientePedido extends JFrame {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final MesaDAO mesaDAO = new MesaDAO();

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
    private final List<DefaultTableModel> modelosCategorias = new ArrayList<>();
    private final String[] ordenCategorias = {"Bebidas", "Entradas", "Platos Fuertes", "Postres"};

    private List<Mesa> mesasDisponibles = new ArrayList<>();
    private int mesaSeleccionadaIndex = 0;
    private PedidoClienteController controlador;

    public FormClientePedido() {
        setTitle("SmartOrder - Vista de Clientes / Toma de Pedidos");
        setSize(1050, 770);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setJMenuBar(crearBarraNavegacion(false));

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

        cardLayout = new CardLayout();
        panelCards = new JPanel(cardLayout);
        panelCards.setBackground(Color.WHITE);

        for (String cat : ordenCategorias) {
            DefaultTableModel modeloCat = new DefaultTableModel(new Object[][]{}, new String[]{"Nombre", "Descripción", "Precio (₡)"}) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            modelosCategorias.add(modeloCat);
            panelCards.add(crearPanelCategoria(modeloCat), cat);
        }

        btnBebidas.addActionListener(e -> {
            cardLayout.show(panelCards, "Bebidas");
            resaltarBotonActivo(btnBebidas);
        });
        btnEntradas.addActionListener(e -> {
            cardLayout.show(panelCards, "Entradas");
            resaltarBotonActivo(btnEntradas);
        });
        btnPlatos.addActionListener(e -> {
            cardLayout.show(panelCards, "Platos Fuertes");
            resaltarBotonActivo(btnPlatos);
        });
        btnPostres.addActionListener(e -> {
            cardLayout.show(panelCards, "Postres");
            resaltarBotonActivo(btnPostres);
        });

        cardLayout.show(panelCards, "Entradas");
        resaltarBotonActivo(btnEntradas);

        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.add(panelCards, BorderLayout.CENTER);

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

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (controlador != null) {
                    controlador.cerrar();
                }
            }
        });

        cargarCatalogoYMesasDesdeBD();
        conectarPunto("MESA", 1);
    }

    private void cargarCatalogoYMesasDesdeBD() {
        int numeroActual = obtenerMesaActual().getNumeroMesa();
        new SwingWorker<Void, Void>() {
            private List<Mesa> mesasBD;
            private List<Producto> productosBD;
            private Exception error;

            @Override
            protected Void doInBackground() {
                try {
                    mesasBD = mesaDAO.listar();
                    productosBD = productoDAO.listarDisponibles();
                } catch (Exception ex) {
                    error = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                if (error != null) {
                    JOptionPane.showMessageDialog(
                            FormClientePedido.this,
                            "No se pudo cargar el menú/mesas desde SQL Server:\n" + error.getMessage(),
                            "Error de base de datos",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                if (mesasBD != null && !mesasBD.isEmpty()) {
                    mesasDisponibles = mesasBD;
                    mesaSeleccionadaIndex = 0;
                    for (int i = 0; i < mesasDisponibles.size(); i++) {
                        if (mesasDisponibles.get(i).getNumeroMesa() == numeroActual) {
                            mesaSeleccionadaIndex = i;
                            break;
                        }
                    }
                    actualizarEtiquetasMesa();
                    actualizarTablaResumen();
                }

                for (int i = 0; i < ordenCategorias.length; i++) {
                    String catNombre = ordenCategorias[i];
                    DefaultTableModel modelo = modelosCategorias.get(i);
                    modelo.setRowCount(0);
                    if (productosBD != null) {
                        for (Producto p : productosBD) {
                            String catProd = (p.getCategoria() != null) ? p.getCategoria().getNombre() : "";
                            if (catProd.equalsIgnoreCase(catNombre)) {
                                modelo.addRow(new Object[]{
                                    p.getNombre(),
                                    p.getDescripcion() != null ? p.getDescripcion() : "",
                                    p.getPrecio()
                                });
                            }
                        }
                    }
                }
            }
        }.execute();
    }

    private Mesa obtenerMesaActual() {
        if (mesasDisponibles.isEmpty()) {
            mesasDisponibles.add(new Mesa(1, 4, "Disponible"));
            mesaSeleccionadaIndex = 0;
        }
        if (mesaSeleccionadaIndex < 0 || mesaSeleccionadaIndex >= mesasDisponibles.size()) {
            mesaSeleccionadaIndex = 0;
        }
        return mesasDisponibles.get(mesaSeleccionadaIndex);
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
            JOptionPane.showMessageDialog(this,
                    "La Mesa #" + mesaActual.getNumeroMesa() + " no tiene productos en el resumen.",
                    "Pedido vacío", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (controlador == null) {
            JOptionPane.showMessageDialog(this, "Aún no hay conexión con el servidor.",
                    "Sin conexión", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tipoPedido = cmbTipoPedido.getSelectedItem().toString();
        String observacion = txtObservacion.getText().trim();
        List<ItemPedido> items = new ArrayList<>(mesaActual.getProductosPedido());

        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return controlador.enviarPedido(mesaActual.getNumeroMesa(), tipoPedido, observacion, items);
            }

            @Override
            protected void done() {
                try {
                    int id = get();
                    mesaActual.limpiarPedido();
                    txtObservacion.setText("");
                    cmbTipoPedido.setSelectedIndex(0);
                    actualizarTablaResumen();
                    cargarCatalogoYMesasDesdeBD();
                    JOptionPane.showMessageDialog(FormClientePedido.this,
                            "Pedido #" + id + " (" + tipoPedido + ") guardado en BD y enviado a cocina para la Mesa #"
                                    + mesaActual.getNumeroMesa() + ".",
                            "Enviado", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    String detalle = (ex.getCause() != null && ex.getCause().getMessage() != null)
                            ? ex.getCause().getMessage()
                            : ex.getMessage();
                    JOptionPane.showMessageDialog(FormClientePedido.this,
                            "No se pudo enviar el pedido:\n" + detalle,
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private JPanel crearPanelCategoria(DefaultTableModel modeloCat) {
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
        if (mesasDisponibles.isEmpty()) {
            return;
        }
        String[] nombresMesas = new String[mesasDisponibles.size()];
        for (int i = 0; i < mesasDisponibles.size(); i++) {
            Mesa m = mesasDisponibles.get(i);
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
            int nuevoIndex = mesaSeleccionadaIndex;
            for (int i = 0; i < nombresMesas.length; i++) {
                if (nombresMesas[i].equals(seleccion)) {
                    nuevoIndex = i;
                    break;
                }
            }
            mesaSeleccionadaIndex = nuevoIndex;
            Mesa nuevaMesa = obtenerMesaActual();
            actualizarEtiquetasMesa();
            actualizarTablaResumen();
            conectarPunto("MESA", nuevaMesa.getNumeroMesa());
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
        String tipo = args.length > 0 ? args[0] : "MESA";
        int numero = args.length > 1 ? Integer.parseInt(args[1]) : 1;
        SwingUtilities.invokeLater(() -> {
            FormClientePedido f = new FormClientePedido();
            f.conectarPunto(tipo, numero);
            f.setVisible(true);
        });
    }

    private void conectarPunto(String tipo, int numero) {
        if (controlador != null) {
            controlador.cerrar();
            controlador = null;
        }
        new SwingWorker<PedidoClienteController, Void>() {
            @Override
            protected PedidoClienteController doInBackground() throws Exception {
                return new PedidoClienteController("localhost", 5555, tipo, numero);
            }

            @Override
            protected void done() {
                try {
                    controlador = get();
                    if ("MESA".equals(tipo)) {
                        for (int i = 0; i < mesasDisponibles.size(); i++) {
                            if (mesasDisponibles.get(i).getNumeroMesa() == numero) {
                                mesaSeleccionadaIndex = i;
                                break;
                            }
                        }
                        actualizarEtiquetasMesa();
                        actualizarTablaResumen();
                    }
                    controlador.setOnEvento(ev -> SwingUtilities.invokeLater(() -> manejarEvento(ev)));
                    setTitle("SmartOrder - " + tipo + " " + numero);
                } catch (Exception ex) {
                    String detalle = (ex.getCause() != null && ex.getCause().getMessage() != null)
                            ? ex.getCause().getMessage()
                            : ex.getMessage();
                    JOptionPane.showMessageDialog(FormClientePedido.this,
                            "No se pudo conectar al servidor:\n" + detalle,
                            "Error de conexión", JOptionPane.ERROR_MESSAGE);
                    dispose();
                }
            }
        }.execute();
    }

    private void manejarEvento(Response ev) {
        System.out.println("Evento recibido: " + ev.getMessage() + " -> " + ev.getData());
    }
}




