# Explicación del backend

## Recorrido de una solicitud

Una petición de Postman pasa primero por los filtros de Spring Security. Si necesita autenticación, se verifica la firma del token, su vencimiento, emisor y destinatario. Luego se comprueba el rol.

El Controller recibe la petición y convierte el JSON a un DTO. En las operaciones de escritura se usa @Valid para comprobar las restricciones. Si la petición es válida, llama al Service. El Service aplica las reglas y utiliza el Repository para consultar o modificar las entidades en Oracle mediante JPA.

La respuesta usa un DTO para devolver solamente los campos que necesita el cliente.

## Responsabilidad de las clases

- Controller: define las rutas y los estados de respuesta.
- Service: procesa las operaciones, comprueba existencia y aplica reglas.
- Repository: permite acceder a Oracle con JpaRepository.
- Entity: representa la tabla y sus columnas.
- DTO: define los datos que entran o salen por la API.
- SecurityConfig: configura los filtros, permisos y comprobación del token.
- GlobalExceptionHandler: transforma los errores previstos en respuestas HTTP.

No se utiliza Lombok para que los atributos y métodos de las entidades se puedan leer directamente.

## Ejemplo con productos

En POST /api/productos, ProductoRequest exige nombre, categoría, precio positivo y stock no negativo. El cliente no puede decidir el ID: Oracle utiliza una secuencia.

ProductoController llama a ProductoService.crear. El servicio arma una entidad Producto y la guarda con el repositorio. Se devuelve ProductoResponse junto con un estado 201 y una cabecera Location.

Si un cliente intenta hacer ese mismo POST, Spring Security responde 403 antes de crear el producto. Si no envía un token válido, recibe 401.

## Ejemplo con usuarios

El registro comprueba que el correo no esté ocupado, normaliza el correo a minúsculas y calcula un hash BCrypt. Asigna el rol CLIENTE desde el servidor.

El inicio de sesión busca la cuenta por correo y compara la contraseña recibida con el hash. Si coincide, TokenService emite un JWT firmado que contiene el ID y rol, sin contraseña.

UsuarioResponse solo contiene ID, nombre, correo y rol. Aunque la entidad guarda el hash, ese dato no se devuelve en la API.

## Dos aplicaciones y dos esquemas

usuarios-service se ejecuta en 8081 y usa TIENDA_USUARIOS. productos-service se ejecuta en 8082 y usa TIENDA_PRODUCTOS. Sus ciclos de ejecución son independientes.

Los roles se guardan como un enum en la columna rol de usuarios. Por eso no existe una tabla de roles que necesite registros adicionales. La tabla usuarios tiene tres cuentas iniciales y productos tiene cinco productos.

## Límites del alcance

Se eligieron usuarios/inicio de sesión y gestión de productos. No se implementaron pedidos, carrito ni compras, correspondientes al servicio no seleccionado. Tampoco se desarrolló frontend ni se procesan pagos.

La autenticación utiliza tokens de corta duración. Un cambio de rol no invalida inmediatamente un token anterior; se necesita un nuevo login o esperar su vencimiento.

## Preguntas para practicar antes de grabar

1. ¿Por qué dos controladores no equivalen a dos microservicios?
2. ¿Qué diferencia hay entre Usuario y UsuarioResponse?
3. ¿Dónde se evita que un registro público cree un administrador?
4. ¿Qué hace @Valid y qué pasa si falta?
5. ¿Por qué un cliente recibe 403 y una petición sin token recibe 401?
6. ¿Cómo se verifica la contraseña si la tabla guarda un hash?
7. ¿Qué consulta SQL demuestra que una actualización quedó guardada?
8. ¿Por qué DELETE responde 204 sin un JSON?
9. ¿Cómo encuentra JPA la tabla y cómo genera el ID?
10. ¿Qué información nunca se debería escribir en los logs?

Practica las respuestas usando las clases del proyecto y tus propias palabras.
