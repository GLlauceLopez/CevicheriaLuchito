<%@page contentType="text/html" pageEncoding="UTF-8"%>

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
    String error = (String) request.getAttribute("error");
    String correo = (String) request.getAttribute("correo");
    String loginToken = (String) request.getAttribute("loginToken");
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Acceso del personal | Luchito El Picante</title>

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

        main {
            max-width: 460px;
            margin: 40px auto;
            padding: 0 20px;
        }

        .panel {
            padding: 30px;
            background: white;
            border-radius: 12px;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
        }

        h2 {
            margin-top: 0;
            color: #075b9a;
        }

        label {
            display: block;
            margin: 20px 0 8px;
            font-weight: bold;
        }

        input {
            width: 100%;
            padding: 12px;
            border: 1px solid #b9c8d6;
            border-radius: 7px;
            font-size: 16px;
        }

        input:focus {
            outline: 2px solid #075b9a;
            outline-offset: 2px;
        }

        button {
            width: 100%;
            margin-top: 25px;
            padding: 13px;
            border: none;
            border-radius: 7px;
            background: #075b9a;
            color: white;
            font-size: 16px;
            font-weight: bold;
            cursor: pointer;
        }

        .error {
            padding: 12px;
            background: #ffe8e8;
            color: #9b2020;
            border-radius: 7px;
        }

        .nota {
            color: #526273;
            line-height: 1.5;
        }

        .volver {
            display: block;
            margin-top: 20px;
            text-align: center;
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
    </header>

    <main>
        <section class="panel">
            <h2>Acceso del personal</h2>

            <p class="nota">
                Ingresa con tu cuenta para acceder al panel de pedidos.
            </p>

            <% if (error != null) { %>
                <p class="error" role="alert">
                    <%= escapar(error) %>
                </p>
            <% } %>

            <form action="<%= contexto %>/login" method="post">
                <input type="hidden"
                       name="loginToken"
                       value="<%= escapar(loginToken) %>">

                <label for="correo">Correo electrónico</label>

                <input type="email"
                       id="correo"
                       name="correo"
                       value="<%= escapar(correo) %>"
                       maxlength="150"
                       autocomplete="username"
                       required>

                <label for="clave">Contraseña</label>

                <input type="password"
                       id="clave"
                       name="clave"
                       maxlength="128"
                       autocomplete="current-password"
                       required>

                <button type="submit">Ingresar</button>
            </form>

            <a class="volver" href="<%= contexto %>/">
                Volver al inicio
            </a>
        </section>
    </main>

    <footer>
        <p>Jr. Huancarqui 1575 · Delivery en Cercado</p>
    </footer>
</body>
</html>
