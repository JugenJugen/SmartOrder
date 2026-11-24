package com.mycompany.smartorder.dao;

import com.mycompany.smartorder.excepciones.DAOException;
import com.mycompany.smartorder.model.Usuario;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD de usuarios y autenticación.
 * Las contraseñas se guardan como hash SHA-256, nunca en texto plano.
 *
 * @author Elian Rojas
 */
public class UsuarioDAO implements CrudDAO<Usuario> {

    private static final String SELECT_BASE =
            "SELECT u.*, r.nombre AS rol FROM usuarios u JOIN roles r ON u.id_rol = r.id_rol ";

    @Override
    public int insertar(Usuario u) throws DAOException {
        String sql = "INSERT INTO usuarios (id_usuario, nombre, correo, contrasena_hash, id_rol, activo) "
                   + "SELECT ?, ?, ?, ?, id_rol, ? FROM roles WHERE nombre = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, u.getId());
            ps.setString(2, u.getNombre());
            ps.setString(3, u.getCorreo());
            ps.setString(4, hash(u.getContraseña()));
            ps.setBoolean(5, u.isEstado());
            ps.setString(6, u.getRol());
            if (ps.executeUpdate() == 0) {
                throw new DAOException("El rol '" + u.getRol() + "' no existe.");
            }
            return u.getId();
        } catch (SQLException e) {
            throw new DAOException("Error al registrar el usuario (¿identificación o correo repetido?): " + e.getMessage(), e);
        }
    }

    /** Actualiza los datos del usuario. La contraseña se cambia con cambiarContrasena(). */
    @Override
    public boolean actualizar(Usuario u) throws DAOException {
        String sql = "UPDATE usuarios SET nombre = ?, correo = ?, id_rol = (SELECT id_rol FROM roles WHERE nombre = ?), "
                   + "activo = ? WHERE id_usuario = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getCorreo());
            ps.setString(3, u.getRol());
            ps.setBoolean(4, u.isEstado());
            ps.setInt(5, u.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Error al actualizar el usuario: " + e.getMessage(), e);
        }
    }

    public boolean cambiarContrasena(int idUsuario, String nuevaContrasena) throws DAOException {
        String sql = "UPDATE usuarios SET contrasena_hash = ? WHERE id_usuario = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hash(nuevaContrasena));
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Error al cambiar la contraseña: " + e.getMessage(), e);
        }
    }

    /** Eliminación lógica: el usuario queda inactivo para no perder el historial. */
    @Override
    public boolean eliminar(int id) throws DAOException {
        String sql = "UPDATE usuarios SET activo = 0 WHERE id_usuario = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Error al desactivar el usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public Usuario buscarPorId(int id) throws DAOException {
        List<Usuario> r = consultar(SELECT_BASE + "WHERE u.id_usuario = ?", id);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public List<Usuario> listar() throws DAOException {
        return consultar(SELECT_BASE + "ORDER BY u.nombre");
    }

    public List<Usuario> listarPorRol(String rol) throws DAOException {
        return consultar(SELECT_BASE + "WHERE r.nombre = ? ORDER BY u.nombre", rol);
    }

    /**
     * Login: devuelve el usuario si el correo y la contraseña son correctos
     * y está activo; si no, devuelve null.
     */
    public Usuario autenticar(String correo, String contrasena) throws DAOException {
        List<Usuario> r = consultar(SELECT_BASE + "WHERE u.correo = ? AND u.contrasena_hash = ? AND u.activo = 1",
                correo, hash(contrasena));
        return r.isEmpty() ? null : r.get(0);
    }

    private List<Usuario> consultar(String sql, Object... params) throws DAOException {
        List<Usuario> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Usuario(rs.getInt("id_usuario"), rs.getString("nombre"), rs.getString("correo"),
                            rs.getString("contrasena_hash"), rs.getString("rol"), rs.getBoolean("activo")));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new DAOException("Error al consultar usuarios: " + e.getMessage(), e);
        }
    }

    /** SHA-256 en hexadecimal (mismo resultado que HASHBYTES('SHA2_256', texto) en SQL Server). */
    static String hash(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
