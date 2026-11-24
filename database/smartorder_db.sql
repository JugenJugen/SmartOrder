/* =====================================================================
   SmartOrder - Script de creación de la base de datos (SQL Server)
   Avance 2: Base de datos y su conexión mediante JDBC
   Autor: Elian Rojas
   ===================================================================== */

USE master;
GO

IF DB_ID('smartorder_db') IS NOT NULL
BEGIN
    ALTER DATABASE smartorder_db SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE smartorder_db;
END
GO

CREATE DATABASE smartorder_db;
GO

USE smartorder_db;
GO

/* 1. Roles (Administrador, Cocina, Atención) */
CREATE TABLE roles (
    id_rol      INT IDENTITY(1,1) PRIMARY KEY,
    nombre      NVARCHAR(30)  NOT NULL UNIQUE,
    descripcion NVARCHAR(150) NULL
);

/* 2. Usuarios (la contraseña se guarda como hash SHA-256, nunca en texto plano) */
CREATE TABLE usuarios (
    id_usuario      INT           NOT NULL PRIMARY KEY,   -- identificación (cédula)
    nombre          NVARCHAR(100) NOT NULL,
    correo          NVARCHAR(120) NOT NULL UNIQUE,
    contrasena_hash CHAR(64)      NOT NULL,
    id_rol          INT           NOT NULL,
    activo          BIT           NOT NULL DEFAULT 1,
    fecha_registro  DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT fk_usuarios_rol FOREIGN KEY (id_rol) REFERENCES roles(id_rol)
);

/* 3. Mesas / puntos de pedido (mesa o barra) */
CREATE TABLE mesas (
    id_mesa   INT IDENTITY(1,1) PRIMARY KEY,
    numero    INT          NOT NULL,
    tipo      VARCHAR(10)  NOT NULL DEFAULT 'MESA' CHECK (tipo IN ('MESA', 'BARRA')),
    capacidad INT          NOT NULL CHECK (capacidad > 0),
    estado    NVARCHAR(20) NOT NULL DEFAULT N'Disponible',
    CONSTRAINT uq_mesas_tipo_numero UNIQUE (tipo, numero)
);

/* 4. Estaciones de preparación (bar, cocina caliente, repostería) */
CREATE TABLE estaciones_preparacion (
    id_estacion INT IDENTITY(1,1) PRIMARY KEY,
    nombre      NVARCHAR(60)  NOT NULL UNIQUE,
    descripcion NVARCHAR(150) NULL
);

/* 5. Categorías del menú (cada categoría se prepara en una estación) */
CREATE TABLE categorias (
    id_categoria INT IDENTITY(1,1) PRIMARY KEY,
    nombre       NVARCHAR(60)  NOT NULL UNIQUE,
    descripcion  NVARCHAR(200) NULL,
    id_estacion  INT           NOT NULL,
    CONSTRAINT fk_categorias_estacion FOREIGN KEY (id_estacion) REFERENCES estaciones_preparacion(id_estacion)
);

/* 6. Productos */
CREATE TABLE productos (
    id_producto  INT IDENTITY(1,1) PRIMARY KEY,
    nombre       NVARCHAR(100) NOT NULL,
    descripcion  NVARCHAR(255) NULL,
    precio       DECIMAL(10,2) NOT NULL CHECK (precio > 0),
    disponible   BIT           NOT NULL DEFAULT 1,
    stock        INT           NOT NULL DEFAULT 0 CHECK (stock >= 0),
    id_categoria INT           NOT NULL,
    CONSTRAINT fk_productos_categoria FOREIGN KEY (id_categoria) REFERENCES categorias(id_categoria),
    CONSTRAINT uq_productos_nombre_cat UNIQUE (nombre, id_categoria)
);

/* 7. Estados de pedido (catálogo) */
CREATE TABLE estados_pedido (
    id_estado INT IDENTITY(1,1) PRIMARY KEY,
    nombre    NVARCHAR(30) NOT NULL UNIQUE,
    orden     INT          NOT NULL
);

