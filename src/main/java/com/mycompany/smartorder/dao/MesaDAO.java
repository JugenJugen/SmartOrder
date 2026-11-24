package com.mycompany.smartorder.dao;

import com.mycompany.smartorder.excepciones.DAOException;
import com.mycompany.smartorder.model.Mesa;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD de mesas. En el modelo el id de la Mesa es su número,
 * por eso aquí se trabaja por número (tipo MESA).
 *
 * @author Elian Rojas
 */
public class MesaDAO implements CrudDAO<Mesa> {

    @Override
    public int insertar(Mesa m) throws DAOException {
        String sql = "INSERT INTO mesas (numero, tipo, capacidad, estado) VALUES (?, 'MESA', ?, ?)";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, m.getNumeroMesa());
            ps.setInt(2, m.getCapacidad());
            ps.setString(3, m.getEstado());
            ps.executeUpdate();
            return m.getNumeroMesa();
        } catch (SQLException e) {
            throw new DAOException("Error al registrar la mesa (¿número repetido?): " + e.getMessage(), e);
        }
    }

    @Override
    public boolean actualizar(Mesa m) throws DAOException {
        String sql = "UPDATE mesas SET capacidad = ?, estado = ? WHERE numero = ? AND tipo = 'MESA'";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, m.getCapacidad());
            ps.setString(2, m.getEstado());
            ps.setInt(3, m.getNumeroMesa());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Error al actualizar la mesa: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean eliminar(int numero) throws DAOException {
        String sql = "DELETE FROM mesas WHERE numero = ? AND tipo = 'MESA'";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, numero);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("No se puede eliminar la mesa porque tiene pedidos registrados.", e);
        }
    }

    @Override
    public Mesa buscarPorId(int numero) throws DAOException {
        List<Mesa> r = consultar("SELECT * FROM mesas WHERE numero = ? AND tipo = 'MESA'", numero);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public List<Mesa> listar() throws DAOException {
        return consultar("SELECT * FROM mesas WHERE tipo = 'MESA' ORDER BY numero");
    }

    /** Números de las mesas disponibles (útil para el combo del cliente). */
    public List<Integer> listarNumerosDisponibles() throws DAOException {
        List<Integer> numeros = new ArrayList<>();
        for (Mesa m : consultar("SELECT * FROM mesas WHERE tipo = 'MESA' AND estado = 'Disponible' ORDER BY numero")) {
            numeros.add(m.getNumeroMesa());
        }
        return numeros;
    }

    private List<Mesa> consultar(String sql, Object... params) throws DAOException {
        List<Mesa> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Mesa(rs.getInt("numero"), rs.getInt("capacidad"), rs.getString("estado")));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new DAOException("Error al consultar mesas: " + e.getMessage(), e);
        }
    }
}
