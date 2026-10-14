<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.UUID" %>
<%@ page import="com.luchito.model.Producto" %>

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
    String token;

    synchronized (session) {
        token = (String) session.getAttribute("csrfToken");

        if (token == null) {
            token = UUID.randomUUID().toString();
            session.setAttribute("csrfToken", token);
        }
    }

    String contexto = request.getContextPath();

    List<Producto> productos =
            (List<Producto>) request.getAttribute("productos");
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Menú | Luchito El Picante</title>

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

        nav a {
            display: inline-block;
            margin: 8px;
            color: white;
        }

        main {
            max-width: 1000px;
            margin: auto;
            padding: 30px 20px;
        }

        .productos {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
        }

        .producto {
            padding: 24px;
            background: white;
            border-radius: 14px;
            box-shadow: 0 4px 15px #17324d15;
        }

        .precio {
            font-size: 26px;
            font-weight: bold;
            color: #087eae;
        }

        input[type="number"] {
            width: 90px;
            padding: 10px;
            margin: 10px 0;
            border: 1px solid #aac5d5;
            border-radius: 6px;
            font-size: 16px;
        }

        .acciones {
            margin-top: 24px;
            padding: 24px;
            text-align: center;
            background: white;
            border-radius: 14px;
        }

        .boton {
            padding: 14px 24px;
            border: none;
            border-radius: 8px;
            background: #087eae;
            color: white;
            font-size: 16px;
            cursor: pointer;
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
        <p>Más fresco, más picante, más peruano.</p>

        <nav>
            <a href="<%= contexto %>/">Volver al inicio</a>
            <a href="<%= contexto %>/carrito">Ver carrito</a>
        </nav>
    </header>

    <main>
        <h2>Nuestro menú</h2>
        <p>
            Elige la cantidad de cada bandeja.
            Deja en 0 las que no quieras agregar.
        </p>

        <% if (productos == null || productos.isEmpty()) { %>

            <p>No hay productos disponibles por el momento.</p>

        <% } else { %>

            <form action="<%= contexto %>/carrito" method="post">

                <input type="hidden" name="csrfToken"
                       value="<%= escapar(token) %>">

                <input type="hidden" name="accion"
                       value="agregarSeleccion">

                <div class="productos">

                    <% for (Producto producto : productos) { %>

                        <article class="producto">
                            <h3>
                                <%= escapar(producto.getNombre()) %>
                            </h3>

                            <p>
                                <%= escapar(producto.getDescripcion()) %>
                            </p>

                            <p class="precio">
                                S/ <%= producto.getPrecio().toPlainString() %>
                            </p>

                            <p>
                                Disponibles: <%= producto.getStock() %>
                            </p>

                            <input type="hidden" name="idProducto"
                                   value="<%= producto.getIdProducto() %>">

                            <label for="cantidad-<%= producto.getIdProducto() %>">
                                Cantidad:
                            </label>

                            <input type="number"
                                   id="cantidad-<%= producto.getIdProducto() %>"
                                   name="cantidad_<%= producto.getIdProducto() %>"
                                   value="0"
                                   min="0"
                                   max="<%= producto.getStock() %>"
                                   step="1"
                                   required>
                        </article>

                    <% } %>

                </div>

                <div class="acciones">
                    <button class="boton" type="submit">
                        Agregar selección al carrito
                    </button>
                </div>

            </form>

        <% } %>
    </main>

    <footer>
        <p>Jr. Huancarqui 1575 · Delivery en Cercado</p>
        <p>Yape · Plin · Efectivo</p>
    </footer>
</body>
</html>