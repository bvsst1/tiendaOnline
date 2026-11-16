-- Ejecutar UNA VEZ en un esquema TIENDA_USUARIOS nuevo.
WHENEVER SQLERROR EXIT SQL.SQLCODE
SET DEFINE OFF
CREATE SEQUENCE usuarios_seq START WITH 100 INCREMENT BY 1 NOCACHE;

CREATE TABLE usuarios (
    id NUMBER(19,0) PRIMARY KEY,
    nombre VARCHAR2(100 CHAR) NOT NULL,
    email VARCHAR2(150 CHAR) NOT NULL,
    password_hash VARCHAR2(100 CHAR) NOT NULL,
    rol VARCHAR2(20 CHAR) NOT NULL,
    CONSTRAINT uk_usuarios_email UNIQUE (email),
    CONSTRAINT ck_usuarios_rol CHECK (rol IN ('ADMINISTRADOR', 'CLIENTE'))
);

-- Cuentas ficticias para la demostración local. Contraseña: DemoTienda2026!
-- El hash fue generado con BCrypt. No se almacena la contraseña en la tabla.
INSERT INTO usuarios VALUES (1, 'Administrador', 'admin@tienda.cl', '$2a$10$BOFRJ2t7M62MToN9mPcBP.WRQQvQspXa6WEowI7WKJGB9PrIt9.ui', 'ADMINISTRADOR');
INSERT INTO usuarios VALUES (2, 'Camila Soto', 'cliente@tienda.cl', '$2a$10$BOFRJ2t7M62MToN9mPcBP.WRQQvQspXa6WEowI7WKJGB9PrIt9.ui', 'CLIENTE');
INSERT INTO usuarios VALUES (3, 'Diego Rojas', 'diego@tienda.cl', '$2a$10$BOFRJ2t7M62MToN9mPcBP.WRQQvQspXa6WEowI7WKJGB9PrIt9.ui', 'CLIENTE');
COMMIT;

