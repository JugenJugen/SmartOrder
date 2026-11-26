package com.mycompany.smartorder.dao;

import com.mycompany.smartorder.excepciones.DAOException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Punto único para obtener conexiones JDBC a la base de datos smartorder_db.
 * Lee la configuración de db.properties (src/main/resources).
 *
 * Cada llamada a getConexion() devuelve una conexión nueva: así cada hilo
 * del servidor usa su propia conexión y no hay problemas de concurrencia.
 * Siempre se debe usar con try-with-resources para que se cierre sola.
 *
 * @author Elian Rojas
 */
public final class ConexionBD {

    private static final String URL;
    private static final String USUARIO;
    private static final String CONTRASENA;

    static {
        Properties props = new Properties();
        try (InputStream in = ConexionBD.class.getResourceAsStream("/db.properties")) {
            if (in != null) {
                props.load(in);
            } else {
                System.err.println("No se encontró /db.properties en el classpath.");
            }
        } catch (IOException e) {
            System.err.println("Error al leer db.properties: " + e.getMessage());
        }
        URL = props.getProperty(
                "db.url",
                "jdbc:sqlserver://localhost:1433;databaseName=smartorder_db;encrypt=true;trustServerCertificate=true"
        ).trim();
        USUARIO = props.getProperty("db.usuario", "smartorder_app").trim();
        CONTRASENA = props.getProperty("db.contrasena", "").trim();
    }

    private ConexionBD() { }

    public static Connection getConexion() throws DAOException {
        try {
            return DriverManager.getConnection(URL, USUARIO, CONTRASENA);
        } catch (SQLException e) {
            throw new DAOException("No se pudo conectar a la base de datos: " + e.getMessage(), e);
        }
    }

    public static boolean probarConexion() {
        try (Connection con = getConexion()) {
            return con.isValid(3);
        } catch (DAOException | SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
}