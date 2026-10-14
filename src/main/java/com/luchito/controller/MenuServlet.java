package com.luchito.controller;

import com.luchito.dao.ProductoDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/menu")
public class MenuServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        try {
            ProductoDAO productoDAO = new ProductoDAO();

            request.setAttribute(
                    "productos", productoDAO.listarDisponibles());

            request.getRequestDispatcher("/WEB-INF/views/menu.jsp")
                    .forward(request, response);

        } catch (SQLException e) {
            getServletContext().log("Error al consultar el menú", e);

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().println(
                    "No se pudo cargar el menú. Revisa el registro de Tomcat.");
        }
    }
}