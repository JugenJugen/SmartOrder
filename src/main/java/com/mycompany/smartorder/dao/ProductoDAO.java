package com.mycompany.smartorder.dao;

import com.mycompany.smartorder.excepciones.DAOException;
import com.mycompany.smartorder.model.Categoria;
import com.mycompany.smartorder.model.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD de productos del menú usando PreparedStatement.
 *
 * @author Elian Rojas
 */
public class ProductoDAO implements CrudDAO<Producto> {

    private static final String SELECT_BASE =
            "SELECT p.*, c.nombre AS cat_nombre, c.descripcion AS cat_desc "
          + "FROM productos p JOIN categorias c ON p.id_categoria = c.id_categoria ";

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    @Override
    public int insertar(Producto p) throws DAOException {
        String sql = "INSERT INTO productos (nombre, descripcion, precio, disponible, stock, id_categoria) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        int idCategoria = resolverCategoria(p.getCategoria());
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setDouble(3, p.getPrecio());
            ps.setBoolean(4, p.isDisponibilidad());
            ps.setInt(5, p.getStock());
            ps.setInt(6, idCategoria);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId(rs.getInt(1));
                }
            }
            return p.getId();
        } catch (SQLException e) {
            throw new DAOException("Error al insertar el producto: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean actualizar(Producto p) throws DAOException {
        String sql = "UPDATE productos SET nombre = ?, descripcion = ?, precio = ?, disponible = ?, "
                   + "stock = ?, id_categoria = ? WHERE id_producto = ?";
        int idCategoria = resolverCategoria(p.getCategoria());
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setDouble(3, p.getPrecio());
            ps.setBoolean(4, p.isDisponibilidad());
            ps.setInt(5, p.getStock());
            ps.setInt(6, idCategoria);
            ps.setInt(7, p.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Error al actualizar el producto: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean eliminar(int id) throws DAOException {
        String sql = "DELETE FROM productos WHERE id_producto = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // Si el producto ya está en pedidos, la FK no deja borrarlo: se marca como no disponible
            throw new DAOException("No se puede eliminar el producto porque ya tiene pedidos. "
                    + "Márquelo como no disponible.", e);
        }
    }

    @Override
    public Producto buscarPorId(int id) throws DAOException {
        List<Producto> r = consultar(SELECT_BASE + "WHERE p.id_producto = ?", id);
        return r.isEmpty() ? null : r.get(0);
    }

    public Producto buscarPorNombre(String nombre) throws DAOException {
        List<Producto> r = consultar(SELECT_BASE + "WHERE p.nombre = ?", nombre);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public List<Producto> listar() throws DAOException {
        return consultar(SELECT_BASE + "ORDER BY p.id_categoria, p.nombre");
    }

    public List<Producto> listarPorCategoria(String nombreCategoria) throws DAOException {
        return consultar(SELECT_BASE + "WHERE c.nombre = ? ORDER BY p.nombre", nombreCategoria);
    }

    /** Stock actual de un producto por nombre (-1 si no existe). */
    public int buscarNombreStock(String nombre) throws DAOException {
        Producto p = buscarPorNombre(nombre);
        return p == null ? -1 : p.getStock();
    }

    /** Menú para los clientes: solo productos disponibles y con stock. */
    public List<Producto> listarDisponibles() throws DAOException {
        return consultar(SELECT_BASE + "WHERE p.disponible = 1 AND p.stock > 0 ORDER BY p.id_categoria, p.nombre");
    }

    private List<Producto> consultar(String sql, Object... params) throws DAOException {
        List<Producto> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new DAOException("Error al consultar productos: " + e.getMessage(), e);
        }
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        Producto p = new Producto(rs.getString("nombre"), rs.getDouble("precio"));
        p.setId(rs.getInt("id_producto"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setDisponibilidad(rs.getBoolean("disponible"));
        p.setStock(rs.getInt("stock"));
        p.setCategoria(new Categoria(rs.getInt("id_categoria"), rs.getString("cat_nombre"), rs.getString("cat_desc")));
        return p;
    }

    /** Las vistas a veces crean la categoría con id 0 y solo el nombre: aquí se busca su id real. */
    private int resolverCategoria(Categoria c) throws DAOException {
        if (c == null) {
            throw new DAOException("El producto debe tener una categoría.");
        }
        if (c.getId() > 0) {
            return c.getId();
        }
        Categoria encontrada = categoriaDAO.buscarPorNombre(c.getNombre());
        if (encontrada == null) {
            throw new DAOException("La categoría '" + c.getNombre() + "' no existe.");
        }
        return encontrada.getId();
    }
}
