CREATE DATABASE IF NOT EXISTS luchito_el_picante
    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE luchito_el_picante;

CREATE TABLE cliente (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    direccion VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL,
    clave_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_usuario_correo UNIQUE (correo),
    CONSTRAINT ck_usuario_rol CHECK (rol IN ('Administrador', 'Cocina', 'Entrega')),
    CONSTRAINT ck_usuario_activo CHECK (activo IN (0, 1))
) ENGINE=InnoDB;

CREATE TABLE producto (
    id_producto INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario_creador INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    disponible BOOLEAN NOT NULL DEFAULT TRUE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_producto_usuario FOREIGN KEY (id_usuario_creador)
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_producto_precio CHECK (precio >= 0),
    CONSTRAINT ck_producto_stock CHECK (stock >= 0),
    CONSTRAINT ck_producto_disponible CHECK (disponible IN (0, 1)),
    CONSTRAINT ck_producto_activo CHECK (activo IN (0, 1))
) ENGINE=InnoDB;

CREATE TABLE pedido (
    id_pedido INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) NOT NULL DEFAULT 'Recibido',
    direccion_entrega VARCHAR(255) NOT NULL,
    metodo_pago VARCHAR(15) NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (id_cliente)
        REFERENCES cliente (id_cliente) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_pedido_estado CHECK (
        estado IN ('Recibido', 'En preparación', 'Listo', 'Entregado')),
    CONSTRAINT ck_pedido_metodo_pago CHECK (metodo_pago IN ('Yape', 'Plin', 'Efectivo')),
    CONSTRAINT ck_pedido_total CHECK (total >= 0),
    INDEX idx_pedido_fecha (fecha),
    INDEX idx_pedido_estado (estado)
) ENGINE=InnoDB;

CREATE TABLE detalle_pedido (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    id_pedido INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (id_pedido)
        REFERENCES pedido (id_pedido) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto)
        REFERENCES producto (id_producto) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_detalle_precio CHECK (precio_unitario >= 0),
    CONSTRAINT uq_detalle_producto UNIQUE (id_pedido, id_producto)
) ENGINE=InnoDB;

