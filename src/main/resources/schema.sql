CREATE TABLE IF NOT EXISTS usuario (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre_usuario VARCHAR(50) NOT NULL UNIQUE,
    correo VARCHAR(150) NOT NULL UNIQUE,
    clave VARCHAR(255) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    creado_en DATETIME NOT NULL,
    habilitado BOOLEAN NOT NULL
);
CREATE TABLE IF NOT EXISTS categoria (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    habilitada BOOLEAN NOT NULL
);
CREATE TABLE IF NOT EXISTS marca (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    habilitada BOOLEAN NOT NULL
);
CREATE TABLE IF NOT EXISTS especie (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL UNIQUE
);
CREATE TABLE IF NOT EXISTS producto (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(1000),
    precio DECIMAL(12,2) NOT NULL,
    stock INT NOT NULL,
    publicado_en DATETIME NOT NULL,
    disponible BOOLEAN NOT NULL,
    categoria_id BIGINT NOT NULL,
    marca_id BIGINT,
    creador_id BIGINT NOT NULL,
    CONSTRAINT producto_categoria_fk FOREIGN KEY (categoria_id) REFERENCES categoria(id),
    CONSTRAINT producto_marca_fk FOREIGN KEY (marca_id) REFERENCES marca(id),
    CONSTRAINT producto_creador_fk FOREIGN KEY (creador_id) REFERENCES usuario(id)
);
CREATE TABLE IF NOT EXISTS imagen_producto (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ubicacion VARCHAR(1000) NOT NULL,
    posicion INT NOT NULL,
    principal BOOLEAN NOT NULL,
    producto_id BIGINT NOT NULL,
    CONSTRAINT imagen_producto_fk FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS producto_especie (
    producto_id BIGINT NOT NULL,
    especie_id BIGINT NOT NULL,
    PRIMARY KEY (producto_id, especie_id),
    CONSTRAINT producto_especie_producto_fk FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE CASCADE,
    CONSTRAINT producto_especie_especie_fk FOREIGN KEY (especie_id) REFERENCES especie(id)
);
CREATE TABLE IF NOT EXISTS carrito (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL UNIQUE,
    creado_en DATETIME NOT NULL,
    actualizado_en DATETIME NOT NULL,
    CONSTRAINT carrito_usuario_fk FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS item_carrito (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    carrito_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    agregado_en DATETIME NOT NULL,
    CONSTRAINT item_carrito_fk FOREIGN KEY (carrito_id) REFERENCES carrito(id) ON DELETE CASCADE,
    CONSTRAINT item_producto_fk FOREIGN KEY (producto_id) REFERENCES producto(id)
);
CREATE TABLE IF NOT EXISTS compra (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    realizada_en DATETIME NOT NULL,
    total DECIMAL(12,2) NOT NULL,
    CONSTRAINT compra_usuario_fk FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);
CREATE TABLE IF NOT EXISTS detalle_compra (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    compra_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio DECIMAL(12,2) NOT NULL,
    CONSTRAINT detalle_compra_fk FOREIGN KEY (compra_id) REFERENCES compra(id) ON DELETE CASCADE,
    CONSTRAINT detalle_producto_fk FOREIGN KEY (producto_id) REFERENCES producto(id)
);
