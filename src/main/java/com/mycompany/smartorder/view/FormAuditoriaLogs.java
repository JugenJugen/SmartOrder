/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartorder.view;

/**
 *
 * @author angel
 */
import java.awt.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

public class FormAuditoriaLogs extends JFrame {

    private static final Color BG_MAIN = new Color(24, 28, 36);
    private static final Color BG_CARD = new Color(34, 39, 50);
    private static final Color BG_INPUT = new Color(45, 51, 64);
    private static final Color ACCENT_ORANGE = new Color(245, 145, 65);
    private static final Color TEXT_PRIMARY = new Color(240, 243, 248);
    private static final Color TEXT_MUTED = new Color(165, 174, 190);
    private static final Color BORDER_COLOR = new Color(62, 70, 88);

    private static final String ARCHIVO_LOG = "smartorder_auditoria.log";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final List<String[]> BITACORA_MEMORIA = Collections.synchronizedList(new ArrayList<>());

    private JComboBox<String> cmbNivel;
    private JTextField txtBusqueda;
    private JTable tblLogs;
    private DefaultTableModel modeloLogs;
    private JLabel lblTotalEventos;

    public FormAuditoriaLogs() {
        setTitle("SmartOrder - Auditoría, Trazabilidad y Logs del Sistema");
        setSize(1020, 620);
        setMinimumSize(new Dimension(900, 540));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();
        cargarDesdeArchivoSiExiste();
        if (BITACORA_MEMORIA.isEmpty()) {
            registrarEvento("INFO", "Sistema", "Módulo de auditoría inicializado correctamente.");
        }
        refrescarTabla();
    }

