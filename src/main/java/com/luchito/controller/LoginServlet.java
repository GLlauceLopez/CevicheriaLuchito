package com.luchito.controller;

import com.luchito.dao.UsuarioDAO;
import com.luchito.model.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        response.setHeader("Cache-Control", "no-store");

        HttpSession session = request.getSession();

        synchronized (session) {
            if (session.getAttribute("usuario") != null) {
                response.sendRedirect(
                        request.getContextPath() + "/pedidos");
                return;
            }

            if (session.getAttribute("loginToken") == null) {
                session.setAttribute(
                        "loginToken",
                        UUID.randomUUID().toString()
                );
            }

            request.setAttribute(
                    "loginToken",
                    session.getAttribute("loginToken")
            );
        }

        request.getRequestDispatcher("/WEB-INF/views/login.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");

        HttpSession session = request.getSession(false);

        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String correo = request.getParameter("correo");
        String clave = request.getParameter("clave");

        synchronized (session) {
            String tokenGuardado =
                    (String) session.getAttribute("loginToken");

            String tokenRecibido = request.getParameter("loginToken");

            if (tokenGuardado == null
                    || !tokenGuardado.equals(tokenRecibido)) {

                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "El formulario venció. Vuelve a abrir el inicio de sesión."
                );
                return;
            }

            try {
                Usuario usuario =
                        new UsuarioDAO().autenticar(correo, clave);

                if (usuario != null) {
                    request.changeSessionId();

                    session.setAttribute("usuario", usuario);
                    session.removeAttribute("loginToken");

                    session.setAttribute(
                            "csrfToken",
                            UUID.randomUUID().toString()
                    );

                    response.sendRedirect(
                            request.getContextPath() + "/pedidos");
                    return;
                }

                request.setAttribute(
                        "error",
                        "Correo o contraseña incorrectos, o cuenta inactiva."
                );

            } catch (SQLException error) {
                getServletContext().log(
                        "Error al iniciar sesión.", error);

                response.setStatus(
                        HttpServletResponse.SC_SERVICE_UNAVAILABLE);

                request.setAttribute(
                        "error",
                        "No se pudo iniciar sesión. Inténtalo nuevamente."
                );
            }

            String nuevoToken = UUID.randomUUID().toString();

            session.setAttribute("loginToken", nuevoToken);
            request.setAttribute("loginToken", nuevoToken);
        }

        request.setAttribute(
                "correo",
                correo == null ? "" : correo.trim()
        );

        request.getRequestDispatcher("/WEB-INF/views/login.jsp")
                .forward(request, response);
    }
}