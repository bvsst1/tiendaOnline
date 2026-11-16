-- Ejecutar cada bloque en su conexión de SQL Developer.
-- Conexión TIENDA_USUARIOS
SELECT id, nombre, email, rol FROM usuarios ORDER BY id;
SELECT COUNT(*) AS total_usuarios FROM usuarios;
-- Verificar que se almacenó un hash sin mostrarlo completo.
SELECT id, SUBSTR(password_hash, 1, 4) AS prefijo_hash FROM usuarios ORDER BY id;

-- Conexión TIENDA_PRODUCTOS
SELECT id, nombre, categoria, precio, stock FROM productos ORDER BY id;
SELECT COUNT(*) AS total_productos FROM productos;
-- Reemplazar 100 por el ID devuelto en Postman durante la demostración.
SELECT id, nombre, precio, stock FROM productos WHERE id = 100;