/* 8. Pedidos (encabezado) */
CREATE TABLE pedidos (
    id_pedido           INT IDENTITY(1,1) PRIMARY KEY,
    id_mesa             INT           NULL,     -- NULL si es para llevar sin mesa
    id_usuario          INT           NULL,     -- quién lo registró (si aplica)
    tipo_pedido         VARCHAR(10)   NOT NULL CHECK (tipo_pedido IN ('LOCAL', 'LLEVAR')),
    id_estado           INT           NOT NULL,
    observacion         NVARCHAR(255) NULL,
    total               DECIMAL(10,2) NOT NULL DEFAULT 0,
    fecha_creacion      DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    fecha_actualizacion DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT fk_pedidos_mesa    FOREIGN KEY (id_mesa)    REFERENCES mesas(id_mesa),
    CONSTRAINT fk_pedidos_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
    CONSTRAINT fk_pedidos_estado  FOREIGN KEY (id_estado)  REFERENCES estados_pedido(id_estado)
);

/* 9. Detalle de pedido (líneas) */
CREATE TABLE detalle_pedido (
    id_detalle      INT IDENTITY(1,1) PRIMARY KEY,
    id_pedido       INT           NOT NULL,
    id_producto     INT           NOT NULL,
    cantidad        INT           NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(10,2) NOT NULL,   -- precio al momento de la venta
    observacion     NVARCHAR(200) NULL,
    CONSTRAINT fk_detalle_pedido   FOREIGN KEY (id_pedido)   REFERENCES pedidos(id_pedido) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
);

/* 10. Pedido por estación (qué parte del pedido va a cada estación) */
CREATE TABLE pedido_estacion (
    id_pedido        INT       NOT NULL,
    id_estacion      INT       NOT NULL,
    id_estado        INT       NOT NULL,
    fecha_asignacion DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT pk_pedido_estacion PRIMARY KEY (id_pedido, id_estacion),
    CONSTRAINT fk_pe_pedido   FOREIGN KEY (id_pedido)   REFERENCES pedidos(id_pedido) ON DELETE CASCADE,
    CONSTRAINT fk_pe_estacion FOREIGN KEY (id_estacion) REFERENCES estaciones_preparacion(id_estacion),
    CONSTRAINT fk_pe_estado   FOREIGN KEY (id_estado)   REFERENCES estados_pedido(id_estado)
);

/* 11. Pagos (simulados, sin pasarela real) */
CREATE TABLE pagos (
    id_pago    INT IDENTITY(1,1) PRIMARY KEY,
    id_pedido  INT           NOT NULL,
    metodo     VARCHAR(10)   NOT NULL CHECK (metodo IN ('EFECTIVO', 'TARJETA', 'SINPE')),
    monto      DECIMAL(10,2) NOT NULL CHECK (monto >= 0),
    estado     VARCHAR(10)   NOT NULL DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE', 'APROBADO', 'RECHAZADO')),
    fecha_pago DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT fk_pagos_pedido FOREIGN KEY (id_pedido) REFERENCES pedidos(id_pedido)
);

/* 12. Historial de estados (trazabilidad de cada cambio de un pedido) */
CREATE TABLE historial_estado_pedido (
    id_historial INT IDENTITY(1,1) PRIMARY KEY,
    id_pedido    INT       NOT NULL,
    id_estado    INT       NOT NULL,
    id_usuario   INT       NULL,
    fecha_cambio DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT fk_hist_pedido  FOREIGN KEY (id_pedido)  REFERENCES pedidos(id_pedido) ON DELETE CASCADE,
    CONSTRAINT fk_hist_estado  FOREIGN KEY (id_estado)  REFERENCES estados_pedido(id_estado),
    CONSTRAINT fk_hist_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);

/* 13. Logs del sistema (auditoría) */
CREATE TABLE logs_sistema (
    id_log     INT IDENTITY(1,1) PRIMARY KEY,
    fecha      DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    nivel      VARCHAR(12)   NOT NULL DEFAULT 'INFO' CHECK (nivel IN ('INFO', 'ADVERTENCIA', 'ERROR')),
    modulo     NVARCHAR(50)  NOT NULL,
    accion     NVARCHAR(255) NOT NULL,
    id_usuario INT           NULL,
    CONSTRAINT fk_logs_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);
