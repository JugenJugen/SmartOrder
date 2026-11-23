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
import com.mycompany.smartorder.model.Categoria;
import com.mycompany.smartorder.model.Mesa;
import com.mycompany.smartorder.model.Pedido;
import com.mycompany.smartorder.model.Producto;
import com.mycompany.smartorder.model.Usuario;
import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;

public class FormAdmin extends JFrame implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Color COLOR_FONDO = new Color(245, 247, 250);
    private static final Color COLOR_BLANCO = Color.WHITE;
    private static final Color COLOR_NARANJA = new Color(230, 126, 34);
    private static final Color COLOR_OSCURO = new Color(33, 43, 54);
    private static final Color COLOR_SELECCION = new Color(232, 237, 243);
    private static final Color COLOR_BORDE = new Color(210, 216, 224);

    private CardLayout cardLayout;
    private JPanel panelCards;

    private JButton btnCatBebidas, btnCatEntradas, btnCatPlatos, btnCatPostres;
    private JButton btnSeccionEmpleados, btnSeccionMesas;
    private String categoriaActual = "Entradas";

    private DefaultTableModel modeloProductos;
    private JTable tablaProductos;
    private JLabel lblTituloSeccionProd;

    private Producto productoEditando = null;
    private JLabel lblTituloFormProd;
    private JTextField txtFormNombreProd;
    private JTextArea txtFormDescProd;
    private JTextField txtFormPrecioProd;
    private JComboBox<String> cmbFormCategoriaProd;
    private JButton btnGuardarFormProd;

    private DefaultTableModel modeloEmpleados;
    private JTable tablaEmpleados;

    private DefaultTableModel modeloMesas;
    private JTable tablaMesas;

    private static final List<Producto> LISTA_PRODUCTOS = new ArrayList<>();
    private static final List<Usuario> LISTA_EMPLEADOS = new ArrayList<>();
    private static final List<Mesa> LISTA_MESAS = new ArrayList<>();

    static {
        cargarDatosIniciales();
    }

    public FormAdmin() {
        setTitle("SmartOrder - Administración");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setJMenuBar(crearBarraNavegacion());
        getContentPane().setBackground(COLOR_FONDO);

        add(crearPanelLateral(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        panelCards = new JPanel(cardLayout);
        panelCards.setBackground(COLOR_FONDO);

        panelCards.add(crearPanelProductos(), "PRODUCTOS");
        panelCards.add(crearPanelFormProducto(), "FORM_PRODUCTO");
        panelCards.add(crearPanelEmpleados(), "EMPLEADOS");
        panelCards.add(crearPanelMesas(), "MESAS");

        add(panelCards, BorderLayout.CENTER);

        mostrarCategoriaProductos("Entradas");
        cargarTablaEmpleados();
        cargarTablaMesas();
    }

    public static List<Producto> getProductos() {
        return LISTA_PRODUCTOS;
    }

    public static List<Usuario> getEmpleados() {
        return LISTA_EMPLEADOS;
    }

    public static List<Mesa> getMesas() {
        return LISTA_MESAS;
    }

    private static void cargarDatosIniciales() {
        if (LISTA_PRODUCTOS.isEmpty()) {
            Categoria bebidas = new Categoria(1, "Bebidas");
            Categoria entradas = new Categoria(2, "Entradas");
            Categoria platos = new Categoria(3, "Platos Fuertes");
            Categoria postres = new Categoria(4, "Postres");

            LISTA_PRODUCTOS.add(crearProducto(1, "Limonada Natural", "Bebida refrescante de limón natural", 1500.0, true, 25, bebidas));
            LISTA_PRODUCTOS.add(crearProducto(2, "Té Frío", "Té frío natural de durazno o limón", 1500.0, true, 20, bebidas));

            LISTA_PRODUCTOS.add(crearProducto(3, "Bruschettas de tomate y albahaca", "Pan tostado con cubos de tomate fresco, albahaca picada, aceite de oliva y sal", 4000.0, true, 15, entradas));
            LISTA_PRODUCTOS.add(crearProducto(4, "Dedos de Queso", "Queso gouda empanizado acompañado de salsa marinara", 3500.0, true, 18, entradas));

            LISTA_PRODUCTOS.add(crearProducto(5, "Pasta Carbonara", "Pasta con salsa cremosa, tocino crujiente y parmesano", 6500.0, true, 12, platos));
            LISTA_PRODUCTOS.add(crearProducto(6, "Lomo Saltado", "Trocitos de lomo salteados con cebolla y tomate", 7500.0, true, 10, platos));

            LISTA_PRODUCTOS.add(crearProducto(7, "Tres Leches", "Tradicional pastel esponjoso bañado en tres leches", 3000.0, true, 14, postres));
            LISTA_PRODUCTOS.add(crearProducto(8, "Flan de Caramelo", "Flan casero con salsa dulce de caramelo", 2800.0, true, 16, postres));
        }

        if (LISTA_EMPLEADOS.isEmpty()) {
            LISTA_EMPLEADOS.add(new Usuario(123456789, "María Salas", "maria@smartorder.com", "1234", "Atención", true));
            LISTA_EMPLEADOS.add(new Usuario(145662098, "Mariela Fuentes", "mariela@smartorder.com", "1234", "Cocina", true));
            LISTA_EMPLEADOS.add(new Usuario(147996223, "Alejandro Gutiérrez", "alejandro@smartorder.com", "1234", "Cocina", true));
        }

        if (LISTA_MESAS.isEmpty()) {
            LISTA_MESAS.add(new Mesa(1, 4, "Disponible"));
            LISTA_MESAS.add(new Mesa(2, 8, "Disponible"));
            LISTA_MESAS.add(new Mesa(3, 6, "Disponible"));
        }
    }

    private static Producto crearProducto(int id, String nombre, String desc, double precio, boolean disp, int stock, Categoria cat) {
        Producto p = new Producto(nombre, precio);
        p.setId(id);
        p.setDescripcion(desc);
        p.setDisponibilidad(disp);
        p.setStock(stock);
        p.setCategoria(cat);
        return p;
    }

    private static int generarSiguienteIdProducto() {
        int maxId = 0;
        for (Producto p : LISTA_PRODUCTOS) {
            if (p.getId() > maxId) {
                maxId = p.getId();
            }
        }
        return maxId + 1;
    }

    private JMenuBar crearBarraNavegacion() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menuNavegacion = new JMenu("Cambiar Vista");

        JMenuItem itemCliente = new JMenuItem("Vista Cliente (Pedidos)");
        JMenuItem itemCocina = new JMenuItem("Vista Cocina");
        JMenuItem itemSeguimiento = new JMenuItem("Seguimiento de Pedidos");
        JMenuItem itemAuditoria = new JMenuItem("Auditoría y Logs");

        itemCliente.addActionListener(e -> {
            FormClientePedido cliente = new FormClientePedido();
            cliente.setVisible(true);
            cliente.toFront();
            dispose();
        });

        itemCocina.addActionListener(e -> {
            FormCocina cocina = new FormCocina();
            cocina.cargarPedidosEnTablas();
            cocina.setVisible(true);
            cocina.toFront();
            dispose();
        });

        itemSeguimiento.addActionListener(e -> {
            FormPedido seg = new FormPedido();
            seg.setVisible(true);
            seg.toFront();
        });

        itemAuditoria.addActionListener(e -> {
            FormAuditoriaLogs aud = new FormAuditoriaLogs();
            aud.setVisible(true);
            aud.toFront();
        });

        menuNavegacion.add(itemCliente);
        menuNavegacion.add(itemCocina);
        menuNavegacion.add(itemSeguimiento);
        menuNavegacion.add(itemAuditoria);
        menuBar.add(menuNavegacion);

        FormAuditoriaLogs.registrarEvento("INFO", "Admin", "Barra de navegación de administración inicializada.");
        return menuBar;
    }

    private JPanel crearPanelLateral() {
        JPanel lateral = new JPanel();
        lateral.setPreferredSize(new Dimension(210, 0));
        lateral.setBackground(COLOR_BLANCO);
        lateral.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(225, 230, 236)));
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));

        lateral.add(crearEtiquetaGrupo("Productos"));
        btnCatBebidas = crearBotonMenu("Bebidas");
        btnCatEntradas = crearBotonMenu("Entradas");
        btnCatPlatos = crearBotonMenu("Platos Fuertes");
        btnCatPostres = crearBotonMenu("Postres");

        btnCatBebidas.addActionListener(e -> mostrarCategoriaProductos("Bebidas"));
        btnCatEntradas.addActionListener(e -> mostrarCategoriaProductos("Entradas"));
        btnCatPlatos.addActionListener(e -> mostrarCategoriaProductos("Platos Fuertes"));
        btnCatPostres.addActionListener(e -> mostrarCategoriaProductos("Postres"));

        lateral.add(btnCatBebidas);
        lateral.add(btnCatEntradas);
        lateral.add(btnCatPlatos);
        lateral.add(btnCatPostres);

        lateral.add(Box.createVerticalStrut(16));
        lateral.add(crearEtiquetaGrupo("Usuarios"));
        btnSeccionEmpleados = crearBotonMenu("Empleados");
        btnSeccionEmpleados.addActionListener(e -> mostrarSeccionEmpleados());
        lateral.add(btnSeccionEmpleados);

        lateral.add(Box.createVerticalStrut(16));
        lateral.add(crearEtiquetaGrupo("Mesas"));
        btnSeccionMesas = crearBotonMenu("Gestión de Mesas");
        btnSeccionMesas.addActionListener(e -> mostrarSeccionMesas());
        lateral.add(btnSeccionMesas);

        return lateral;
    }

    private JLabel crearEtiquetaGrupo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        lbl.setForeground(COLOR_OSCURO);
        lbl.setBorder(new EmptyBorder(10, 16, 6, 10));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JButton crearBotonMenu(String texto) {
        JButton btn = new JButton(texto);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setPreferredSize(new Dimension(190, 38));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(new Font("Arial", Font.PLAIN, 13));
        btn.setBackground(COLOR_BLANCO);
        btn.setForeground(COLOR_OSCURO);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setBorder(new EmptyBorder(0, 20, 0, 10));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        return btn;
    }

    private void resaltarBotonLateral(JButton activo) {
        JButton[] todos = {btnCatBebidas, btnCatEntradas, btnCatPlatos, btnCatPostres, btnSeccionEmpleados, btnSeccionMesas};
        for (JButton b : todos) {
            b.setBackground(COLOR_BLANCO);
            b.setFont(new Font("Arial", Font.PLAIN, 13));
        }
        activo.setBackground(COLOR_SELECCION);
        activo.setFont(new Font("Arial", Font.BOLD, 13));
    }

    private void mostrarCategoriaProductos(String categoria) {
        this.categoriaActual = categoria;
        if (lblTituloSeccionProd != null) {
            lblTituloSeccionProd.setText("Productos - " + categoria);
        }

        if ("Bebidas".equals(categoria)) {
            resaltarBotonLateral(btnCatBebidas);
        } else if ("Entradas".equals(categoria)) {
            resaltarBotonLateral(btnCatEntradas);
        } else if ("Platos Fuertes".equals(categoria)) {
            resaltarBotonLateral(btnCatPlatos);
        } else if ("Postres".equals(categoria)) {
            resaltarBotonLateral(btnCatPostres);
        }

        cargarTablaProductos();
        cardLayout.show(panelCards, "PRODUCTOS");
    }

    private void mostrarSeccionEmpleados() {
        resaltarBotonLateral(btnSeccionEmpleados);
        cargarTablaEmpleados();
        cardLayout.show(panelCards, "EMPLEADOS");
    }

    private void mostrarSeccionMesas() {
        resaltarBotonLateral(btnSeccionMesas);
        cargarTablaMesas();
        cardLayout.show(panelCards, "MESAS");
    }

    private JPanel crearPanelProductos() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));

        lblTituloSeccionProd = new JLabel("Productos");
        lblTituloSeccionProd.setFont(new Font("Arial", Font.BOLD, 24));
        lblTituloSeccionProd.setForeground(COLOR_OSCURO);
        panel.add(lblTituloSeccionProd, BorderLayout.NORTH);

        String[] columnas = {"Nombre", "Descripción", "Precio"};
        modeloProductos = new DefaultTableModel(new Object[][]{}, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaProductos = new JTable(modeloProductos);
        estilizarTabla(tablaProductos);

        tablaProductos.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                int fila = tablaProductos.getSelectedRow();
                if (fila != -1) {
                    String nombreProd = modeloProductos.getValueAt(fila, 0).toString();
                    productoEditando = buscarProductoPorNombreYCategoria(nombreProd, categoriaActual);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tablaProductos);
        scroll.getViewport().setBackground(COLOR_BLANCO);
        panel.add(scroll, BorderLayout.CENTER);

        JButton btnAgregar = crearBotonNaranja("+ Agregar Producto");
        btnAgregar.addActionListener(e -> abrirFormProducto(null));

        JButton btnEditar = new JButton("Editar Seleccionado");
        btnEditar.addActionListener(e -> {
            int fila = tablaProductos.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione un producto de la tabla para editar.");
                return;
            }
            String nombreProd = modeloProductos.getValueAt(fila, 0).toString();
            Producto prod = buscarProductoPorNombreYCategoria(nombreProd, categoriaActual);
            if (prod != null) {
                abrirFormProducto(prod);
            }
        });

        JButton btnEliminar = new JButton("Eliminar Seleccionado");
        btnEliminar.addActionListener(e -> {
            int fila = tablaProductos.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione un producto de la tabla para eliminar.");
                return;
            }
            String nombreProd = modeloProductos.getValueAt(fila, 0).toString();
            Producto prod = buscarProductoPorNombreYCategoria(nombreProd, categoriaActual);
            if (prod != null) {
                LISTA_PRODUCTOS.remove(prod);
                cargarTablaProductos();
            }
        });

        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelSur.setBackground(COLOR_FONDO);
        panelSur.add(btnAgregar);
        panelSur.add(btnEditar);
        panelSur.add(btnEliminar);
        panel.add(panelSur, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelFormProducto() {
        JPanel panelFondoForm = new JPanel(new BorderLayout(0, 14));
        panelFondoForm.setBackground(COLOR_FONDO);
        panelFondoForm.setBorder(new EmptyBorder(24, 28, 24, 28));

        lblTituloFormProd = new JLabel("Agregar Producto");
        lblTituloFormProd.setFont(new Font("Arial", Font.BOLD, 24));
        lblTituloFormProd.setForeground(COLOR_OSCURO);
        panelFondoForm.add(lblTituloFormProd, BorderLayout.NORTH);

        JPanel contenedorAlineado = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 6));
        contenedorAlineado.setBackground(COLOR_FONDO);

        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(COLOR_BLANCO);
        tarjeta.setPreferredSize(new Dimension(410, 445));
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder(BorderFactory.createLineBorder(COLOR_BORDE, 1), "Datos del Producto"),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JLabel lblNombre = crearLabelCampo("Nombre");
        txtFormNombreProd = new JTextField();
        txtFormNombreProd.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txtFormNombreProd.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc = crearLabelCampo("Descripción");
        txtFormDescProd = new JTextArea(3, 25);
        txtFormDescProd.setFont(new Font("Arial", Font.PLAIN, 12));
        txtFormDescProd.setLineWrap(true);
        txtFormDescProd.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtFormDescProd);
        scrollDesc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        scrollDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblPrecio = crearLabelCampo("Precio");
        txtFormPrecioProd = new JTextField();
        txtFormPrecioProd.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txtFormPrecioProd.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblCat = crearLabelCampo("Categoría");
        cmbFormCategoriaProd = new JComboBox<>(new String[]{
            "Seleccione la categoría",
            "Bebidas",
            "Entradas",
            "Platos Fuertes",
            "Postres"
        });
        cmbFormCategoriaProd.setFont(new Font("Arial", Font.PLAIN, 12));
        cmbFormCategoriaProd.setBackground(COLOR_BLANCO);
        cmbFormCategoriaProd.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cmbFormCategoriaProd.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnGuardarFormProd = crearBotonNaranja("Agregar Producto");
        btnGuardarFormProd.setPreferredSize(new Dimension(160, 36));
        btnGuardarFormProd.addActionListener(e -> guardarProductoDesdeForm());

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Arial", Font.PLAIN, 12));
        btnCancelar.setFocusPainted(false);
        btnCancelar.setPreferredSize(new Dimension(100, 36));
        btnCancelar.addActionListener(e -> mostrarCategoriaProductos(categoriaActual));

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelBotones.setBackground(COLOR_BLANCO);
        panelBotones.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelBotones.add(btnGuardarFormProd);
        panelBotones.add(btnCancelar);

        tarjeta.add(lblNombre);
        tarjeta.add(Box.createVerticalStrut(4));
        tarjeta.add(txtFormNombreProd);
        tarjeta.add(Box.createVerticalStrut(12));

        tarjeta.add(lblDesc);
        tarjeta.add(Box.createVerticalStrut(4));
        tarjeta.add(scrollDesc);
        tarjeta.add(Box.createVerticalStrut(12));

        tarjeta.add(lblPrecio);
        tarjeta.add(Box.createVerticalStrut(4));
        tarjeta.add(txtFormPrecioProd);
        tarjeta.add(Box.createVerticalStrut(12));

        tarjeta.add(lblCat);
        tarjeta.add(Box.createVerticalStrut(4));
        tarjeta.add(cmbFormCategoriaProd);
        tarjeta.add(Box.createVerticalStrut(18));

        tarjeta.add(panelBotones);

        contenedorAlineado.add(tarjeta);
        panelFondoForm.add(contenedorAlineado, BorderLayout.CENTER);
        return panelFondoForm;
    }

    private JLabel crearLabelCampo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        lbl.setForeground(COLOR_OSCURO);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void abrirFormProducto(Producto existente) {
        this.productoEditando = existente;

        if (existente == null) {
            lblTituloFormProd.setText("Agregar Producto");
            btnGuardarFormProd.setText("Agregar Producto");
            txtFormNombreProd.setText("");
            txtFormDescProd.setText("");
            txtFormPrecioProd.setText("");
            cmbFormCategoriaProd.setSelectedItem(categoriaActual);
        } else {
            lblTituloFormProd.setText("Editar Producto");
            btnGuardarFormProd.setText("Guardar Cambios");
            txtFormNombreProd.setText(existente.getNombre());
            txtFormDescProd.setText(existente.getDescripcion() != null ? existente.getDescripcion() : "");
            txtFormPrecioProd.setText(String.valueOf(existente.getPrecio()));
            String catProd = (existente.getCategoria() != null) ? existente.getCategoria().getNombre() : categoriaActual;
            cmbFormCategoriaProd.setSelectedItem(catProd);
        }

        cardLayout.show(panelCards, "FORM_PRODUCTO");
    }

    private void guardarProductoDesdeForm() {
        String nombre = txtFormNombreProd.getText().trim();
        String descripcion = txtFormDescProd.getText().trim();
        String precioTexto = txtFormPrecioProd.getText().trim();
        String categoriaElegida = cmbFormCategoriaProd.getSelectedItem().toString();

        if ("Seleccione la categoría".equals(categoriaElegida)) {
            JOptionPane.showMessageDialog(this, "Seleccione una categoría para el producto.");
            return;
        }

        try {
            double precio = Double.parseDouble(precioTexto);
            Categoria catObj = new Categoria(0, categoriaElegida);

            if (productoEditando == null) {
                Producto nuevo = new Producto(nombre, precio);
                nuevo.setId(generarSiguienteIdProducto());
                nuevo.setDescripcion(descripcion);
                nuevo.setDisponibilidad(true);
                nuevo.setCategoria(catObj);
                nuevo.validar();
                LISTA_PRODUCTOS.add(nuevo);
            } else {
                productoEditando.setNombre(nombre);
                productoEditando.setDescripcion(descripcion);
                productoEditando.setPrecio(precio);
                productoEditando.setCategoria(catObj);
                productoEditando.validar();
            }

            productoEditando = null;
            mostrarCategoriaProductos(categoriaElegida);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese un precio numérico válido.");
        } catch (ValidacionException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void cargarTablaProductos() {
        if (modeloProductos == null) {
            return;
        }
        modeloProductos.setRowCount(0);

        List<Producto> filtrados = new ArrayList<>();
        for (Producto p : LISTA_PRODUCTOS) {
            String nombreCat = (p.getCategoria() != null) ? p.getCategoria().getNombre() : "";
            if (nombreCat.equalsIgnoreCase(categoriaActual)) {
                filtrados.add(p);
            }
        }

        Collections.sort(filtrados, Comparator.comparing(Producto::getNombre, String.CASE_INSENSITIVE_ORDER));

        for (Producto p : filtrados) {
            modeloProductos.addRow(new Object[]{
                p.getNombre(),
                p.getDescripcion() != null ? p.getDescripcion() : "",
                String.format("%.2f", p.getPrecio())
            });
        }
    }

    private Producto buscarProductoPorNombreYCategoria(String nombre, String categoria) {
        for (Producto p : LISTA_PRODUCTOS) {
            String cat = (p.getCategoria() != null) ? p.getCategoria().getNombre() : "";
            if (p.getNombre().equals(nombre) && cat.equalsIgnoreCase(categoria)) {
                return p;
            }
        }
        return null;
    }

    private JPanel crearPanelEmpleados() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel lblTitulo = new JLabel("Empleados");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(COLOR_OSCURO);
        panel.add(lblTitulo, BorderLayout.NORTH);

        String[] columnas = {"Identificación", "Nombre", "Puesto"};
        modeloEmpleados = new DefaultTableModel(new Object[][]{}, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaEmpleados = new JTable(modeloEmpleados);
        estilizarTabla(tablaEmpleados);

        JScrollPane scroll = new JScrollPane(tablaEmpleados);
        scroll.getViewport().setBackground(COLOR_BLANCO);
        panel.add(scroll, BorderLayout.CENTER);

        JButton btnAgregarEmp = crearBotonNaranja("+ Agregar Empleado");
        btnAgregarEmp.addActionListener(e -> {
            String idTexto = JOptionPane.showInputDialog(this, "Identificación:");
            String nombre = JOptionPane.showInputDialog(this, "Nombre:");
            String correo = JOptionPane.showInputDialog(this, "Correo:");
            String puesto = JOptionPane.showInputDialog(this, "Puesto (Atención / Cocina / Administrador):");
            if (idTexto != null && nombre != null && correo != null && puesto != null) {
                try {
                    int id = Integer.parseInt(idTexto.trim());
                    Usuario nuevo = new Usuario(id, nombre.trim(), correo.trim(), "1234", puesto.trim(), true);
                    nuevo.validar();
                    LISTA_EMPLEADOS.add(nuevo);
                    cargarTablaEmpleados();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "La identificación debe ser numérica.");
                } catch (ValidacionException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage());
                }
            }
        });

        JButton btnEliminarEmp = new JButton("Eliminar Seleccionado");
        btnEliminarEmp.addActionListener(e -> {
            int fila = tablaEmpleados.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione un empleado de la tabla para eliminar.");
                return;
            }
            int idEmp = Integer.parseInt(modeloEmpleados.getValueAt(fila, 0).toString());
            LISTA_EMPLEADOS.removeIf(u -> u.getId() == idEmp);
            cargarTablaEmpleados();
        });

        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelSur.setBackground(COLOR_FONDO);
        panelSur.add(btnAgregarEmp);
        panelSur.add(btnEliminarEmp);
        panel.add(panelSur, BorderLayout.SOUTH);

        return panel;
    }

    private void cargarTablaEmpleados() {
        if (modeloEmpleados == null) {
            return;
        }
        modeloEmpleados.setRowCount(0);

        for (Usuario u : LISTA_EMPLEADOS) {
            modeloEmpleados.addRow(new Object[]{
                u.getId(),
                u.getNombre(),
                u.getRol()
            });
        }
    }

    private JPanel crearPanelMesas() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel lblTitulo = new JLabel("Mesas");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(COLOR_OSCURO);
        panel.add(lblTitulo, BorderLayout.NORTH);

        String[] columnas = {"# de Mesa", "Capacidad"};
        modeloMesas = new DefaultTableModel(new Object[][]{}, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaMesas = new JTable(modeloMesas);
        estilizarTabla(tablaMesas);

        JScrollPane scroll = new JScrollPane(tablaMesas);
        scroll.getViewport().setBackground(COLOR_BLANCO);
        panel.add(scroll, BorderLayout.CENTER);

        JButton btnAgregarMesa = crearBotonNaranja("+ Agregar Mesa");
        btnAgregarMesa.addActionListener(e -> {
            String numTexto = JOptionPane.showInputDialog(this, "# de Mesa:", LISTA_MESAS.size() + 1);
            String capTexto = JOptionPane.showInputDialog(this, "Capacidad:", "4");
            if (numTexto != null && capTexto != null) {
                try {
                    int num = Integer.parseInt(numTexto.trim());
                    int cap = Integer.parseInt(capTexto.trim());
                    Mesa nuevaMesa = new Mesa(num, cap, "Disponible");
                    nuevaMesa.validar();
                    LISTA_MESAS.add(nuevaMesa);
                    cargarTablaMesas();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Ingrese números válidos.");
                } catch (ValidacionException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage());
                }
            }
        });

        JButton btnEliminarMesa = new JButton("Eliminar Seleccionada");
        btnEliminarMesa.addActionListener(e -> {
            int fila = tablaMesas.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione una mesa de la tabla para eliminar.");
                return;
            }
            int numMesa = Integer.parseInt(modeloMesas.getValueAt(fila, 0).toString());
            LISTA_MESAS.removeIf(m -> m.getNumeroMesa() == numMesa);
            cargarTablaMesas();
        });

        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelSur.setBackground(COLOR_FONDO);
        panelSur.add(btnAgregarMesa);
        panelSur.add(btnEliminarMesa);
        panel.add(panelSur, BorderLayout.SOUTH);

        return panel;
    }

    private void cargarTablaMesas() {
        if (modeloMesas == null) {
            return;
        }
        modeloMesas.setRowCount(0);

        for (Mesa m : LISTA_MESAS) {
            modeloMesas.addRow(new Object[]{
                m.getNumeroMesa(),
                m.getCapacidad()
            });
        }
    }

    private void estilizarTabla(JTable tabla) {
        tabla.setRowHeight(32);
        tabla.setFont(new Font("Arial", Font.PLAIN, 13));
        tabla.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tabla.getTableHeader().setBackground(COLOR_OSCURO);
        tabla.getTableHeader().setForeground(COLOR_BLANCO);
        tabla.setGridColor(new Color(230, 234, 240));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private JButton crearBotonNaranja(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(COLOR_NARANJA);
        btn.setForeground(COLOR_BLANCO);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(170, 36));
        return btn;
    }

    private static final List<Pedido> LISTA_PEDIDOS = new ArrayList<>();

    public static List<Pedido> getPedidos() {
        return LISTA_PEDIDOS;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FormAdmin().setVisible(true));
    }
}
