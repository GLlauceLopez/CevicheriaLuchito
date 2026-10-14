<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String contexto = request.getContextPath();
    Integer idPedido = (Integer) request.getAttribute("idPedido");

    if (idPedido == null) {
        response.sendRedirect(contexto + "/carrito");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Pedido registrado | Luchito El Picante</title>

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
            max-width: 700px;
            margin: 40px auto;
            padding: 0 20px;
        }

        .confirmacion {
            padding: 35px 25px;
            background: white;
            border-radius: 15px;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
            text-align: center;
        }

        .simbolo {
            display: inline-block;
            padding: 15px 22px;
            border-radius: 50%;
            background: #e4f5eb;
            color: #187443;
            font-size: 35px;
        }

        h2 {
            color: #075b9a;
        }

        .numero {
            font-size: 24px;
            font-weight: bold;
        }

        .estado {
            display: inline-block;
            padding: 10px 18px;
            background: #e8f2fc;
            color: #075b9a;
            border-radius: 20px;
            font-weight: bold;
        }

        .nota {
            margin-top: 20px;
            line-height: 1.6;
            color: #526273;
        }

        .acciones {
            display: flex;
            justify-content: center;
            flex-wrap: wrap;
            gap: 12px;
            margin-top: 25px;
        }

        .boton {
            display: inline-block;
            padding: 13px 20px;
            background: #075b9a;
            color: white;
            text-decoration: none;
            border-radius: 8px;
            font-weight: bold;
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
    </header>

    <main>
        <section class="confirmacion">
            <div class="simbolo" aria-hidden="true">✓</div>

            <h2>¡Tu pedido fue registrado!</h2>

            <p class="numero">Pedido N.º <%= idPedido %></p>

            <p>Estado al registrar:</p>
            <span class="estado">Recibido</span>

            <p class="nota">
                Tu pedido se guardó correctamente.
                El pago y la entrega se coordinarán contigo.
                Esta confirmación no acredita un pago.
            </p>

            <div class="acciones">
                <a class="boton" href="<%= contexto %>/menu">
                    Volver al menú
                </a>

                <a class="boton secundario" href="<%= contexto %>/">
                    Ir al inicio
                </a>
            </div>
        </section>
    </main>

    <footer>
        <p>Jr. Huancarqui 1575 · Delivery en Cercado</p>
        <p>Yape · Plin · Efectivo</p>
    </footer>

</body>
</html>