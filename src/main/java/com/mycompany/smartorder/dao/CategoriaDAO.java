package com.mycompany.smartorder.dao;

import com.mycompany.smartorder.excepciones.DAOException;
import com.mycompany.smartorder.model.Categoria;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD de categorías del menú usando PreparedStatement.
 *
 * @author Elian Rojas
 */
public class CategoriaDAO implements CrudDAO<Categoria> {

    /** Estación por defecto (Cocina caliente) cuando se crea una categoría nueva. */
    private static final int ESTACION_POR_DEFECTO = 2;

    @Override
    public int insertar(Categoria c) throws DAOException {
        String sql = "INSERT INTO categorias (nombre, descripcion, id_estacion) VALUES (?, ?, ?)";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDescripcion());
            ps.setInt(3, ESTACION_POR_DEFECTO);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    c.setId(rs.getInt(1));
                }
            }
            return c.getId();
        } catch (SQLException e) {
            throw new DAOException("Error al insertar la categoría: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean actualizar(Categoria c) throws DAOException {
        String sql = "UPDATE categorias SET nombre = ?, descripcion = ? WHERE id_categoria = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDescripcion());
            ps.setInt(3, c.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Error al actualizar la categoría: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean eliminar(int id) throws DAOException {
        String sql = "DELETE FROM categorias WHERE id_categoria = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("No se puede eliminar la categoría (puede tener productos asociados).", e);
        }
    }

    @Override
    public Categoria buscarPorId(int id) throws DAOException {
        return buscarUno("SELECT * FROM categorias WHERE id_categoria = ?", id);
    }

    public Categoria buscarPorNombre(String nombre) throws DAOException {
        return buscarUno("SELECT * FROM categorias WHERE nombre = ?", nombre);
    }

    @Override
    public List<Categoria> listar() throws DAOException {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT * FROM categorias ORDER BY id_categoria";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new DAOException("Error al listar categorías: " + e.getMessage(), e);
        }
    }

    private Categoria buscarUno(String sql, Object parametro) throws DAOException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new DAOException("Error al buscar la categoría: " + e.getMessage(), e);
        }
    }

    private Categoria mapear(ResultSet rs) throws SQLException {
        return new Categoria(rs.getInt("id_categoria"), rs.getString("nombre"), rs.getString("descripcion"));
    }
}
