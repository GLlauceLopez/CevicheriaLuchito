package com.luchito.controller;

import com.luchito.config.Conexion;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/prueba-conexion")
public class PruebaConexion extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        response.setContentType("text/plain;charset=UTF-8");

        try (Connection conexion = Conexion.conectar()) {
            if (conexion.isValid(5)) {
                response.getWriter().println(
                        "¡Conexión con MySQL exitosa!");
            } else {
                response.setStatus(500);
                response.getWriter().println(
                        "No se pudo validar la conexión.");
            }
        } catch (SQLException e) {
            getServletContext().log("Error al conectar con MySQL", e);
            response.setStatus(500);
            response.getWriter().println(
                    "No se pudo conectar. Revisa la salida de Tomcat en NetBeans.");
        }
    }
}