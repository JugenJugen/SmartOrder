package com.mycompany.smartorder.dao;

import com.mycompany.smartorder.excepciones.DAOException;
import com.mycompany.smartorder.model.Categoria;
import com.mycompany.smartorder.model.DetallePedido;
import com.mycompany.smartorder.model.ItemPedido;
import com.mycompany.smartorder.model.Mesa;
import com.mycompany.smartorder.model.Pedido;
import com.mycompany.smartorder.model.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Persistencia de pedidos. Crear un pedido y cambiar su estado se hacen
 * en TRANSACCIONES (commit / rollback): o se guarda todo o no se guarda nada.
 * Esto es importante porque el servidor recibe pedidos de varias mesas a la vez.
 *
 * @author Elian Rojas
 */
public class PedidoDAO {

    public static final String RECIBIDO = "Recibido";
    public static final String EN_PREPARACION = "En preparación";
    public static final String LISTO = "Listo";
    public static final String ENTREGADO = "Entregado";
    public static final String CANCELADO = "Cancelado";

    /**
     * Crea un pedido completo a partir de lo que manda el cliente.
     *
     * @param numeroPunto número de la mesa o barra (null si es para llevar sin punto)
     * @param tipoPunto   "MESA" o "BARRA"
     * @param tipoPedido  "Local" o "Para llevar" (se normaliza)
     * @param observacion observación general del pedido
     * @param items       productos (por nombre) y cantidades
     * @param idUsuario   usuario que lo registra (puede ser null si lo hace el cliente)
     * @return id del pedido generado
     */
    public int crearPedido(Integer numeroPunto, String tipoPunto, String tipoPedido, String observacion,
                           List<ItemPedido> items, Integer idUsuario) throws DAOException {
        if (items == null || items.isEmpty()) {
            throw new DAOException("No se puede registrar un pedido sin productos.");
        }
        // Se ordenan los productos por nombre para que todos los hilos bloqueen las filas
        // en el mismo orden (así se evitan deadlocks entre pedidos simultáneos).
        List<ItemPedido> ordenados = new ArrayList<>(items);
        ordenados.sort(Comparator.comparing(ItemPedido::getNombre));
        for (int intento = 1; ; intento++) {
            try {
                return crearPedidoTx(numeroPunto, tipoPunto, tipoPedido, observacion, ordenados, idUsuario);
            } catch (DAOException e) {
                // 1205 = SQL Server eligió esta transacción como víctima de deadlock: se reintenta
                if (intento < 3 && e.getCause() instanceof SQLException
                        && ((SQLException) e.getCause()).getErrorCode() == 1205) {
                    continue;
                }
                throw e;
            }
        }
    }

    private int crearPedidoTx(Integer numeroPunto, String tipoPunto, String tipoPedido, String observacion,
                              List<ItemPedido> items, Integer idUsuario) throws DAOException {
        Connection con = null;
        try {
            con = ConexionBD.getConexion();
            con.setAutoCommit(false);   // ---- inicia la transacción ----

            Integer idMesa = (numeroPunto == null) ? null : buscarIdMesa(con, numeroPunto, tipoPunto);
            int idEstado = idEstado(con, RECIBIDO);

            // 1. Encabezado del pedido
            int idPedido;
            String sqlPedido = "INSERT INTO pedidos (id_mesa, id_usuario, tipo_pedido, id_estado, observacion) "
                             + "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                setEntero(ps, 1, idMesa);
                setEntero(ps, 2, idUsuario);
                ps.setString(3, normalizarTipo(tipoPedido));
                ps.setInt(4, idEstado);
                ps.setString(5, observacion);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    idPedido = rs.getInt(1);
                }
            }

