<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="com.luchito.model.ItemCarrito"%>

<%!
    private String escapar(String texto) {
        if (texto == null) {
            return "";
        }

        return texto.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
%>

<%
    String contexto = request.getContextPath();

    List<ItemCarrito> items =
        (List<ItemCarrito>) request.getAttribute("items");

    BigDecimal total =
        (BigDecimal) request.getAttribute("total");

    if (total == null) {
        total = BigDecimal.ZERO;
    }

    String csrfToken;
    String error;

    synchronized (session) {
        csrfToken = (String) session.getAttribute("csrfToken");
        error = (String) session.getAttribute("errorCarrito");
        session.removeAttribute("errorCarrito");
    }
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Carrito | Luchito El Picante</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: #f2f6fa;
            color: #243447;
        }

        header {
            padding: 25px 20px;
            background: #075b9a;
            color: white;
            text-align: center;
        }

        header h1 {
            margin: 0 0 10px;
        }

        header a {
            color: white;
        }

        main {
            max-width: 850px;
            margin: 30px auto;
            padding: 0 20px;
        }

        h2, h3 {
            color: #075b9a;
        }

        .producto, .resumen, .vacio {
            margin-bottom: 20px;
            padding: 25px;
            background: white;
            border-radius: 12px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
        }

        .error {
            padding: 15px;
            margin-bottom: 20px;
            border-radius: 8px;
            background: #ffe8e8;
            color: #9b2020;
        }

        .controles {
            display: flex;
            align-items: center;
            flex-wrap: wrap;
            gap: 10px;
            margin-top: 10px;
        }

        input[type="number"] {
            width: 90px;
            padding: 10px;
            border: 1px solid #b9c8d6;
            border-radius: 6px;
            text-align: center;
            font-size: 16px;
        }

        button, .boton {
            display: inline-block;
            padding: 12px 18px;
            border: none;
            border-radius: 7px;
            background: #075b9a;
            color: white;
            font-size: 15px;
            font-weight: bold;
            text-decoration: none;
            cursor: pointer;
        }

        .cambio {
            min-width: 42px;
            background: #e8f2fc;
            color: #075b9a;
            font-size: 20px;
        }

        .quitar {
            background: #b83232;
        }

        .form-quitar {
            margin-top: 15px;
        }

        .nota {
            color: #526273;
            line-height: 1.5;
            font-size: 14px;
        }

        .total {
            font-size: 24px;
            font-weight: bold;
        }

        .acciones {
            display: flex;
            align-items: center;
            flex-wrap: wrap;
            gap: 12px;
            margin-top: 20px;
        }

        .continuar {
            background: #187443;
        }

        .secundario {
            background: #e8f2fc;
            color: #075b9a;
        }

        footer {
            padding: 20px;
            text-align: center;
            color: #526273;
        }
    </style>
</head>

<body>
    <header>
        <h1>Luchito El Picante</h1>
        <p>Más fresco, más picante, más peruano.</p>
        <a href="<%= contexto %>/menu">Volver al menú</a>
    </header>

    <main>
        <h2>Tu carrito</h2>

        <% if (error != null) { %>
            <p class="error" role="alert"><%= escapar(error) %></p>
        <% } %>

        <% if (items == null || items.isEmpty()) { %>

            <section class="vacio">
                <p>Tu carrito está vacío.</p>

                <a class="boton" href="<%= contexto %>/menu">
                    Ver menú
                </a>
            </section>

        <% } else { %>

            <% for (ItemCarrito item : items) {
                int id = item.getProducto().getIdProducto();
            %>

                <article class="producto">
                    <h3><%= escapar(item.getProducto().getNombre()) %></h3>

                    <p>
                        Precio por unidad:
                        S/ <%= item.getProducto().getPrecio().toPlainString() %>
                    </p>

                    <p>
                        Subtotal:
                        <strong>S/ <%= item.getSubtotal().toPlainString() %></strong>
                    </p>

                    <form action="<%= contexto %>/carrito" method="post">
                        <input type="hidden" name="csrfToken"
                               value="<%= escapar(csrfToken) %>">

                        <input type="hidden" name="accion" value="actualizar">

                        <input type="hidden" name="idProducto"
                               value="<%= id %>">

                        <label for="cantidad-<%= id %>">Cantidad:</label>

                        <div class="controles">
                            <button type="button" class="cambio"
                                    data-cambio="-1"
                                    aria-label="Disminuir cantidad">
                                −
                            </button>

                            <input type="number"
                                   class="cantidad"
                                   id="cantidad-<%= id %>"
                                   name="cantidad"
                                   value="<%= item.getCantidad() %>"
                                   min="0"
                                   step="1"
                                   required>

                            <button type="button" class="cambio"
                                    data-cambio="1"
                                    aria-label="Aumentar cantidad">
                                +
                            </button>

                            <button type="submit">Actualizar</button>
                        </div>

                        <p class="nota">
                            Pulsa Actualizar para guardar el cambio.
                            Si colocas 0, se quitará el producto.
                        </p>
                    </form>

                    <form action="<%= contexto %>/carrito"
                          method="post" class="form-quitar">

                        <input type="hidden" name="csrfToken"
                               value="<%= escapar(csrfToken) %>">

                        <input type="hidden" name="accion" value="eliminar">

                        <input type="hidden" name="idProducto"
                               value="<%= id %>">

                        <button type="submit" class="quitar">
                            Quitar producto
                        </button>
                    </form>
                </article>

            <% } %>

            <section class="resumen">
                <h2>Resumen</h2>

                <p class="total">
                    Total estimado: S/ <%= total.toPlainString() %>
                </p>

                <p class="nota">
                    El stock y los precios vigentes se verificarán
                    al confirmar el pedido.
                    Si cambias cantidades, pulsa Actualizar antes de continuar.
                </p>

                <div class="acciones">
                    <a class="boton continuar"
                       href="<%= contexto %>/pedido">
                        Continuar con el pedido
                    </a>

                    <a class="boton secundario"
                       href="<%= contexto %>/menu">
                        Seguir comprando
                    </a>

                    <form action="<%= contexto %>/carrito" method="post">
                        <input type="hidden" name="csrfToken"
                               value="<%= escapar(csrfToken) %>">

                        <input type="hidden" name="accion" value="vaciar">

                        <button type="submit" class="quitar">
                            Vaciar carrito
                        </button>
                    </form>
                </div>
            </section>

        <% } %>
    </main>

    <footer>
        <p>Jr. Huancarqui 1575 · Delivery en Cercado</p>
        <p>Yape · Plin · Efectivo</p>
    </footer>

    <script>
        document.querySelectorAll("[data-cambio]").forEach(function (boton) {
            boton.addEventListener("click", function () {
                const controles = boton.closest(".controles");
                const entrada = controles.querySelector(".cantidad");

                const cantidad = Number(entrada.value);
                const cambio = Number(boton.dataset.cambio);

                if (!Number.isSafeInteger(cantidad) || cantidad < 0) {
                    entrada.reportValidity();
                    return;
                }

                const nuevaCantidad = Math.max(0, cantidad + cambio);

                if (Number.isSafeInteger(nuevaCantidad)) {
                    entrada.value = nuevaCantidad;
                }
            });
        });
    </script>
</body>
</html>