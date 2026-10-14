<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.time.format.DateTimeFormatter"%>
<%@page import="com.luchito.model.Pedido"%>
<%@page import="com.luchito.model.Usuario"%>

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

    Usuario usuario = (Usuario) request.getAttribute("usuario");

    List<Pedido> pedidos =
            (List<Pedido>) request.getAttribute("pedidos");

    DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Panel de pedidos | Luchito El Picante</title>

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
            max-width: 1100px;
            margin: 30px auto;
            padding: 0 20px;
        }

        h2, h3 {
            color: #075b9a;
        }

        .personal, .pedido, .vacio {
            margin-bottom: 20px;
            padding: 25px;
            background: white;
            border-radius: 12px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
        }

        .cabecera-pedido {
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 12px;
        }

        .cabecera-pedido h3 {
            margin: 0;
        }

        .estado {
            padding: 9px 15px;
            background: #e8f2fc;
            color: #075b9a;
            border-radius: 20px;
            font-weight: bold;
        }

        .datos {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 0 25px;
            margin-top: 15px;
        }

        .datos p {
            overflow-wrap: anywhere;
            line-height: 1.5;
        }

        .total {
            font-size: 20px;
            font-weight: bold;
            color: #075b9a;
        }

        .boton {
            display: inline-block;
            padding: 12px 18px;
            background: #075b9a;
            color: white;
            border-radius: 7px;
            text-decoration: none;
            font-weight: bold;
        }

        footer {
            padding: 20px;
            text-align: center;
            color: #526273;
        }

        @media (max-width: 600px) {
            .datos {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>

<body>
    <header>
        <h1>Luchito El Picante</h1>
        <p>Panel del personal</p>
        <a href="<%= contexto %>/">Ir al inicio</a>
    </header>

    <main>
        <section class="personal">
            <h2>Panel de pedidos</h2>

            <% if (usuario != null) { %>
                <p>
                    <strong>Usuario:</strong>
                    <%= escapar(usuario.getNombre()) %>
                </p>

                <p>
                    <strong>Rol:</strong>
                    <%= escapar(usuario.getRol()) %>
                </p>
            <% } %>

            <a class="boton" href="<%= contexto %>/pedidos">
                Actualizar lista
            </a>
        </section>

        <% if (pedidos == null || pedidos.isEmpty()) { %>

            <section class="vacio">
                <p>Todavía no hay pedidos registrados.</p>
            </section>

        <% } else { %>

            <% for (Pedido pedido : pedidos) { %>

                <article class="pedido">
                    <div class="cabecera-pedido">
                        <h3>Pedido N.º <%= pedido.getIdPedido() %></h3>

                        <span class="estado">
                            <%= escapar(pedido.getEstado()) %>
                        </span>
                    </div>

                    <div class="datos">
                        <p>
                            <strong>Cliente:</strong>
                            <%= escapar(pedido.getNombreCliente()) %>
                        </p>

                        <p>
                            <strong>Teléfono:</strong>
                            <%= escapar(pedido.getTelefonoCliente()) %>
                        </p>

                        <p>
                            <strong>Fecha:</strong>
                            <%= pedido.getFecha() == null
                                    ? ""
                                    : pedido.getFecha().format(formatoFecha) %>
                        </p>

                        <p>
                            <strong>Método de pago:</strong>
                            <%= escapar(pedido.getMetodoPago()) %>
                        </p>

                        <p>
                            <strong>Dirección de entrega:</strong>
                            <%= escapar(pedido.getDireccionEntrega()) %>
                        </p>

                        <p class="total">
                            Total: S/
                            <%= pedido.getTotal().toPlainString() %>
                        </p>
                    </div>
                </article>

            <% } %>

        <% } %>
    </main>

    <footer>
        <p>Jr. Huancarqui 1575 · Luchito El Picante</p>
    </footer>
</body>
</html>
