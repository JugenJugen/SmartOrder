/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartorder.view;

/**
 *
 * @author angel
 */
import com.mycompany.smartorder.dao.MesaDAO;
import com.mycompany.smartorder.dao.ProductoDAO;
import com.mycompany.smartorder.dao.UsuarioDAO;
import com.mycompany.smartorder.excepciones.ValidacionException;
import com.mycompany.smartorder.model.Categoria;
import com.mycompany.smartorder.model.Mesa;
import com.mycompany.smartorder.model.Producto;
import com.mycompany.smartorder.model.Usuario;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

public class FormAdmin extends JFrame {

    private static final Color COLOR_FONDO = new Color(245, 247, 250);
    private static final Color COLOR_BLANCO = Color.WHITE;
    private static final Color COLOR_NARANJA = new Color(230, 126, 34);
    private static final Color COLOR_OSCURO = new Color(33, 43, 54);
    private static final Color COLOR_SELECCION = new Color(232, 237, 243);
    private static final Color COLOR_BORDE = new Color(210, 216, 224);

    static Iterable<Usuario> getEmpleados() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    static List<Mesa> getMesas() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final MesaDAO mesaDAO = new MesaDAO();

    private CardLayout cardLayout;
    private JPanel panelCards;

    private JButton btnCatBebidas, btnCatEntradas, btnCatPlatos, btnCatPostres;
    private JButton btnSeccionEmpleados, btnSeccionMesas;
    private String categoriaActual = "Entradas";

    private DefaultTableModel modeloProductos;
    private JTable tablaProductos;
    private JLabel lblTituloSeccionProd;
    private List<Producto> productosCache = new ArrayList<>();

    private Producto productoEditando = null;
    private JLabel lblTituloFormProd;
    private JTextField txtFormNombreProd;
    private JTextArea txtFormDescProd;
    private JTextField txtFormPrecioProd;
    private JTextField txtFormStockProd;
    private JComboBox<String> cmbFormCategoriaProd;
    private JButton btnGuardarFormProd;

    private DefaultTableModel modeloEmpleados;
    private JTable tablaEmpleados;

    private DefaultTableModel modeloMesas;
    private JTable tablaMesas;

