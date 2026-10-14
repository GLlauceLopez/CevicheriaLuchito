package com.luchito.dao;

import com.luchito.config.Conexion;
import com.luchito.model.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public List<Producto> listarDisponibles() throws SQLException {

        List<Producto> productos = new ArrayList<>();

        String sql = """
                SELECT id_producto, nombre, descripcion, precio,
                       stock, disponible, activo
                FROM producto
                WHERE activo = TRUE
                  AND disponible = TRUE
                  AND stock > 0
                ORDER BY id_producto
                """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {

            while (resultado.next()) {
                Producto producto = new Producto();

                producto.setIdProducto(resultado.getInt("id_producto"));
                producto.setNombre(resultado.getString("nombre"));
                producto.setDescripcion(resultado.getString("descripcion"));
                producto.setPrecio(resultado.getBigDecimal("precio"));
                producto.setStock(resultado.getInt("stock"));
                producto.setDisponible(resultado.getBoolean("disponible"));
                producto.setActivo(resultado.getBoolean("activo"));

                productos.add(producto);
            }
        }

        return productos;
    }
}