-- Ejecutar UNA VEZ en un esquema TIENDA_PRODUCTOS nuevo.
WHENEVER SQLERROR EXIT SQL.SQLCODE
SET DEFINE OFF
CREATE SEQUENCE productos_seq START WITH 100 INCREMENT BY 1 NOCACHE;

CREATE TABLE productos (
    id NUMBER(19,0) PRIMARY KEY,
    nombre VARCHAR2(100 CHAR) NOT NULL,
    descripcion VARCHAR2(500 CHAR),
    categoria VARCHAR2(60 CHAR) NOT NULL,
    precio NUMBER(12,2) NOT NULL,
    stock NUMBER(10,0) NOT NULL,
    CONSTRAINT ck_productos_precio CHECK (precio > 0),
    CONSTRAINT ck_productos_stock CHECK (stock >= 0)
);

-- Precios en pesos chilenos. Productos ficticios para la actividad.
INSERT INTO productos VALUES (1, 'Mouse USB', 'Mouse óptico con cable', 'Accesorios', 9990, 15);
INSERT INTO productos VALUES (2, 'Teclado USB', 'Teclado en español', 'Accesorios', 14990, 12);
INSERT INTO productos VALUES (3, 'Audífonos', 'Audífonos con micrófono', 'Audio', 19990, 8);
INSERT INTO productos VALUES (4, 'Pendrive 64 GB', 'Memoria USB de 64 GB', 'Almacenamiento', 7990, 20);
INSERT INTO productos VALUES (5, 'Soporte notebook', 'Soporte regulable de escritorio', 'Escritorio', 12990, 10);
COMMIT;
