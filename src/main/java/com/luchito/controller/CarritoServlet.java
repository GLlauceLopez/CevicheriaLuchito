package com.luchito.controller;

import com.luchito.dao.ProductoDAO;
import com.luchito.model.ItemCarrito;
import com.luchito.model.Producto;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@WebServlet("/carrito")
public class CarritoServlet extends HttpServlet {

    @SuppressWarnings("unchecked")
    private List<ItemCarrito> obtenerCarrito(HttpSession sesion) {
        List<ItemCarrito> carrito =
                (List<ItemCarrito>) sesion.getAttribute("carrito");

        if (carrito == null) {
            carrito = new ArrayList<>();
            sesion.setAttribute("carrito", carrito);
        }

        return carrito;
    }

    private Map<Integer, Producto> obtenerDisponibles()
            throws SQLException {

        Map<Integer, Producto> disponibles = new HashMap<>();

        for (Producto producto : new ProductoDAO().listarDisponibles()) {
            disponibles.put(producto.getIdProducto(), producto);
        }

        return disponibles;
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession sesion = request.getSession();

        synchronized (sesion) {
            List<ItemCarrito> carrito = obtenerCarrito(sesion);
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

            request.setAttribute("items", copia);
            request.setAttribute("total", total);
        }

        request.getRequestDispatcher("/WEB-INF/views/carrito.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession sesion = request.getSession();

        synchronized (sesion) {
            String token = (String) sesion.getAttribute("csrfToken");

            if (token == null
                    || !token.equals(request.getParameter("csrfToken"))) {

                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Formulario vencido. Recarga la página.");
                return;
            }

            List<ItemCarrito> carrito = obtenerCarrito(sesion);
            String accion = request.getParameter("accion");

            try {
                if ("vaciar".equals(accion)) {
                    carrito.clear();

                } else if ("eliminar".equals(accion)) {
                    int id = Integer.parseInt(
                            request.getParameter("idProducto"));

                    carrito.removeIf(item ->
                            item.getProducto().getIdProducto() == id);

                } else if ("actualizar".equals(accion)) {
                    int id = Integer.parseInt(
                            request.getParameter("idProducto"));

                    int cantidad = Integer.parseInt(
                            request.getParameter("cantidad"));

                    if (cantidad < 0) {
                        throw new IllegalArgumentException(
                                "La cantidad no puede ser negativa.");
                    }

                    ItemCarrito existente = null;

                    for (ItemCarrito item : carrito) {
                        if (item.getProducto().getIdProducto() == id) {
                            existente = item;
                            break;
                        }
                    }

                    if (existente == null) {
                        throw new IllegalArgumentException(
                                "El producto ya no está en el carrito.");
                    }

                    if (cantidad == 0) {
                        carrito.remove(existente);

                    } else {
                        Producto producto = obtenerDisponibles().get(id);

                        if (producto == null) {
                            throw new IllegalArgumentException(
                                    "El producto ya no está disponible.");
                        }

                        if (cantidad > producto.getStock()) {
                            throw new IllegalArgumentException(
                                    "La cantidad supera el stock disponible.");
                        }

                        int posicion = carrito.indexOf(existente);

                        carrito.set(
                                posicion,
                                new ItemCarrito(producto, cantidad));
                    }

                } else if ("agregarSeleccion".equals(accion)
                        || "agregar".equals(accion)) {

                    Map<Integer, Integer> seleccion =
                            leerSeleccion(request, accion);

                    if (seleccion.isEmpty()) {
                        throw new IllegalArgumentException(
                                "Selecciona al menos una bandeja.");
                    }

                    Map<Integer, Producto> disponibles =
                            obtenerDisponibles();

                    Map<Integer, ItemCarrito> nuevosItems =
                            new LinkedHashMap<>();

                    for (ItemCarrito item : carrito) {
                        nuevosItems.put(
                                item.getProducto().getIdProducto(),
                                new ItemCarrito(
                                        item.getProducto(),
                                        item.getCantidad()));
                    }

                    for (Map.Entry<Integer, Integer> entrada :
                            seleccion.entrySet()) {

                        int id = entrada.getKey();
                        Producto producto = disponibles.get(id);

                        if (producto == null) {
                            throw new IllegalArgumentException(
                                    "Una bandeja seleccionada ya no está disponible.");
                        }

                        ItemCarrito existente = nuevosItems.get(id);
                        long cantidadTotal = entrada.getValue();

                        if (existente != null) {
                            cantidadTotal += existente.getCantidad();
                        }

                        if (cantidadTotal > producto.getStock()) {
                            throw new IllegalArgumentException(
                                    "Stock insuficiente para "
                                    + producto.getNombre()
                                    + ". Disponibles: "
                                    + producto.getStock()
                                    + ".");
                        }

                        nuevosItems.put(
                                id,
                                new ItemCarrito(
                                        producto, (int) cantidadTotal));
                    }

                    // Guarda los cambios solo si toda la selección es válida.
                    carrito.clear();
                    carrito.addAll(nuevosItems.values());

                } else {
                    throw new IllegalArgumentException(
                            "Acción no válida.");
                }

            } catch (NumberFormatException e) {
                sesion.setAttribute(
                        "errorCarrito",
                        "Ingresa cantidades enteras válidas.");

            } catch (IllegalArgumentException e) {
                sesion.setAttribute(
                        "errorCarrito", e.getMessage());

            } catch (SQLException e) {
                getServletContext().log(
                        "Error al consultar productos para el carrito", e);

                sesion.setAttribute(
                        "errorCarrito",
                        "No se pudieron consultar los productos. Intenta nuevamente.");
            }
        }

        response.sendRedirect(response.encodeRedirectURL(
                request.getContextPath() + "/carrito"));
    }

    private Map<Integer, Integer> leerSeleccion(
            HttpServletRequest request, String accion) {

        Map<Integer, Integer> seleccion = new LinkedHashMap<>();

        if ("agregar".equals(accion)) {
            int id = Integer.parseInt(
                    request.getParameter("idProducto"));

            int cantidad = Integer.parseInt(
                    request.getParameter("cantidad"));

            if (id <= 0 || cantidad <= 0) {
                throw new IllegalArgumentException(
                        "Selecciona un producto y una cantidad válida.");
            }

            seleccion.put(id, cantidad);
            return seleccion;
        }

        String[] ids = request.getParameterValues("idProducto");

        if (ids == null || ids.length == 0) {
            throw new IllegalArgumentException(
                    "No se recibieron productos.");
        }

        for (String textoId : ids) {
            int id = Integer.parseInt(textoId);

            if (id <= 0) {
                throw new IllegalArgumentException(
                        "Producto no válido.");
            }

            int cantidad = Integer.parseInt(
                    request.getParameter("cantidad_" + id));

            if (cantidad < 0) {
                throw new IllegalArgumentException(
                        "Las cantidades no pueden ser negativas.");
            }

            if (cantidad > 0) {
                if (seleccion.containsKey(id)) {
                    throw new IllegalArgumentException(
                            "Producto repetido en la selección.");
                }

                seleccion.put(id, cantidad);
            }
        }

        return seleccion;
    }
}