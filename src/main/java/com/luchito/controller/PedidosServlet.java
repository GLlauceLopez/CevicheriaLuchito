package com.luchito.controller;

import com.luchito.dao.PedidoDAO;
import com.luchito.model.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

@WebServlet("/pedidos")
public class PedidosServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        response.setHeader("Cache-Control", "no-store");

        HttpSession session = request.getSession(false);

        if (session == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login");
            return;
        }

        Usuario usuario;

        synchronized (session) {
            usuario = (Usuario) session.getAttribute("usuario");
        }

        if (usuario == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login");
            return;
        }

        String rol = usuario.getRol();

        if (!usuario.isActivo()
                || rol == null
                || !Set.of("Administrador", "Cocina", "Entrega")
                        .contains(rol)) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "No tienes permiso para acceder al panel."
            );
            return;
        }

        try {
            request.setAttribute(
                    "pedidos",
                    new PedidoDAO().listar()
            );

            request.setAttribute("usuario", usuario);

            request.getRequestDispatcher("/WEB-INF/views/pedidos.jsp")
                    .forward(request, response);

        } catch (SQLException error) {
            getServletContext().log(
                    "Error al consultar los pedidos.", error);

            response.sendError(
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "No se pudieron consultar los pedidos. "
                    + "Inténtalo nuevamente."
            );
        }
    }
}
