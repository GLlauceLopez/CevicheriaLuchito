package com.luchito.controller;

import com.luchito.dao.PedidoDAO;
import com.luchito.model.ItemCarrito;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@WebServlet("/pedido")
public class PedidoServlet extends HttpServlet {

    @SuppressWarnings("unchecked")
    private List<ItemCarrito> obtenerCarrito(HttpSession sesion) {
        return (List<ItemCarrito>) sesion.getAttribute("carrito");
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession sesion = request.getSession();
        boolean mostrarConfirmacion = false;
        boolean carritoVacio = false;

        synchronized (sesion) {
            Object ultimoPedido = sesion.getAttribute("ultimoPedido");

            if ("confirmado".equals(request.getParameter("estado"))
                    && ultimoPedido != null) {

                request.setAttribute("idPedido", ultimoPedido);
                mostrarConfirmacion = true;

            } else {
                List<ItemCarrito> carrito = obtenerCarrito(sesion);

                if (carrito == null || carrito.isEmpty()) {
                    carritoVacio = true;

                } else {
                    List<ItemCarrito> copia = new ArrayList<>();
                    BigDecimal total = BigDecimal.ZERO;

                    for (ItemCarrito item : carrito) {
                        copia.add(new ItemCarrito(
                                item.getProducto(), item.getCantidad()));

                        total = total.add(item.getSubtotal());
                    }

                    if (sesion.getAttribute("csrfToken") == null) {
                        sesion.setAttribute(
                                "csrfToken", UUID.randomUUID().toString());
                    }

                    if (sesion.getAttribute("pedidoToken") == null) {
                        sesion.setAttribute(
                                "pedidoToken", UUID.randomUUID().toString());
                    }

                    request.setAttribute("items", copia);
                    request.setAttribute("total", total);
                    request.setAttribute(
                            "errorPedido", sesion.getAttribute("errorPedido"));

                    sesion.removeAttribute("errorPedido");
                }
            }
        }

        if (mostrarConfirmacion) {
            request.getRequestDispatcher(
                    "/WEB-INF/views/confirmacion.jsp")
                    .forward(request, response);

        } else if (carritoVacio) {
            response.sendRedirect(response.encodeRedirectURL(
                    request.getContextPath() + "/carrito"));

        } else {
            request.getRequestDispatcher("/WEB-INF/views/pedido.jsp")
                    .forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession sesion = request.getSession();
        boolean confirmado = false;

        synchronized (sesion) {
            String csrfToken =
                    (String) sesion.getAttribute("csrfToken");

            if (csrfToken == null
                    || !csrfToken.equals(request.getParameter("csrfToken"))) {

                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Formulario vencido. Recarga la página.");
                return;
            }

            String pedidoToken =
                    (String) sesion.getAttribute("pedidoToken");

            if (pedidoToken == null
                    || !pedidoToken.equals(
                            request.getParameter("pedidoToken"))) {

                sesion.setAttribute(
                        "errorPedido",
                        "Este formulario ya fue utilizado o venció. Revisa tu carrito.");

            } else {
                List<ItemCarrito> carrito = obtenerCarrito(sesion);

                try {
                    int idPedido = new PedidoDAO().registrar(
                            request.getParameter("nombre"),
                            request.getParameter("telefono"),
                            request.getParameter("direccion"),
                            request.getParameter("metodoPago"),
                            carrito);

                    sesion.setAttribute("ultimoPedido", idPedido);
                    sesion.removeAttribute("carrito");
                    sesion.removeAttribute("pedidoToken");
                    sesion.removeAttribute("errorPedido");
                    confirmado = true;

                } catch (IllegalArgumentException e) {
                    sesion.setAttribute(
                            "errorPedido", e.getMessage());

                } catch (SQLException e) {
                    getServletContext().log(
                            "Error al registrar el pedido", e);

                    sesion.setAttribute(
                            "errorPedido",
                            "No se pudo registrar el pedido. Intenta nuevamente.");
                }
            }
        }

        String destino = confirmado
                ? "/pedido?estado=confirmado"
                : "/pedido";

        response.sendRedirect(response.encodeRedirectURL(
                request.getContextPath() + destino));
    }
}