    public FormAdmin() {
        setTitle("SmartOrder - Administración");
        setSize(1100, 720);
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

        String[] columnas = {"ID", "Nombre", "Descripción", "Stock", "Precio"};
        modeloProductos = new DefaultTableModel(new Object[][]{}, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaProductos = new JTable(modeloProductos);
        estilizarTabla(tablaProductos);

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
            int idProd = Integer.parseInt(modeloProductos.getValueAt(fila, 0).toString());
            Producto prod = buscarProductoEnCache(idProd);
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
            int idProd = Integer.parseInt(modeloProductos.getValueAt(fila, 0).toString());
            eliminarProductoEnBD(idProd);
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
        tarjeta.setPreferredSize(new Dimension(420, 500));
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
        scrollDesc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        scrollDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblPrecio = crearLabelCampo("Precio");
        txtFormPrecioProd = new JTextField();
        txtFormPrecioProd.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txtFormPrecioProd.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblStock = crearLabelCampo("Stock inicial");
        txtFormStockProd = new JTextField("10");
        txtFormStockProd.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txtFormStockProd.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblCat = crearLabelCampo("Categoría");
        cmbFormCategoriaProd = new JComboBox<>(new String[]{
            "Seleccione la categoría", "Bebidas", "Entradas", "Platos Fuertes", "Postres"
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
        tarjeta.add(Box.createVerticalStrut(10));

        tarjeta.add(lblDesc);
        tarjeta.add(Box.createVerticalStrut(4));
        tarjeta.add(scrollDesc);
        tarjeta.add(Box.createVerticalStrut(10));

        tarjeta.add(lblPrecio);
        tarjeta.add(Box.createVerticalStrut(4));
        tarjeta.add(txtFormPrecioProd);
        tarjeta.add(Box.createVerticalStrut(10));

        tarjeta.add(lblStock);
        tarjeta.add(Box.createVerticalStrut(4));
        tarjeta.add(txtFormStockProd);
        tarjeta.add(Box.createVerticalStrut(10));

        tarjeta.add(lblCat);
        tarjeta.add(Box.createVerticalStrut(4));
        tarjeta.add(cmbFormCategoriaProd);
        tarjeta.add(Box.createVerticalStrut(16));

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
            txtFormStockProd.setText("10");
            cmbFormCategoriaProd.setSelectedItem(categoriaActual);
        } else {
            lblTituloFormProd.setText("Editar Producto");
            btnGuardarFormProd.setText("Guardar Cambios");
            txtFormNombreProd.setText(existente.getNombre());
            txtFormDescProd.setText(existente.getDescripcion() != null ? existente.getDescripcion() : "");
            txtFormPrecioProd.setText(String.valueOf(existente.getPrecio()));
            txtFormStockProd.setText(String.valueOf(existente.getStock()));
            String catProd = (existente.getCategoria() != null) ? existente.getCategoria().getNombre() : categoriaActual;
            cmbFormCategoriaProd.setSelectedItem(catProd);
        }

        cardLayout.show(panelCards, "FORM_PRODUCTO");
    }

    private void guardarProductoDesdeForm() {
        String nombre = txtFormNombreProd.getText().trim();
        String descripcion = txtFormDescProd.getText().trim();
        String precioTexto = txtFormPrecioProd.getText().trim();
        String stockTexto = txtFormStockProd.getText().trim();
        String categoriaElegida = cmbFormCategoriaProd.getSelectedItem().toString();

        if ("Seleccione la categoría".equals(categoriaElegida)) {
            JOptionPane.showMessageDialog(this, "Seleccione una categoría para el producto.");
            return;
        }

        try {
            double precio = Double.parseDouble(precioTexto);
            int stock = Integer.parseInt(stockTexto);
            Categoria catObj = new Categoria(0, categoriaElegida);

            Producto objetivo = (productoEditando == null) ? new Producto(nombre, precio) : productoEditando;
            if (productoEditando != null) {
                objetivo.setNombre(nombre);
                objetivo.setPrecio(precio);
            }
            objetivo.setDescripcion(descripcion);
            objetivo.setStock(stock);
            objetivo.setDisponibilidad(stock > 0);
            objetivo.setCategoria(catObj);
            objetivo.validar();

            boolean esNuevo = (productoEditando == null);
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    if (esNuevo) {
                        productoDAO.insertar(objetivo);
                    } else {
                        productoDAO.actualizar(objetivo);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        productoEditando = null;
                        mostrarCategoriaProductos(categoriaElegida);
                    } catch (Exception ex) {
                        mostrarErrorDAO("No se pudo guardar el producto", ex);
                    }
                }
            }.execute();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese valores numéricos válidos para precio y stock.");
        } catch (ValidacionException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void eliminarProductoEnBD(int idProd) {
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return productoDAO.eliminar(idProd);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        cargarTablaProductos();
                    }
                } catch (Exception ex) {
                    mostrarErrorDAO("No se pudo eliminar el producto", ex);
                }
            }
        }.execute();
    }

    private void cargarTablaProductos() {
        if (modeloProductos == null) {
            return;
        }
        new SwingWorker<List<Producto>, Void>() {
            @Override
            protected List<Producto> doInBackground() throws Exception {
                return productoDAO.listarPorCategoria(categoriaActual);
            }

            @Override
            protected void done() {
                try {
                    productosCache = get();
                    productosCache.sort(Comparator.comparing(Producto::getNombre, String.CASE_INSENSITIVE_ORDER));
                    modeloProductos.setRowCount(0);
                    for (Producto p : productosCache) {
                        modeloProductos.addRow(new Object[]{
                            p.getId(),
                            p.getNombre(),
                            p.getDescripcion() != null ? p.getDescripcion() : "",
                            p.getStock(),
                            String.format("%.2f", p.getPrecio())
                        });
                    }
                } catch (Exception ex) {
                    mostrarErrorDAO("Error al cargar productos desde la BD", ex);
                }
            }
        }.execute();
    }

    private Producto buscarProductoEnCache(int idProd) {
        for (Producto p : productosCache) {
            if (p.getId() == idProd) {
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

        String[] columnas = {"Identificación", "Nombre", "Correo", "Puesto", "Estado"};
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
                    new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            usuarioDAO.insertar(nuevo);
                            return null;
                        }

                        @Override
                        protected void done() {
                            try {
                                get();
                                cargarTablaEmpleados();
                            } catch (Exception ex) {
                                mostrarErrorDAO("No se pudo registrar el empleado", ex);
                            }
                        }
                    }.execute();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "La identificación debe ser numérica.");
                } catch (ValidacionException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage());
                }
            }
        });

        JButton btnEliminarEmp = new JButton("Desactivar Seleccionado");
        btnEliminarEmp.addActionListener(e -> {
            int fila = tablaEmpleados.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione un empleado de la tabla.");
                return;
            }
            int idEmp = Integer.parseInt(modeloEmpleados.getValueAt(fila, 0).toString());
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return usuarioDAO.eliminar(idEmp);
                }

                @Override
                protected void done() {
                    try {
                        get();
                        cargarTablaEmpleados();
                    } catch (Exception ex) {
                        mostrarErrorDAO("No se pudo actualizar el estado del empleado", ex);
                    }
                }
            }.execute();
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
        new SwingWorker<List<Usuario>, Void>() {
            @Override
            protected List<Usuario> doInBackground() throws Exception {
                return usuarioDAO.listar();
            }

            @Override
            protected void done() {
                try {
                    List<Usuario> lista = get();
                    modeloEmpleados.setRowCount(0);
                    for (Usuario u : lista) {
                        modeloEmpleados.addRow(new Object[]{
                            u.getId(),
                            u.getNombre(),
                            u.getCorreo(),
                            u.getRol(),
                            u.isEstado() ? "Activo" : "Inactivo"
                        });
                    }
                } catch (Exception ex) {
                    mostrarErrorDAO("Error al cargar empleados desde la BD", ex);
                }
            }
        }.execute();
    }

    private JPanel crearPanelMesas() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel lblTitulo = new JLabel("Mesas");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(COLOR_OSCURO);
        panel.add(lblTitulo, BorderLayout.NORTH);

        String[] columnas = {"# de Mesa", "Capacidad", "Estado"};
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
            String numTexto = JOptionPane.showInputDialog(this, "# de Mesa:", String.valueOf(modeloMesas.getRowCount() + 1));
            String capTexto = JOptionPane.showInputDialog(this, "Capacidad:", "4");
            if (numTexto != null && capTexto != null) {
                try {
                    int num = Integer.parseInt(numTexto.trim());
                    int cap = Integer.parseInt(capTexto.trim());
                    Mesa nuevaMesa = new Mesa(num, cap, "Disponible");
                    nuevaMesa.validar();
                    new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            mesaDAO.insertar(nuevaMesa);
                            return null;
                        }

                        @Override
                        protected void done() {
                            try {
                                get();
                                cargarTablaMesas();
                            } catch (Exception ex) {
                                mostrarErrorDAO("No se pudo registrar la mesa", ex);
                            }
                        }
                    }.execute();
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
                JOptionPane.showMessageDialog(this, "Seleccione una mesa de la tabla.");
                return;
            }
            int numMesa = Integer.parseInt(modeloMesas.getValueAt(fila, 0).toString());
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return mesaDAO.eliminar(numMesa);
                }

                @Override
                protected void done() {
                    try {
                        get();
                        cargarTablaMesas();
                    } catch (Exception ex) {
                        mostrarErrorDAO("No se pudo eliminar la mesa", ex);
                    }
                }
            }.execute();
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
        new SwingWorker<List<Mesa>, Void>() {
            @Override
            protected List<Mesa> doInBackground() throws Exception {
                return mesaDAO.listar();
            }

            @Override
            protected void done() {
                try {
                    List<Mesa> lista = get();
                    modeloMesas.setRowCount(0);
                    for (Mesa m : lista) {
                        modeloMesas.addRow(new Object[]{
                            m.getNumeroMesa(),
                            m.getCapacidad(),
                            m.getEstado()
                        });
                    }
                } catch (Exception ex) {
                    mostrarErrorDAO("Error al cargar mesas desde la BD", ex);
                }
            }
        }.execute();
    }

    private void mostrarErrorDAO(String prefijo, Exception ex) {
        Throwable causa = (ex.getCause() != null) ? ex.getCause() : ex;
        String mensaje = (causa.getMessage() != null) ? causa.getMessage() : ex.getMessage();
        JOptionPane.showMessageDialog(this, prefijo + ":\n" + mensaje, "Error de base de datos", JOptionPane.ERROR_MESSAGE);
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
        btn.setPreferredSize(new Dimension(180, 36));
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FormAdmin().setVisible(true));
    }
}
