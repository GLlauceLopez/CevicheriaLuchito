package com.luchito.dao;

import com.luchito.config.Conexion;
import com.luchito.config.Seguridad;
import com.luchito.model.Usuario;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

public class UsuarioDAO {

    public Usuario autenticar(String correo, String clave)
            throws SQLException {

        if (correo == null
                || clave == null
                || correo.trim().isEmpty()
                || correo.trim().length() > 150
                || clave.isEmpty()
                || clave.length() > 128) {
            return null;
        }

        String sql = """
            SELECT
                id_usuario,
                nombre,
                correo,
                clave_hash,
                rol,
                activo
            FROM usuario
            WHERE correo = ? AND activo = TRUE
            """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {

            consulta.setString(1, correo.trim());

            try (ResultSet resultado = consulta.executeQuery()) {
                if (!resultado.next()) {
                    return null;
                }

                String rol = resultado.getString("rol");

                if (!Set.of("Administrador", "Cocina", "Entrega")
                        .contains(rol == null ? "" : rol)) {
                    return null;
                }

                boolean claveCorrecta;

                try {
                    claveCorrecta = Seguridad.verificar(
                            clave,
                            resultado.getString("clave_hash")
                    );
                } catch (GeneralSecurityException error) {
                    throw new SQLException(
                            "No se pudo comprobar la contraseña.", error);
                }

                if (!claveCorrecta) {
                    return null;
                }

                Usuario usuario = new Usuario();

                usuario.setIdUsuario(
                        resultado.getInt("id_usuario"));
                usuario.setNombre(
                        resultado.getString("nombre"));
                usuario.setCorreo(
                        resultado.getString("correo"));
                usuario.setRol(rol);
                usuario.setActivo(
                        resultado.getBoolean("activo"));

                return usuario;
            }
        }
    }
}