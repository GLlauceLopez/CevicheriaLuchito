USE luchito_el_picante;

START TRANSACTION;

-- Devolver los productos del pedido al stock
UPDATE producto AS p
JOIN detalle_pedido AS d
    ON p.id_producto = d.id_producto
SET p.stock = p.stock + d.cantidad
WHERE d.id_pedido = 1;

-- Eliminar el detalle del pedido
DELETE FROM detalle_pedido
WHERE id_pedido = 1;

-- Eliminar el pedido
DELETE FROM pedido
WHERE id_pedido = 1;

COMMIT;