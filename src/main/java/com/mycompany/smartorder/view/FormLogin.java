/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartorder.view;

/**
 *
 * @author angel
 */
import com.mycompany.smartorder.dao.UsuarioDAO;
import com.mycompany.smartorder.excepciones.DAOException;
import com.mycompany.smartorder.model.Usuario;
import java.awt.*;
import java.util.Arrays;
import javax.swing.*;
public class FormLogin extends JFrame {

    private JTextField txtEmail;
    private JPasswordField txtContrasena;
    private JButton btnIniciarSesion;

    public FormLogin() {
        setTitle("SmartOrder - Iniciar Sesión");
        setSize(800, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panelFondo = new JPanel();
        panelFondo.setBackground(new Color(33, 43, 54));
        panelFondo.setLayout(new GridBagLayout());

        JPanel tarjetaBlanca = new JPanel();
        tarjetaBlanca.setBackground(Color.WHITE);
        tarjetaBlanca.setPreferredSize(new Dimension(420, 360));
        tarjetaBlanca.setLayout(null);

        JLabel lblTitulo = new JLabel("Bienvenido/a");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitulo.setBounds(140, 30, 150, 30);
        tarjetaBlanca.add(lblTitulo);

        JLabel lblSub = new JLabel("Por favor, iniciar sesión para continuar");
        lblSub.setFont(new Font("Arial", Font.PLAIN, 12));
        lblSub.setForeground(Color.GRAY);
        lblSub.setBounds(105, 60, 220, 20);
        tarjetaBlanca.add(lblSub);

        JLabel lblEmail = new JLabel("Email o Usuario");
        lblEmail.setFont(new Font("Arial", Font.PLAIN, 11));
        lblEmail.setForeground(Color.DARK_GRAY);
        lblEmail.setBounds(50, 100, 150, 20);
        tarjetaBlanca.add(lblEmail);

        txtEmail = new JTextField();
        txtEmail.setBounds(50, 120, 320, 35);
        tarjetaBlanca.add(txtEmail);

        JLabel lblPass = new JLabel("Contraseña");
        lblPass.setFont(new Font("Arial", Font.PLAIN, 11));
        lblPass.setForeground(Color.DARK_GRAY);
        lblPass.setBounds(50, 170, 150, 20);
        tarjetaBlanca.add(lblPass);

        txtContrasena = new JPasswordField();
        txtContrasena.setBounds(50, 190, 320, 35);
        tarjetaBlanca.add(txtContrasena);

        txtContrasena.addActionListener(e -> validarAcceso());

        btnIniciarSesion = new JButton("Iniciar Sesión");
        btnIniciarSesion.setBackground(new Color(230, 126, 34));
        btnIniciarSesion.setForeground(Color.WHITE);
        btnIniciarSesion.setFont(new Font("Arial", Font.BOLD, 13));
        btnIniciarSesion.setFocusPainted(false);
        btnIniciarSesion.setBounds(50, 255, 320, 40);
        tarjetaBlanca.add(btnIniciarSesion);

        btnIniciarSesion.addActionListener(e -> validarAcceso());

        panelFondo.add(tarjetaBlanca);
        add(panelFondo);
    }

    private void validarAcceso() {
        String usuario = txtEmail.getText().trim();
        char[] rawPassword = txtContrasena.getPassword();
        String password = new String(rawPassword).trim();
        Arrays.fill(rawPassword, '\0');

        if (usuario.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Por favor, complete todos los campos.",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // 1. Intentar autenticación real contra SQL Server (UsuarioDAO con SHA-256)
        try {
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            Usuario autenticado = usuarioDAO.autenticar(usuario, password);
            if (autenticado != null) {
                FormAuditoriaLogs.registrarEvento("INFO", "Login", "Acceso BD exitoso: " + autenticado.getNombre());
                abrirVistaPorRol(autenticado.getRol(), "Bienvenido/a " + autenticado.getNombre() + ".");
                return;
            }
        } catch (DAOException ex) {
            System.err.println("BD no disponible en Login, usando respaldo local: " + ex.getMessage());
        }

        String usuarioNormalizado = usuario.toLowerCase();

        // 2. Credenciales base rápidas (respaldo local)
        if ("1234".equals(password)) {
            if ("admin@smartorder.com".equals(usuarioNormalizado) || "admin".equals(usuarioNormalizado)) {
                FormAuditoriaLogs.registrarEvento("INFO", "Login", "Acceso local: Administrador");
                abrirVistaPorRol("Administrador", "Bienvenido Administrador.");
                return;
            }
            if ("cocina@smartorder.com".equals(usuarioNormalizado) || "cocina".equals(usuarioNormalizado)) {
                FormAuditoriaLogs.registrarEvento("INFO", "Login", "Acceso local: Cocina");
                abrirVistaPorRol("Cocina", "Bienvenido a Estación de Cocina.");
                return;
            }
            if ("cliente@smartorder.com".equals(usuarioNormalizado) || "cliente".equals(usuarioNormalizado)) {
                FormAuditoriaLogs.registrarEvento("INFO", "Login", "Acceso local: Atención/Cliente");
                abrirVistaPorRol("Atención", "Bienvenido a Toma de Pedidos.");
                return;
            }
        }

        // 3. Empleados registrados en FormAdmin
        for (Usuario emp : FormAdmin.getEmpleados()) {
            if (emp.isEstado() && emp.getContraseña() != null && password.equals(emp.getContraseña())) {
                boolean coincideCorreo = emp.getCorreo() != null
                        && emp.getCorreo().trim().equalsIgnoreCase(usuarioNormalizado);
                boolean coincideNombre = emp.getNombre() != null
                        && emp.getNombre().trim().equalsIgnoreCase(usuarioNormalizado);

                if (coincideCorreo || coincideNombre) {
                    FormAuditoriaLogs.registrarEvento("INFO", "Login", "Acceso local empleado: " + emp.getNombre());
                    abrirVistaPorRol(emp.getRol(), "Bienvenido/a " + emp.getNombre() + ".");
                    return;
                }
            }
        }

        FormAuditoriaLogs.registrarEvento("ERROR", "Login", "Intento fallido para usuario: " + usuario);
        JOptionPane.showMessageDialog(
                this,
                "Credenciales incorrectas o usuario inactivo.",
                "Error de autenticación",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void abrirVistaPorRol(String rol, String mensajeBienvenida) {
        JOptionPane.showMessageDialog(
                this,
                "¡Acceso concedido! " + mensajeBienvenida,
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE
        );

        String rolNormalizado = (rol != null) ? rol.trim().toLowerCase() : "";

        if (rolNormalizado.contains("admin")) {
            FormAdmin vistaAdmin = new FormAdmin();
            vistaAdmin.setVisible(true);
        } else if (rolNormalizado.contains("cocina")) {
            FormCocina vistaCocina = new FormCocina();
            vistaCocina.cargarPedidosEnTablas();
            vistaCocina.setVisible(true);
        } else {
            FormClientePedido vistaCliente = new FormClientePedido();
            vistaCliente.setVisible(true);
        }

        this.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FormLogin().setVisible(true);
        });
    }
}
