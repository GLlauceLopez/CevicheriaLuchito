<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="com.luchito.model.ItemCarrito" %>

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

    String error = (String) request.getAttribute("errorPedido");
    String csrfToken = (String) session.getAttribute("csrfToken");
    String pedidoToken = (String) session.getAttribute("pedidoToken");
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Confirmar pedido | Luchito El Picante</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: #f0f9ff;
            color: #17324d;
        }

        header {
            padding: 30px 20px;
            text-align: center;
            background: #087eae;
            color: white;
        }

        header a {
            color: white;
        }

        main {
            max-width: 800px;
            margin: auto;
            padding: 30px 20px;
        }

        .panel {
            margin-bottom: 20px;
            padding: 24px;
            background: white;
            border-radius: 14px;
            box-shadow: 0 4px 15px #17324d15;
        }

        .producto {
            padding: 12px 0;
            border-bottom: 1px solid #dceaf2;
        }

        .total {
            font-size: 26px;
            font-weight: bold;
            color: #087eae;
        }

        label {
            display: block;
            margin: 18px 0 8px;
            font-weight: bold;
        }

        input, textarea, select {
            width: 100%;
            padding: 12px;
            border: 1px solid #aac5d5;
            border-radius: 8px;
            font-family: inherit;
            font-size: 16px;
        }

        textarea {
            min-height: 100px;
            resize: vertical;
        }

        .boton {
            width: 100%;
            margin-top: 24px;
            padding: 14px;
            border: none;
            border-radius: 8px;
            background: #087eae;
            color: white;
            font-size: 16px;
            cursor: pointer;
        }

        .error {
            padding: 15px;
            background: #ffe8e8;
            color: #9b1c1c;
            border-radius: 8px;
        }

        footer {
            padding: 25px;
            text-align: center;
        }
    </style>
</head>

<body>
    <header>
        <h1>Luchito El Picante</h1>
        <p>Completa tu pedido</p>
        <a href="<%= contexto %>/carrito">Volver al carrito</a>
    </header>

    <main>
        <% if (error != null) { %>
            <p class="error" role="alert">
                <%= escapar(error) %>
            </p>
        <% } %>

        <section class="panel">
            <h2>Resumen del pedido</h2>

            <% if (items != null) {
                for (ItemCarrito item : items) {
            %>
                <div class="producto">
                    <p>
                        <strong>
                            <%= escapar(item.getProducto().getNombre()) %>
                        </strong>
                    </p>

                    <p>
                        Cantidad: <%= item.getCantidad() %>
                        · Subtotal:
                        S/ <%= item.getSubtotal().toPlainString() %>
                    </p>
                </div>
            <%
                }
            }
            %>

            <p class="total">
                Total estimado:
                S/ <%= total == null ? "0.00" : total.toPlainString() %>
            </p>

            <p>
                Al confirmar se verifican el stock y los precios vigentes.
            </p>
        </section>

        <section class="panel">
            <h2>Datos para la entrega</h2>
            <p>Delivery en Cercado de Lima.</p>

            <form action="<%= contexto %>/pedido" method="post">
                <input type="hidden" name="csrfToken"
                       value="<%= escapar(csrfToken) %>">

                <input type="hidden" name="pedidoToken"
                       value="<%= escapar(pedidoToken) %>">

                <label for="nombre">Nombre completo</label>
                <input type="text"
                       id="nombre"
                       name="nombre"
                       maxlength="100"
                       autocomplete="name"
                       required>

                <label for="telefono">Teléfono</label>
                <input type="tel"
                       id="telefono"
                       name="telefono"
                       pattern="[0-9]{9}"
                       minlength="9"
                       maxlength="9"
                       inputmode="numeric"
                       autocomplete="tel-national"
                       title="Ingresa 9 dígitos, sin espacios."
                       placeholder="Ejemplo: 987654321"
                       required>

                <label for="direccion">Dirección de entrega</label>
                <textarea id="direccion"
                          name="direccion"
                          maxlength="255"
                          autocomplete="street-address"
                          placeholder="Calle, número y referencia"
                          required></textarea>

                <label for="metodoPago">Método de pago</label>
                <select id="metodoPago" name="metodoPago" required>
                    <option value="">Selecciona una opción</option>
                    <option value="Yape">Yape</option>
                    <option value="Plin">Plin</option>
                    <option value="Efectivo">Efectivo</option>
                </select>

                <p>
                    Seleccionar el método de pago no realiza un cobro.
                    El pago se coordina con la cevichería.
                </p>

                <button class="boton" type="submit">
                    Confirmar pedido
                </button>
            </form>
        </section>
    </main>

    <footer>
        <p>Jr. Huancarqui 1575 · Luchito El Picante</p>
        <p>Más fresco, más picante, más peruano.</p>
    </footer>
</body>
</html>