    public static synchronized void registrarEvento(String nivel, String modulo, String detalle) {
        String fechaHora = LocalDateTime.now().format(FORMATO_FECHA);
        String[] fila = new String[]{fechaHora, nivel.toUpperCase(), modulo, detalle};
        BITACORA_MEMORIA.add(fila);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO_LOG, true))) {
            bw.write(fechaHora + " | " + nivel.toUpperCase() + " | " + modulo + " | " + detalle);
            bw.newLine();
        } catch (IOException ex) {
            System.err.println("No se pudo escribir en el archivo de log: " + ex.getMessage());
        }
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBackground(BG_MAIN);
        root.setBorder(new EmptyBorder(18, 20, 18, 20));
        setContentPane(root);

        // Header
        JPanel header = new JPanel(new BorderLayout(12, 8));
        header.setBackground(BG_CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);
        JLabel lblTitulo = new JLabel("Auditoría y Trazabilidad del Sistema");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Bitácora de accesos, cambios de estado de pedidos, conexiones de sockets y eventos críticos");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(TEXT_MUTED);
        titleBox.add(lblTitulo);
        titleBox.add(lblSub);

        JPanel headerBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerBtns.setOpaque(false);

        JButton btnExportar = crearBoton("Exportar Log (.txt)", BG_INPUT, TEXT_PRIMARY);
        btnExportar.addActionListener(e -> exportarLog());

        JButton btnRefrescar = crearBoton("Refrescar", ACCENT_ORANGE, BG_MAIN);
        btnRefrescar.addActionListener(e -> refrescarTabla());

        headerBtns.add(btnExportar);
        headerBtns.add(btnRefrescar);

        header.add(titleBox, BorderLayout.CENTER);
        header.add(headerBtns, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // Panel central con barra de filtros y tabla
        JPanel bodyCard = new JPanel(new BorderLayout(10, 12));
        bodyCard.setBackground(BG_CARD);
        bodyCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JPanel topFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        topFilters.setOpaque(false);

        cmbNivel = new JComboBox<>(new String[]{"Todos", "INFO", "PEDIDO", "COCINA", "PAGO", "ERROR"});
        cmbNivel.setBackground(BG_INPUT);
        cmbNivel.setForeground(TEXT_PRIMARY);
        cmbNivel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        txtBusqueda = new JTextField(22);
        txtBusqueda.setBackground(BG_INPUT);
        txtBusqueda.setForeground(TEXT_PRIMARY);
        txtBusqueda.setCaretColor(TEXT_PRIMARY);
        txtBusqueda.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(5, 8, 5, 8)
        ));

        JButton btnBuscar = crearBoton("Aplicar Filtro", ACCENT_ORANGE, BG_MAIN);
        btnBuscar.addActionListener(e -> refrescarTabla());

        JLabel lblNivel = new JLabel("Tipo de Evento:");
        lblNivel.setForeground(TEXT_MUTED);
        JLabel lblBuscar = new JLabel("Buscar texto:");
        lblBuscar.setForeground(TEXT_MUTED);

        topFilters.add(lblNivel);
        topFilters.add(cmbNivel);
        topFilters.add(lblBuscar);
        topFilters.add(txtBusqueda);
        topFilters.add(btnBuscar);

        String[] columnas = {"Fecha y Hora", "Tipo", "Módulo / Origen", "Descripción del Evento"};
        modeloLogs = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblLogs = new JTable(modeloLogs);
        tblLogs.setBackground(BG_INPUT);
        tblLogs.setForeground(TEXT_PRIMARY);
        tblLogs.setSelectionBackground(ACCENT_ORANGE);
        tblLogs.setSelectionForeground(BG_MAIN);
        tblLogs.setRowHeight(26);
        tblLogs.setFont(new Font("Consolas", Font.PLAIN, 12));
        tblLogs.getTableHeader().setBackground(BG_MAIN);
        tblLogs.getTableHeader().setForeground(ACCENT_ORANGE);
        tblLogs.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        tblLogs.getColumnModel().getColumn(0).setPreferredWidth(155);
        tblLogs.getColumnModel().getColumn(1).setPreferredWidth(90);
        tblLogs.getColumnModel().getColumn(2).setPreferredWidth(140);
        tblLogs.getColumnModel().getColumn(3).setPreferredWidth(540);

        JScrollPane scroll = new JScrollPane(tblLogs);
        scroll.getViewport().setBackground(BG_INPUT);
        scroll.setBorder(new LineBorder(BORDER_COLOR, 1));

        lblTotalEventos = new JLabel("Registros mostrados: 0");
        lblTotalEventos.setForeground(TEXT_MUTED);
        lblTotalEventos.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        bodyCard.add(topFilters, BorderLayout.NORTH);
        bodyCard.add(scroll, BorderLayout.CENTER);
        bodyCard.add(lblTotalEventos, BorderLayout.SOUTH);

        root.add(bodyCard, BorderLayout.CENTER);
    }

    private void cargarDesdeArchivoSiExiste() {
        File archivo = new File(ARCHIVO_LOG);
        if (!archivo.exists() || !BITACORA_MEMORIA.isEmpty()) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split("\\s*\\|\\s*", 4);
                if (partes.length == 4) {
                    BITACORA_MEMORIA.add(partes);
                }
            }
        } catch (IOException ex) {
            System.err.println("Error al leer el archivo de auditoría: " + ex.getMessage());
        }
    }

    private void refrescarTabla() {
        modeloLogs.setRowCount(0);
        String nivelFiltro = (String) cmbNivel.getSelectedItem();
        String textoFiltro = txtBusqueda.getText().trim().toLowerCase();

        synchronized (BITACORA_MEMORIA) {
            for (int i = BITACORA_MEMORIA.size() - 1; i >= 0; i--) {
                String[] reg = BITACORA_MEMORIA.get(i);
                if (nivelFiltro != null && !"Todos".equals(nivelFiltro) && !reg[1].equalsIgnoreCase(nivelFiltro)) {
                    continue;
                }
                if (!textoFiltro.isEmpty()) {
                    String unido = (reg[0] + " " + reg[1] + " " + reg[2] + " " + reg[3]).toLowerCase();
                    if (!unido.contains(textoFiltro)) {
                        continue;
                    }
                }
                modeloLogs.addRow(reg);
            }
        }
        lblTotalEventos.setText("Registros mostrados: " + modeloLogs.getRowCount()
                + "  |  Archivo local: " + ARCHIVO_LOG);
    }

    private void exportarLog() {
        File archivoDestino = new File("auditoria_smartorder.txt");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivoDestino))) {
            synchronized (BITACORA_MEMORIA) {
                for (String[] reg : BITACORA_MEMORIA) {
                    bw.write(reg[0] + " | " + reg[1] + " | " + reg[2] + " | " + reg[3]);
                    bw.newLine();
                }
            }
            JOptionPane.showMessageDialog(this,
                    "Archivo guardado");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al guardar el TXT: " + ex.getMessage());
        }
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
        SwingUtilities.invokeLater(() -> new FormAuditoriaLogs().setVisible(true));
    }
}
