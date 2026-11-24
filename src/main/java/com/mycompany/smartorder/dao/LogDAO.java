package com.mycompany.smartorder.dao;

import com.mycompany.smartorder.excepciones.DAOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Registro de auditoría en la tabla logs_sistema.
 *
 * @author Elian Rojas
 */
public class LogDAO {

    public void registrar(String nivel, String modulo, String accion, Integer idUsuario) throws DAOException {
        String sql = "INSERT INTO logs_sistema (nivel, modulo, accion, id_usuario) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nivel);
            ps.setString(2, modulo);
            ps.setString(3, accion);
            if (idUsuario == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, idUsuario);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Error al registrar el log: " + e.getMessage(), e);
        }
    }

    public void info(String modulo, String accion) throws DAOException {
        registrar("INFO", modulo, accion, null);
    }

    /** Devuelve los últimos logs como filas {fecha, nivel, modulo, accion, usuario} para un JTable. */
    public List<Object[]> listarRecientes(int limite) throws DAOException {
        String sql = "SELECT TOP (?) l.fecha, l.nivel, l.modulo, l.accion, u.nombre "
                   + "FROM logs_sistema l LEFT JOIN usuarios u ON l.id_usuario = u.id_usuario "
                   + "ORDER BY l.fecha DESC, l.id_log DESC";
        List<Object[]> filas = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filas.add(new Object[]{rs.getTimestamp(1), rs.getString(2), rs.getString(3),
                            rs.getString(4), rs.getString(5) != null ? rs.getString(5) : "Sistema"});
                }
            }
            return filas;
        } catch (SQLException e) {
            throw new DAOException("Error al consultar logs: " + e.getMessage(), e);
        }
    }
}
