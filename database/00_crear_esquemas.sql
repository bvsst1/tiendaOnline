-- Ejecutar como SYSTEM, conectado al servicio FREEPDB1 (no al contenedor FREE).
-- En SQL Developer usar Ejecutar script (F5). No incluir contraseñas en Git.
WHENEVER SQLERROR EXIT SQL.SQLCODE
-- Oracle Lite puede no traer USERS. La ruta siguiente corresponde al contenedor del proyecto.
DECLARE
    cantidad NUMBER;
BEGIN
    SELECT COUNT(*) INTO cantidad FROM dba_tablespaces WHERE tablespace_name = 'USERS';
    IF cantidad = 0 THEN
        EXECUTE IMMEDIATE 'CREATE TABLESPACE users DATAFILE ''/opt/oracle/oradata/tienda_users01.dbf'' SIZE 50M AUTOEXTEND ON NEXT 10M MAXSIZE 300M';
    END IF;
END;
/
ACCEPT clave_usuarios CHAR PROMPT 'Contraseña del esquema TIENDA_USUARIOS: ' HIDE
ACCEPT clave_productos CHAR PROMPT 'Contraseña del esquema TIENDA_PRODUCTOS: ' HIDE

CREATE USER tienda_usuarios IDENTIFIED BY "&clave_usuarios"
    DEFAULT TABLESPACE users QUOTA 20M ON users;
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE TO tienda_usuarios;

CREATE USER tienda_productos IDENTIFIED BY "&clave_productos"
    DEFAULT TABLESPACE users QUOTA 20M ON users;
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE TO tienda_productos;

UNDEFINE clave_usuarios
UNDEFINE clave_productos
