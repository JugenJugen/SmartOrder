/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartorder.view;

/**
 *
 * @author angel
 */
import com.mycompany.smartorder.model.DetallePedido;
import com.mycompany.smartorder.model.Mesa;
import com.mycompany.smartorder.model.Pedido;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import com.mycompany.smartorder.dao.PedidoDAO;
import com.mycompany.smartorder.excepciones.DAOException;
import com.mycompany.smartorder.model.GestorPedidos;

public class FormPedido extends JFrame {

    private static final Color BG_MAIN = new Color(24, 28, 36);
    private static final Color BG_CARD = new Color(34, 39, 50);
    private static final Color BG_INPUT = new Color(45, 51, 64);
    private static final Color ACCENT_ORANGE = new Color(245, 145, 65);
    private static final Color TEXT_PRIMARY = new Color(240, 243, 248);
    private static final Color TEXT_MUTED = new Color(165, 174, 190);
    private static final Color BORDER_COLOR = new Color(62, 70, 88);

    private JTextField txtBuscarId;
    private JComboBox<String> cmbFiltroMesa;
    private JComboBox<String> cmbFiltroEstado;
    private JTable tblPedidos;
    private DefaultTableModel modeloTabla;

    private JLabel lblPedidoSeleccionado;
    private JLabel lblEstadoBadge;
    private JLabel lblModalidadMesa;
    private JLabel lblTotalPedido;
    private JProgressBar barraProgreso;
    private JTextArea txtDetalleOrden;
    private JComboBox<String> cmbMedioPago;
    private JLabel lblEstadoPago;