            // 2. Detalles: se bloquea la fila del producto (UPDLOCK) para validar y rebajar stock
            //    sin que otro hilo venda el mismo stock al mismo tiempo.
            double total = 0;
            Set<Integer> estaciones = new LinkedHashSet<>();
            String sqlProd = "SELECT TOP 1 p.id_producto, p.precio, p.stock, p.disponible, c.id_estacion "
                           + "FROM productos p WITH (UPDLOCK, ROWLOCK) "
                           + "JOIN categorias c ON p.id_categoria = c.id_categoria "
                           + "WHERE p.nombre = ?";
            String sqlDet = "INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
            String sqlStock = "UPDATE productos SET stock = stock - ? WHERE id_producto = ?";
            try (PreparedStatement psProd = con.prepareStatement(sqlProd);
                 PreparedStatement psDet = con.prepareStatement(sqlDet);
                 PreparedStatement psStock = con.prepareStatement(sqlStock)) {
                for (ItemPedido item : items) {
                    if (item.getCantidad() <= 0) {
                        throw new DAOException("Cantidad inválida para " + item.getNombre());
                    }
                    psProd.setString(1, item.getNombre());
                    int idProducto;
                    double precio;
                    try (ResultSet rs = psProd.executeQuery()) {
                        if (!rs.next()) {
                            throw new DAOException("El producto '" + item.getNombre() + "' no existe.");
                        }
                        if (!rs.getBoolean("disponible") || rs.getInt("stock") < item.getCantidad()) {
                            throw new DAOException("No hay suficiente '" + item.getNombre() + "' disponible.");
                        }
                        idProducto = rs.getInt("id_producto");
                        precio = rs.getDouble("precio");   // se usa el precio de la BD, no el del cliente
                        estaciones.add(rs.getInt("id_estacion"));
                    }
                    psDet.setInt(1, idPedido);
                    psDet.setInt(2, idProducto);
                    psDet.setInt(3, item.getCantidad());
                    psDet.setDouble(4, precio);
                    psDet.addBatch();

                    psStock.setInt(1, item.getCantidad());
                    psStock.setInt(2, idProducto);
                    psStock.addBatch();
                    total += precio * item.getCantidad();
                }
                psDet.executeBatch();
                psStock.executeBatch();
            }

            // 3. Total del pedido
            try (PreparedStatement ps = con.prepareStatement("UPDATE pedidos SET total = ?, fecha_actualizacion = SYSDATETIME() WHERE id_pedido = ?")) {
                ps.setDouble(1, total);
                ps.setInt(2, idPedido);
                ps.executeUpdate();
            }

