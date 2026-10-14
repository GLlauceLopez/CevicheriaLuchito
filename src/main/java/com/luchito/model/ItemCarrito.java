package com.luchito.model;

import java.math.BigDecimal;

public class ItemCarrito {

    private final Producto producto;
    private int cantidad;

    public ItemCarrito(Producto producto, int cantidad) {
        if (producto == null) {
            throw new IllegalArgumentException(
                    "El producto es obligatorio.");
        }

        this.producto = producto;
        setCantidad(cantidad);
    }

    public Producto getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero.");
        }

        this.cantidad = cantidad;
    }

    public BigDecimal getSubtotal() {
        return producto.getPrecio()
                .multiply(BigDecimal.valueOf(cantidad));
    }
}