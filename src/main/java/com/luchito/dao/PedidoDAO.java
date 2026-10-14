package com.luchito.dao;

import com.luchito.config.Conexion;
import com.luchito.model.ItemCarrito;
import com.luchito.model.Pedido;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class PedidoDAO {

    public int registrar(
            String nombre,
            String telefono,
            String direccion,
            String metodoPago,
            List<ItemCarrito> carrito
    ) throws SQLException {

        nombre = nombre == null ? "" : nombre.trim();
        telefono = telefono == null ? "" : telefono.trim();
        direccion = direccion == null ? "" : direccion.trim();
        metodoPago = metodoPago == null ? "" : metodoPago.trim();

        if (nombre.isEmpty() || nombre.length() > 100) {
            throw new IllegalArgumentException(
                    "Ingresa un nombre de hasta 100 caracteres.");
        }

        if (!telefono.matches("[0-9]{9}")) {
            throw new IllegalArgumentException(
                    "El teléfono debe tener 9 dígitos.");
        }

        if (direccion.isEmpty() || direccion.length() > 255) {
            throw new IllegalArgumentException(
                    "Ingresa una dirección de hasta 255 caracteres.");
        }

        if (!Set.of("Yape", "Plin", "Efectivo").contains(metodoPago)) {
            throw new IllegalArgumentException(
                    "Selecciona un método de pago válido.");
        }

        if (carrito == null || carrito.isEmpty()) {
            throw new IllegalArgumentException("Tu carrito está vacío.");
        }

        Map<Integer, Integer> cantidades = new TreeMap<>();
        Map<Integer, BigDecimal> precios = new TreeMap<>();

        for (ItemCarrito item : carrito) {
            if (item == null
                    || item.getProducto() == null
                    || item.getProducto().getIdProducto() <= 0
                    || item.getCantidad() <= 0) {

                throw new IllegalArgumentException(
                        "El carrito contiene un producto inválido.");
            }

            int idProducto = item.getProducto().getIdProducto();

            if (cantidades.putIfAbsent(
                    idProducto, item.getCantidad()) != null) {

                throw new IllegalArgumentException(
                        "El carrito contiene productos repetidos.");
            }
        }

        try (Connection conexion = Conexion.conectar()) {
            conexion.setAutoCommit(false);

            try {
                BigDecimal total = BigDecimal.ZERO;

                String sqlProducto = """
                    SELECT nombre, precio, stock, disponible, activo
                    FROM producto
                    WHERE id_producto = ?
                    FOR UPDATE
                    """;

                try (PreparedStatement consulta =
                        conexion.prepareStatement(sqlProducto)) {

                    for (Map.Entry<Integer, Integer> entrada
                            : cantidades.entrySet()) {

                        consulta.setInt(1, entrada.getKey());

                        try (ResultSet resultado = consulta.executeQuery()) {
                            if (!resultado.next()
                                    || !resultado.getBoolean("activo")
                                    || !resultado.getBoolean("disponible")) {

                                throw new IllegalArgumentException(
                                        "Un producto ya no está disponible. "
                                        + "Revisa tu carrito.");
                            }

                            if (resultado.getInt("stock") < entrada.getValue()) {
                                throw new IllegalArgumentException(
                                        "Stock insuficiente para "
                                        + resultado.getString("nombre") + ".");
                            }

                            BigDecimal precio =
                                    resultado.getBigDecimal("precio");

                            precios.put(entrada.getKey(), precio);

                            total = total.add(precio.multiply(
                                    BigDecimal.valueOf(entrada.getValue())));
                        }
                    }
                }

                String sqlCliente = """
                    INSERT INTO cliente (nombre, telefono, direccion)
                    VALUES (?, ?, ?)
                    """;

                int idCliente;

                try (PreparedStatement consulta = conexion.prepareStatement(
                        sqlCliente, Statement.RETURN_GENERATED_KEYS)) {

                    consulta.setString(1, nombre);
                    consulta.setString(2, telefono);
                    consulta.setString(3, direccion);
                    consulta.executeUpdate();

                    idCliente = obtenerId(consulta);
                }

                String sqlPedido = """
                    INSERT INTO pedido
                        (id_cliente, estado, direccion_entrega,
                         metodo_pago, total)
                    VALUES (?, 'Recibido', ?, ?, ?)
                    """;

                int idPedido;

                try (PreparedStatement consulta = conexion.prepareStatement(
                        sqlPedido, Statement.RETURN_GENERATED_KEYS)) {

                    consulta.setInt(1, idCliente);
                    consulta.setString(2, direccion);
                    consulta.setString(3, metodoPago);
                    consulta.setBigDecimal(4, total);
                    consulta.executeUpdate();

                    idPedido = obtenerId(consulta);
                }

                String sqlDetalle = """
                    INSERT INTO detalle_pedido
                        (id_pedido, id_producto, cantidad, precio_unitario)
                    VALUES (?, ?, ?, ?)
                    """;

                String sqlStock = """
                    UPDATE producto
                    SET stock = stock - ?
                    WHERE id_producto = ? AND stock >= ?
                    """;

                try (PreparedStatement detalle =
                            conexion.prepareStatement(sqlDetalle);
                     PreparedStatement stock =
                            conexion.prepareStatement(sqlStock)) {

                    for (Map.Entry<Integer, Integer> entrada
                            : cantidades.entrySet()) {

                        int idProducto = entrada.getKey();
                        int cantidad = entrada.getValue();

                        detalle.setInt(1, idPedido);
                        detalle.setInt(2, idProducto);
                        detalle.setInt(3, cantidad);
                        detalle.setBigDecimal(4, precios.get(idProducto));
                        detalle.executeUpdate();

                        stock.setInt(1, cantidad);
                        stock.setInt(2, idProducto);
                        stock.setInt(3, cantidad);

                        if (stock.executeUpdate() != 1) {
                            throw new SQLException(
                                    "No se pudo actualizar el stock.");
                        }
                    }
                }

                conexion.commit();
                return idPedido;

            } catch (SQLException | RuntimeException error) {
                try {
                    conexion.rollback();
                } catch (SQLException errorRollback) {
                    error.addSuppressed(errorRollback);
                }

                throw error;
            }
        }
    }

    public List<Pedido> listar() throws SQLException {
        List<Pedido> pedidos = new ArrayList<>();

        String sql = """
            SELECT
                p.id_pedido,
                c.nombre AS nombre_cliente,
                c.telefono AS telefono_cliente,
                p.fecha,
                p.estado,
                p.direccion_entrega,
                p.metodo_pago,
                p.total
            FROM pedido p
            INNER JOIN cliente c ON c.id_cliente = p.id_cliente
            ORDER BY p.fecha DESC, p.id_pedido DESC
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {

            while (resultado.next()) {
                Pedido pedido = new Pedido();

                pedido.setIdPedido(resultado.getInt("id_pedido"));
                pedido.setNombreCliente(
                        resultado.getString("nombre_cliente"));
                pedido.setTelefonoCliente(
                        resultado.getString("telefono_cliente"));
                pedido.setFecha(
                        resultado.getTimestamp("fecha").toLocalDateTime());
                pedido.setEstado(resultado.getString("estado"));
                pedido.setDireccionEntrega(
                        resultado.getString("direccion_entrega"));
                pedido.setMetodoPago(resultado.getString("metodo_pago"));
                pedido.setTotal(resultado.getBigDecimal("total"));

                pedidos.add(pedido);
            }
        }

        return pedidos;
    }

    private int obtenerId(PreparedStatement consulta) throws SQLException {
        try (ResultSet claves = consulta.getGeneratedKeys()) {
            if (claves.next()) {
                return claves.getInt(1);
            }

            throw new SQLException(
                    "No se pudo obtener el número del registro.");
        }
    }
}