    public FormPedido() {
        setTitle("SmartOrder - Seguimiento de Pedidos en Tiempo Real");
        setSize(1080, 680);
        setMinimumSize(new Dimension(960, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();
        cargarFiltrosMesa();
        cargarPedidos();
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBackground(BG_MAIN);
        root.setBorder(new EmptyBorder(18, 20, 18, 20));
        setContentPane(root);

        // Encabezado
        JPanel header = new JPanel(new BorderLayout(12, 8));
        header.setBackground(BG_CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);
        JLabel lblTitulo = new JLabel("Seguimiento de Pedidos en Tiempo Real");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Consulta el estado de preparación, tiempo estimado y estado de pago de cada orden");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(TEXT_MUTED);
        titleBox.add(lblTitulo);
        titleBox.add(lblSub);

        JButton btnActualizar = crearBoton("Actualizar Estado", ACCENT_ORANGE, BG_MAIN);
        btnActualizar.addActionListener(e -> {
            cargarPedidos();
            FormAuditoriaLogs.registrarEvento("INFO", "Seguimiento", "Consulta manual de estado de pedidos.");
        });

        header.add(titleBox, BorderLayout.CENTER);
        header.add(btnActualizar, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // Panel central dividido: izquierda tabla/filtros, derecha detalle + barra de progreso
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        centerPanel.setOpaque(false);

        // --- IZQUIERDA: Filtros + Tabla ---
        JPanel leftCard = new JPanel(new BorderLayout(10, 12));
        leftCard.setBackground(BG_CARD);
        leftCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterPanel.setOpaque(false);

        txtBuscarId = new JTextField(7);
        estilizarCampoTexto(txtBuscarId);

        cmbFiltroMesa = new JComboBox<>();
        estilizarCombo(cmbFiltroMesa);

        cmbFiltroEstado = new JComboBox<>(new String[]{
            "Todos", "Recibido", "En preparación", "Listo", "Entregado", "Cancelado"
        });
        estilizarCombo(cmbFiltroEstado);

        JButton btnFiltrar = crearBoton("Filtrar", BG_INPUT, TEXT_PRIMARY);
        btnFiltrar.addActionListener(e -> cargarPedidos());

        filterPanel.add(crearEtiqueta("ID:"));
        filterPanel.add(txtBuscarId);
        filterPanel.add(crearEtiqueta("Mesa:"));
        filterPanel.add(cmbFiltroMesa);
        filterPanel.add(crearEtiqueta("Estado:"));
        filterPanel.add(cmbFiltroEstado);
        filterPanel.add(btnFiltrar);

        String[] columnas = {"ID Pedido", "Ubicación", "Modalidad", "Estado", "Total"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblPedidos = new JTable(modeloTabla);
        tblPedidos.setBackground(BG_INPUT);
        tblPedidos.setForeground(TEXT_PRIMARY);
        tblPedidos.setSelectionBackground(ACCENT_ORANGE);
        tblPedidos.setSelectionForeground(BG_MAIN);
        tblPedidos.setRowHeight(28);
        tblPedidos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblPedidos.getTableHeader().setBackground(BG_MAIN);
        tblPedidos.getTableHeader().setForeground(ACCENT_ORANGE);
        tblPedidos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblPedidos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblPedidos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarDetalleSeleccionado();
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tblPedidos);
        scrollTabla.getViewport().setBackground(BG_INPUT);
        scrollTabla.setBorder(new LineBorder(BORDER_COLOR, 1));

        leftCard.add(filterPanel, BorderLayout.NORTH);
        leftCard.add(scrollTabla, BorderLayout.CENTER);

        // --- DERECHA: Progreso visual, detalle y pago simulado ---
        JPanel rightCard = new JPanel(new BorderLayout(10, 12));
        rightCard.setBackground(BG_CARD);
        rightCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JPanel infoTop = new JPanel(new GridLayout(4, 1, 0, 6));
        infoTop.setOpaque(false);

        lblPedidoSeleccionado = new JLabel("Selecciona un pedido de la lista");
        lblPedidoSeleccionado.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblPedidoSeleccionado.setForeground(TEXT_PRIMARY);

        lblEstadoBadge = new JLabel("Estado actual: --");
        lblEstadoBadge.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblEstadoBadge.setForeground(ACCENT_ORANGE);

        lblModalidadMesa = new JLabel("Mesa / Modalidad: --");
        lblModalidadMesa.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblModalidadMesa.setForeground(TEXT_MUTED);

        barraProgreso = new JProgressBar(0, 100);
        barraProgreso.setValue(0);
        barraProgreso.setStringPainted(true);
        barraProgreso.setString("Sin selección");
        barraProgreso.setForeground(ACCENT_ORANGE);
        barraProgreso.setBackground(BG_INPUT);
        barraProgreso.setPreferredSize(new Dimension(100, 24));

        infoTop.add(lblPedidoSeleccionado);
        infoTop.add(lblEstadoBadge);
        infoTop.add(lblModalidadMesa);
        infoTop.add(barraProgreso);

        txtDetalleOrden = new JTextArea();
        txtDetalleOrden.setEditable(false);
        txtDetalleOrden.setBackground(BG_INPUT);
        txtDetalleOrden.setForeground(TEXT_PRIMARY);
        txtDetalleOrden.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtDetalleOrden.setBorder(new EmptyBorder(10, 10, 10, 10));
        txtDetalleOrden.setText("Aquí se mostrarán los productos, cantidades y observaciones del pedido.");

        JScrollPane scrollDetalle = new JScrollPane(txtDetalleOrden);
        scrollDetalle.setBorder(new LineBorder(BORDER_COLOR, 1));

        // Panel inferior de Pago Simulado (Requerimiento Sec. 5)
        JPanel pagoPanel = new JPanel(new GridLayout(3, 1, 6, 6));
        pagoPanel.setBackground(BG_INPUT);
        pagoPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        lblTotalPedido = new JLabel("Total a pagar: ₡0.00");
        lblTotalPedido.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalPedido.setForeground(TEXT_PRIMARY);

        JPanel pagoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pagoRow.setOpaque(false);
        cmbMedioPago = new JComboBox<>(new String[]{"Tarjeta", "SINPE Móvil", "Efectivo"});
        estilizarCombo(cmbMedioPago);

        JButton btnRegistrarPago = crearBoton("Simular Pago", ACCENT_ORANGE, BG_MAIN);
        btnRegistrarPago.addActionListener(e -> registrarPagoSimulado());

        pagoRow.add(crearEtiqueta("Medio:"));
        pagoRow.add(cmbMedioPago);
        pagoRow.add(btnRegistrarPago);

        lblEstadoPago = new JLabel("Estado de pago: Pendiente");
        lblEstadoPago.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblEstadoPago.setForeground(TEXT_MUTED);

        pagoPanel.add(lblTotalPedido);
        pagoPanel.add(pagoRow);
        pagoPanel.add(lblEstadoPago);

        rightCard.add(infoTop, BorderLayout.NORTH);
        rightCard.add(scrollDetalle, BorderLayout.CENTER);
        rightCard.add(pagoPanel, BorderLayout.SOUTH);

        centerPanel.add(leftCard);
        centerPanel.add(rightCard);
        root.add(centerPanel, BorderLayout.CENTER);
    }

    private void cargarFiltrosMesa() {
        cmbFiltroMesa.removeAllItems();
        cmbFiltroMesa.addItem("Todas");
        cmbFiltroMesa.addItem("Para llevar / Barra");
        List<Mesa> mesas = FormAdmin.getMesas();
        if (mesas != null) {
            for (Mesa m : mesas) {
                cmbFiltroMesa.addItem("Mesa #" + m.getNumeroMesa());
            }
        }
    }

    private List<Pedido> obtenerListaPedidos() {
        try {
            List<Pedido> desdeBD = new PedidoDAO().listarTodos();
            if (desdeBD != null && !desdeBD.isEmpty()) {
                return desdeBD;
            }
        } catch (DAOException ex) {
            // Si no hay BD activa, usa la lista compartida en memoria
        }
        return GestorPedidos.getPedidos();
    }

    private Pedido buscarPedidoPorId(int id) {
        List<Pedido> pedidos = obtenerListaPedidos();
        if (pedidos == null) {
            return null;
        }
        for (Pedido p : pedidos) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    private void cargarPedidos() {
        modeloTabla.setRowCount(0);
        List<Pedido> pedidos = obtenerListaPedidos();
        if (pedidos == null) {
            return;
        }

        String textoId = (txtBuscarId != null) ? txtBuscarId.getText().trim() : "";
        String filtroMesa = (cmbFiltroMesa != null && cmbFiltroMesa.getSelectedItem() != null)
                ? cmbFiltroMesa.getSelectedItem().toString() : "Todas";
        String filtroEstado = (cmbFiltroEstado != null && cmbFiltroEstado.getSelectedItem() != null)
                ? cmbFiltroEstado.getSelectedItem().toString() : "Todos";

        for (Pedido p : pedidos) {
            if (!textoId.isEmpty() && !String.valueOf(p.getId()).equals(textoId)) {
                continue;
            }

            String ubicacion = (p.getMesa() != null)
                    ? "Mesa #" + p.getMesa().getNumeroMesa()
                    : "Para llevar / Barra";

            if (!"Todas".equals(filtroMesa) && !ubicacion.equalsIgnoreCase(filtroMesa)) {
                continue;
            }

            String estado = (p.getEstado() != null && !p.getEstado().isBlank())
                    ? p.getEstado() : "Recibido";

            if (!"Todos".equals(filtroEstado) && !estado.equalsIgnoreCase(filtroEstado)) {
                continue;
            }

            String modalidad = (p.getTipoPedido() != null && !p.getTipoPedido().isBlank())
                    ? p.getTipoPedido()
                    : (p.getMesa() != null ? "Local" : "Para llevar");

            double total = (p.getTotal() > 0) ? p.getTotal() : calcularTotal(p);

            modeloTabla.addRow(new Object[]{
                p.getId(),
                ubicacion,
                modalidad,
                estado,
                String.format("₡%.2f", total)
            });
        }
    }

    private void mostrarDetalleSeleccionado() {
        int fila = tblPedidos.getSelectedRow();
        if (fila < 0) {
            return;
        }

        int idPedido = Integer.parseInt(modeloTabla.getValueAt(fila, 0).toString());
        Pedido seleccionado = buscarPedidoPorId(idPedido);
        if (seleccionado == null) {
            return;
        }

        String estado = (seleccionado.getEstado() != null) ? seleccionado.getEstado() : "Recibido";
        String ubicacion = (seleccionado.getMesa() != null)
                ? "Mesa #" + seleccionado.getMesa().getNumeroMesa() + " (Consumo en local)"
                : "Barra / Para llevar";

        lblPedidoSeleccionado.setText("Orden #" + seleccionado.getId());
        lblEstadoBadge.setText("Estado actual: " + estado.toUpperCase());
        lblModalidadMesa.setText("Ubicación: " + ubicacion);
        actualizarBarraProgreso(estado);

        StringBuilder sb = new StringBuilder();
        sb.append("DETALLE DE PRODUCTOS\n");
        sb.append("--------------------------------------------------\n");
        if (seleccionado.getDetalles() != null && !seleccionado.getDetalles().isEmpty()) {
            for (DetallePedido d : seleccionado.getDetalles()) {
                String nombreProd = (d.getProducto() != null) ? d.getProducto().getNombre() : "Producto";
                sb.append(String.format("• %dx %s  (₡%.2f)\n",
                        d.getCantidad(), nombreProd, d.getSubtotal()));
            }
        } else {
            sb.append("Sin líneas de detalle registradas.\n");
        }
        sb.append("--------------------------------------------------\n");
        txtDetalleOrden.setText(sb.toString());

        double total = (seleccionado.getTotal() > 0) ? seleccionado.getTotal() : calcularTotal(seleccionado);
        lblTotalPedido.setText(String.format("Total a pagar: ₡%.2f", total));
        lblEstadoPago.setText("Estado de pago: Pendiente de confirmación");
    }

    private void actualizarBarraProgreso(String estado) {
        if ("Recibido".equalsIgnoreCase(estado)) {
            barraProgreso.setValue(25);
            barraProgreso.setForeground(ACCENT_ORANGE);
            barraProgreso.setString("25% - Recibido en cocina");
        } else if ("En preparación".equalsIgnoreCase(estado)) {
            barraProgreso.setValue(60);
            barraProgreso.setForeground(ACCENT_ORANGE);
            barraProgreso.setString("60% - En preparación");
        } else if ("Listo".equalsIgnoreCase(estado)) {
            barraProgreso.setValue(90);
            barraProgreso.setForeground(new Color(88, 196, 130));
            barraProgreso.setString("90% - Listo para entregar");
        } else if ("Entregado".equalsIgnoreCase(estado)) {
            barraProgreso.setValue(100);
            barraProgreso.setForeground(new Color(88, 196, 130));
            barraProgreso.setString("100% - Entregado");
        } else {
            barraProgreso.setValue(100);
            barraProgreso.setForeground(new Color(220, 85, 85));
            barraProgreso.setString("Pedido Cancelado");
        }
    }

    private void registrarPagoSimulado() {
        int fila = tblPedidos.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona primero un pedido de la tabla.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String idPedido = modeloTabla.getValueAt(fila, 0).toString();
        String medio = (String) cmbMedioPago.getSelectedItem();
        lblEstadoPago.setText("Estado de pago: PAGADO vía " + medio + " (Simulado)");
        lblEstadoPago.setForeground(new Color(88, 196, 130));

        FormAuditoriaLogs.registrarEvento("PAGO", "Caja/Cliente",
                "Pago simulado registrado para Pedido #" + idPedido + " mediante " + medio);
        JOptionPane.showMessageDialog(this,
                "Pago simulado registrado con éxito para el Pedido #" + idPedido + " (" + medio + ").",
                "Pago Confirmado", JOptionPane.INFORMATION_MESSAGE);
    }

    private double calcularTotal(Pedido p) {
        double suma = 0.0;
        if (p.getDetalles() != null) {
            for (DetallePedido d : p.getDetalles()) {
                suma += d.getSubtotal();
            }
        }
        return suma;
    }

    private JLabel crearEtiqueta(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(TEXT_MUTED);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return lbl;
    }

    private void estilizarCampoTexto(JTextField txt) {
        txt.setBackground(BG_INPUT);
        txt.setForeground(TEXT_PRIMARY);
        txt.setCaretColor(TEXT_PRIMARY);
        txt.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(4, 8, 4, 8)
        ));
    }

    private void estilizarCombo(JComboBox<String> cmb) {
        cmb.setBackground(BG_INPUT);
        cmb.setForeground(TEXT_PRIMARY);
        cmb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }

    private JButton crearBoton(String texto, Color bg, Color fg) {
        JButton btn = new JButton(texto);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FormPedido().setVisible(true));
    }
}