            // 4. Se envía a cada estación de preparación involucrada
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO pedido_estacion (id_pedido, id_estacion, id_estado) VALUES (?, ?, ?)")) {
                for (int idEstacion : estaciones) {
                    ps.setInt(1, idPedido);
                    ps.setInt(2, idEstacion);
                    ps.setInt(3, idEstado);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // 5. Historial + log de auditoría
            insertarHistorial(con, idPedido, idEstado, idUsuario);
            insertarLog(con, "Pedidos", "Pedido #" + idPedido + " creado (" + items.size() + " productos, total ₡" + total + ")", idUsuario);

            con.commit();   // ---- todo salió bien ----
            return idPedido;
        } catch (SQLException | DAOException e) {
            rollback(con);
            if (e instanceof DAOException) {
                throw (DAOException) e;
            }
            throw new DAOException("Error al guardar el pedido: " + e.getMessage(), e);
        } finally {
            cerrar(con);
        }
    }

    /** Guarda un objeto Pedido del modelo (usa los detalles con su producto). */
    public int insertar(Pedido pedido) throws DAOException {
        List<ItemPedido> items = new ArrayList<>();
        for (DetallePedido d : pedido.getDetalles()) {
            items.add(new ItemPedido(d.getProducto().getNombre(), d.getPrecioUnitario(), d.getCantidad()));
        }
        Integer mesa = pedido.getMesa() != null ? pedido.getMesa().getNumeroMesa() : null;
        Integer usuario = pedido.getUsuario() != null ? pedido.getUsuario().getId() : null;
        int id = crearPedido(mesa, "MESA", pedido.getTipoPedido(), null, items, usuario);
        pedido.setId(id);
        return id;
    }

    /**
     * Cambia el estado de un pedido (Recibido, En preparación, Listo, Entregado, Cancelado).
     * Si se cancela, se devuelve el stock de los productos.
     */
    public boolean actualizarEstado(int idPedido, String nuevoEstado, Integer idUsuario) throws DAOException {
        Connection con = null;
        try {
            con = ConexionBD.getConexion();
            con.setAutoCommit(false);
            int idEstado = idEstado(con, nuevoEstado);

            int filas;
            try (PreparedStatement ps = con.prepareStatement("UPDATE pedidos SET id_estado = ?, fecha_actualizacion = SYSDATETIME() WHERE id_pedido = ?")) {
                ps.setInt(1, idEstado);
                ps.setInt(2, idPedido);
                filas = ps.executeUpdate();
            }
            if (filas == 0) {
                con.rollback();
                return false;
            }
            try (PreparedStatement ps = con.prepareStatement("UPDATE pedido_estacion SET id_estado = ? WHERE id_pedido = ?")) {
                ps.setInt(1, idEstado);
                ps.setInt(2, idPedido);
                ps.executeUpdate();
            }
            if (CANCELADO.equals(nuevoEstado)) {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE productos SET stock = stock + (SELECT SUM(d.cantidad) FROM detalle_pedido d "
                      + "WHERE d.id_pedido = ? AND d.id_producto = productos.id_producto) "
                      + "WHERE id_producto IN (SELECT id_producto FROM detalle_pedido WHERE id_pedido = ?)")) {
                    ps.setInt(1, idPedido);
                    ps.setInt(2, idPedido);
                    ps.executeUpdate();
                }
            }
            insertarHistorial(con, idPedido, idEstado, idUsuario);
            insertarLog(con, "Cocina", "Pedido #" + idPedido + " cambió a " + nuevoEstado, idUsuario);
            con.commit();
            return true;
        } catch (SQLException | DAOException e) {
            rollback(con);
            if (e instanceof DAOException) {
                throw (DAOException) e;
            }
            throw new DAOException("Error al actualizar el estado: " + e.getMessage(), e);
        } finally {
            cerrar(con);
        }
    }

    /** Pago simulado (sin pasarela real): registra método y monto como APROBADO. */
    public int registrarPago(int idPedido, String metodo) throws DAOException {
        String sql = "INSERT INTO pagos (id_pedido, metodo, monto, estado) "
                   + "SELECT id_pedido, ?, total, 'APROBADO' FROM pedidos WHERE id_pedido = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, metodo.toUpperCase());
            ps.setInt(2, idPedido);
            if (ps.executeUpdate() == 0) {
                throw new DAOException("El pedido #" + idPedido + " no existe.");
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DAOException("Error al registrar el pago (métodos: EFECTIVO, TARJETA, SINPE): " + e.getMessage(), e);
        }
    }

    public Pedido buscarPorId(int idPedido) throws DAOException {
        List<Pedido> r = consultar("WHERE pe.id_pedido = ?", idPedido);
        return r.isEmpty() ? null : r.get(0);
    }

    /** Pedidos que la cocina todavía tiene que atender (no entregados ni cancelados). */
    public List<Pedido> listarActivos() throws DAOException {
        return consultar("WHERE e.nombre NOT IN ('Entregado', 'Cancelado')");
    }

    public List<Pedido> listarPorEstado(String estado) throws DAOException {
        return consultar("WHERE e.nombre = ?", estado);
    }

    public List<Pedido> listarTodos() throws DAOException {
        return consultar("");
    }

    /** Historial de estados de un pedido como filas {fecha, estado, usuario}. */
    public List<Object[]> historial(int idPedido) throws DAOException {
        String sql = "SELECT h.fecha_cambio, e.nombre, u.nombre FROM historial_estado_pedido h "
                   + "JOIN estados_pedido e ON h.id_estado = e.id_estado "
                   + "LEFT JOIN usuarios u ON h.id_usuario = u.id_usuario "
                   + "WHERE h.id_pedido = ? ORDER BY h.id_historial";
        List<Object[]> filas = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filas.add(new Object[]{rs.getTimestamp(1), rs.getString(2),
                            rs.getString(3) != null ? rs.getString(3) : "Cliente"});
                }
            }
            return filas;
        } catch (SQLException e) {
            throw new DAOException("Error al consultar el historial: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------ privados

    /** Una sola consulta con JOIN trae pedidos + detalles, y se arman los objetos del modelo. */
    private List<Pedido> consultar(String where, Object... params) throws DAOException {
        String sql = "SELECT pe.id_pedido, pe.tipo_pedido, e.nombre AS estado, "
                   + "m.numero, m.capacidad, m.estado AS estado_mesa, "
                   + "d.cantidad, d.precio_unitario, d.observacion AS obs_det, "
                   + "p.id_producto, p.nombre AS producto, c.id_categoria, c.nombre AS categoria "
                   + "FROM pedidos pe "
                   + "JOIN estados_pedido e ON pe.id_estado = e.id_estado "
                   + "LEFT JOIN mesas m ON pe.id_mesa = m.id_mesa "
                   + "JOIN detalle_pedido d ON d.id_pedido = pe.id_pedido "
                   + "JOIN productos p ON d.id_producto = p.id_producto "
                   + "JOIN categorias c ON p.id_categoria = c.id_categoria "
                   + where + " ORDER BY pe.fecha_creacion, pe.id_pedido, d.id_detalle";
        Map<Integer, Pedido> pedidos = new LinkedHashMap<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id_pedido");
                    Pedido pedido = pedidos.get(id);
                    if (pedido == null) {
                        Mesa mesa = null;
                        if (rs.getObject("numero") != null) {
                            mesa = new Mesa(rs.getInt("numero"), rs.getInt("capacidad"), rs.getString("estado_mesa"));
                        }
                        String tipo = "LLEVAR".equals(rs.getString("tipo_pedido")) ? "Para llevar" : "Local";
                        pedido = new Pedido(id, mesa, null, tipo);
                        pedido.setEstado(rs.getString("estado"));
                        pedidos.put(id, pedido);
                    }
                    Producto prod = new Producto(rs.getString("producto"), rs.getDouble("precio_unitario"));
                    prod.setId(rs.getInt("id_producto"));
                    prod.setCategoria(new Categoria(rs.getInt("id_categoria"), rs.getString("categoria")));
                    pedido.agregarDetalle(new DetallePedido(prod, rs.getInt("cantidad"), rs.getString("obs_det")));
                }
            }
            return new ArrayList<>(pedidos.values());
        } catch (SQLException e) {
            throw new DAOException("Error al consultar pedidos: " + e.getMessage(), e);
        }
    }

    private Integer buscarIdMesa(Connection con, int numero, String tipoPunto) throws SQLException, DAOException {
        String tipo = "BARRA".equalsIgnoreCase(tipoPunto) ? "BARRA" : "MESA";
        try (PreparedStatement ps = con.prepareStatement("SELECT id_mesa FROM mesas WHERE numero = ? AND tipo = ?")) {
            ps.setInt(1, numero);
            ps.setString(2, tipo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DAOException("La " + tipo.toLowerCase() + " " + numero + " no existe.");
                }
                return rs.getInt(1);
            }
        }
    }

    private int idEstado(Connection con, String nombre) throws SQLException, DAOException {
        try (PreparedStatement ps = con.prepareStatement("SELECT id_estado FROM estados_pedido WHERE nombre = ?")) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DAOException("Estado de pedido inválido: " + nombre);
                }
                return rs.getInt(1);
            }
        }
    }

    private void insertarHistorial(Connection con, int idPedido, int idEstado, Integer idUsuario) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO historial_estado_pedido (id_pedido, id_estado, id_usuario) VALUES (?, ?, ?)")) {
            ps.setInt(1, idPedido);
            ps.setInt(2, idEstado);
            setEntero(ps, 3, idUsuario);
            ps.executeUpdate();
        }
    }

    private void insertarLog(Connection con, String modulo, String accion, Integer idUsuario) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO logs_sistema (nivel, modulo, accion, id_usuario) VALUES ('INFO', ?, ?, ?)")) {
            ps.setString(1, modulo);
            ps.setString(2, accion);
            setEntero(ps, 3, idUsuario);
            ps.executeUpdate();
        }
    }

    /** Pone un Integer que puede ser null (SQL Server necesita el tipo explícito para los null). */
    private void setEntero(PreparedStatement ps, int indice, Integer valor) throws SQLException {
        if (valor == null) {
            ps.setNull(indice, Types.INTEGER);
        } else {
            ps.setInt(indice, valor);
        }
    }

    private String normalizarTipo(String tipoPedido) {
        return (tipoPedido != null && tipoPedido.toLowerCase().contains("llevar")) ? "LLEVAR" : "LOCAL";
    }

    private void rollback(Connection con) {
        if (con != null) {
            try {
                con.rollback();
            } catch (SQLException ignored) { }
        }
    }

    private void cerrar(Connection con) {
        if (con != null) {
            try {
                con.setAutoCommit(true);
                con.close();
            } catch (SQLException ignored) { }
        }
    }
}