GO

CREATE INDEX idx_pedidos_estado ON pedidos(id_estado);
CREATE INDEX idx_logs_fecha     ON logs_sistema(fecha);
GO

/* =====================================================================
   DATOS INICIALES (coinciden con los datos de prueba de FormAdmin)
   ===================================================================== */
INSERT INTO roles (nombre, descripcion) VALUES
 (N'Administrador', N'Gestiona usuarios, mesas, categorías y productos'),
 (N'Cocina',        N'Visualiza y actualiza el estado de los pedidos'),
 (N'Atención',      N'Atiende mesas y registra pedidos');

/* Contraseña de todos los usuarios de prueba: 1234 (guardada como SHA-256) */
DECLARE @clave CHAR(64) = LOWER(CONVERT(CHAR(64), HASHBYTES('SHA2_256', '1234'), 2));
INSERT INTO usuarios (id_usuario, nombre, correo, contrasena_hash, id_rol) VALUES
 (100000001, N'Administrador',       N'admin@smartorder.com',     @clave, 1),
 (123456789, N'María Salas',         N'maria@smartorder.com',     @clave, 3),
 (145662098, N'Mariela Fuentes',     N'mariela@smartorder.com',   @clave, 2),
 (147996223, N'Alejandro Gutiérrez', N'alejandro@smartorder.com', @clave, 2);

INSERT INTO mesas (numero, tipo, capacidad) VALUES
 (1,'MESA',4),(2,'MESA',4),(3,'MESA',2),(4,'MESA',6),(5,'MESA',8),
 (1,'BARRA',1),(2,'BARRA',1),(3,'BARRA',1);

INSERT INTO estaciones_preparacion (nombre, descripcion) VALUES
 (N'Bar',             N'Bebidas'),
 (N'Cocina caliente', N'Entradas y platos fuertes'),
 (N'Repostería',      N'Postres');

INSERT INTO categorias (nombre, descripcion, id_estacion) VALUES
 (N'Bebidas',        N'Bebidas frías y calientes', 1),
 (N'Entradas',       N'Platos para compartir',     2),
 (N'Platos Fuertes', N'Platos principales',        2),
 (N'Postres',        N'Postres de la casa',        3);

INSERT INTO productos (nombre, descripcion, precio, disponible, stock, id_categoria) VALUES
 (N'Limonada Natural', N'Bebida refrescante de limón natural', 1500, 1, 25, 1),
 (N'Té Frío', N'Té frío natural de durazno o limón', 1500, 1, 20, 1),
 (N'Bruschettas de tomate y albahaca', N'Pan tostado con cubos de tomate fresco, albahaca picada, aceite de oliva y sal', 4000, 1, 15, 2),
 (N'Dedos de Queso', N'Queso gouda empanizado acompañado de salsa marinara', 3500, 1, 18, 2),
 (N'Pasta Carbonara', N'Pasta con salsa cremosa, tocino crujiente y parmesano', 6500, 1, 12, 3),
 (N'Lomo Saltado', N'Trocitos de lomo salteados con cebolla y tomate', 7500, 1, 10, 3),
 (N'Tres Leches', N'Tradicional pastel esponjoso bañado en tres leches', 3000, 1, 14, 4),
 (N'Flan de Caramelo', N'Flan casero con salsa dulce de caramelo', 2800, 1, 16, 4);

INSERT INTO estados_pedido (nombre, orden) VALUES
 (N'Recibido',1),(N'En preparación',2),(N'Listo',3),(N'Entregado',4),(N'Cancelado',5);
GO

/* Usuario de la aplicación */
IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = 'smartorder_app')
    CREATE LOGIN smartorder_app WITH PASSWORD = 'SmartOrder2026*', CHECK_POLICY = OFF;
GO
USE smartorder_db;
CREATE USER smartorder_app FOR LOGIN smartorder_app;
ALTER ROLE db_datareader ADD MEMBER smartorder_app;
ALTER ROLE db_datawriter ADD MEMBER smartorder_app;
GO
