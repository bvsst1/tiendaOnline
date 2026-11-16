# Tienda en línea Semana 2

Backend de una tienda de accesorios de computación para Desarrollo Full Stack III. Se seleccionaron dos servicios: control de usuarios e inicio de sesión, y gestión de productos.

El alcance sigue el caso de tienda y las prácticas descritas por el profesor en el foro de semana 2. El archivo de instrucciones del caso de foros no se utilizó para definir las funcionalidades de esta tienda.

## Organización

- `usuarios-service`: usuarios, contraseñas y emisión de tokens. Puerto 8081.
- `productos-service`: consulta y gestión de productos. Puerto 8082.
- `database`: creación de esquemas, tablas, secuencias y datos iniciales.
- `postman`: colección con 26 solicitudes y comprobaciones.
- `scripts`: preparación de Oracle y ejecución.
- `docs`: explicación, revisión de requisitos y guía de presentación.

Cada servicio tiene sus propias capas Controller, Service, Repository, Entity y DTO. Cada uno tiene su propio esquema Oracle. El servicio de productos comprueba la firma del token emitido por usuarios; no consulta las tablas de usuarios.

El proyecto padre permite compilar ambos servicios con un comando. Los dos archivos JAR se ejecutan por separado; dos controladores dentro de una sola aplicación no serían dos microservicios.

## Herramientas

- Java 17 o posterior compatible con Spring Boot 3.5.7. La compilación genera bytecode Java 17.
- VS Code con Extension Pack for Java y, opcionalmente, Spring Boot Extension Pack.
- Maven Wrapper incluido; la primera ejecución necesita internet.
- Docker Desktop con contenedores Linux para la instalación local de Oracle Free.
- SQL Developer para consultar Oracle y Postman para probar las API.

## Preparar Oracle local

Abre esta carpeta en VS Code y ejecuta en PowerShell:

```powershell
.\scripts\Inicializar-Oracle.ps1
```

Docker Desktop debe estar iniciado. La primera vez se descarga la imagen oficial de Oracle Free 23ai Lite, versión 23.9.0.0. Puede tardar varios minutos; el script espera hasta quince minutos. Genera claves aleatorias en `.env`, levanta Oracle, crea el espacio USERS si falta y prepara dos esquemas con datos iniciales. No borra tablas al ejecutarlo nuevamente.

Oracle queda accesible solo desde el equipo en `localhost:1521/FREEPDB1`. Los datos persisten en un volumen Docker. Para detenerlo:

```powershell
docker compose stop
```

Para volver a iniciarlo:

```powershell
docker compose start
```

No elimines el volumen si quieres conservar los datos. No borres `.env`: contiene las claves de los esquemas ya creados. Cambiar el archivo no cambia automáticamente las contraseñas en Oracle.

Si ya tienes otra instancia Oracle, usa `.env.example` como referencia y ejecuta los scripts SQL manualmente: `00_crear_esquemas.sql` con SYSTEM dentro de la base conectable, `01_usuarios.sql` con TIENDA_USUARIOS y `02_productos.sql` con TIENDA_PRODUCTOS. Los scripts de tablas se ejecutan una sola vez sobre esquemas nuevos.

## Conectar SQL Developer

Crea estas dos conexiones de tipo Básico:

| Campo | Usuarios | Productos |
|---|---|---|
| Nombre de conexión | Tienda Usuarios | Tienda Productos |
| Usuario | TIENDA_USUARIOS | TIENDA_PRODUCTOS |
| Contraseña | Valor de USUARIOS_DB_PASSWORD en .env | Valor de PRODUCTOS_DB_PASSWORD en .env |
| Host | localhost | localhost |
| Puerto | 1521 | 1521 |
| Nombre de servicio | FREEPDB1 | FREEPDB1 |

Selecciona **Nombre del servicio**, no SID. Pulsa Probar y luego Conectar. Las contraseñas Oracle son diferentes de las contraseñas de las cuentas de la aplicación.

## Compilar y ejecutar

Configura JAVA_HOME con la carpeta de tu JDK. Si usas el Java encontrado en este equipo:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
.\mvnw.cmd clean verify
```

Las pruebas automáticas usan H2 en modo Oracle, únicamente en el classpath de pruebas. No reemplazan la verificación contra Oracle. La ejecución normal requiere Oracle y valida las tablas existentes; Hibernate no crea ni borra el esquema.

En una terminal:

```powershell
.\scripts\Iniciar-Servicio.ps1 -Servicio usuarios
```

En una segunda terminal:

```powershell
.\scripts\Iniciar-Servicio.ps1 -Servicio productos
```

Los scripts cargan las variables de `.env` en su proceso. Spring Boot no lee ese archivo automáticamente si se inicia desde otro comando.

## Cuentas de demostración

| Correo | Rol |
|---|---|
| admin@tienda.cl | ADMINISTRADOR |
| cliente@tienda.cl | CLIENTE |
| diego@tienda.cl | CLIENTE |

Las tres cuentas ficticias usan `DemoTienda2026!`. La base guarda hashes BCrypt, no contraseñas en texto plano. Son datos públicos de demostración local.

El registro público siempre crea un CLIENTE. Solo un administrador puede cambiar roles mediante la actualización de usuarios. El primer administrador proviene del script SQL.

## Autenticación y permisos

1. Envía correo y contraseña a `POST /api/auth/login`.
2. Copia el campo `token` de la respuesta.
3. En Postman selecciona Authorization, Bearer Token y pega el token.
4. Usa el mismo token en los dos servicios.

Los tokens duran 15 minutos. Si cambias un rol o eliminas una cuenta, un token ya emitido puede seguir siendo válido hasta que expire. Para esta entrega se utiliza autenticación sin sesiones y no se implementó revocación inmediata. Las pruebas de permisos deben usar tokens nuevos después de cambiar roles.

La clave de firma tiene que ser la misma en ambas aplicaciones y debe quedar fuera de Git. Este proyecto está preparado para demostración local; si se publica en una red debe utilizar HTTPS.

## Endpoints

| Servicio | Método y ruta | Permiso |
|---|---|---|
| Usuarios | POST /api/auth/registro | Público, crea cliente |
| Usuarios | POST /api/auth/login | Público |
| Usuarios | GET /api/usuarios/me | Cuenta autenticada |
| Usuarios | GET /api/usuarios | Administrador |
| Usuarios | GET /api/usuarios/{id} | Administrador |
| Usuarios | POST /api/usuarios | Administrador, crea cliente |
| Usuarios | PUT /api/usuarios/{id} | Administrador |
| Usuarios | DELETE /api/usuarios/{id} | Administrador |
| Productos | GET /api/productos | Cliente o administrador |
| Productos | GET /api/productos?nombre=mouse | Cliente o administrador |
| Productos | GET /api/productos/{id} | Cliente o administrador |
| Productos | POST /api/productos | Administrador |
| Productos | PUT /api/productos/{id} | Administrador |
| Productos | DELETE /api/productos/{id} | Administrador |

PUT reemplaza los campos editables del recurso. En usuarios, la contraseña es opcional: si se omite, se conserva la actual. Para crear cuentas se aceptan contraseñas de 8 a 64 caracteres ASCII imprimibles sin espacios.

Los precios se expresan en pesos chilenos, con hasta dos decimales. El precio debe ser mayor que cero y el stock no puede ser negativo. Las categorías son un atributo del producto; no hay una tabla adicional de categorías.

Se responde 200 en consultas y actualizaciones, 201 al crear y 204 al eliminar sin cuerpo. Los errores previstos usan 400, 401, 403, 404 o 409 según la situación. Un 204 no devuelve JSON.

## Probar con Postman

Importa `postman/Tienda-Semana2.postman_collection.json`. Ejecuta la colección en orden con Collection Runner, usando los datos iniciales y ambos servicios encendidos. El login almacena automáticamente los tokens en variables de colección.

La colección comprueba operaciones exitosas, datos inválidos, correo duplicado, credenciales incorrectas, permisos y registros inexistentes. Crea registros temporales y los elimina al final. Si se interrumpe, pueden quedar esos registros; revisa Oracle antes de grabar. No exportes una colección con tokens reales almacenados.

Para demostrar persistencia, ejecuta las consultas de `database/03_verificar.sql` en la conexión correspondiente. Durante el video detente después de crear, actualizar y eliminar para mostrar los cambios en Oracle.

## Entrega

Revisa `docs/REVISION-ENTREGA.md` y prepara la presentación con `docs/GUIA-VIDEO.md`. Ejecuta `scripts/Crear-Zip.ps1` para generar el comprimido con código, scripts SQL, Maven Wrapper, colección y documentación. El script excluye `.env`, tokens de configuración, cachés y carpetas target.

El repositorio es [bvsst1/tiendaOnline](https://github.com/bvsst1/tiendaOnline). Usa Git durante las modificaciones y mantén esa versión actualizada. El enlace Git y la grabación Kaltura deben adjuntarse en el AVA. Los resultados de la revisión técnica están en `docs/VERIFICACION.md`.

## Documentación consultada

- [Spring Boot 3.5 y requisitos de Java](https://docs.spring.io/spring-boot/3.5/system-requirements.html).
- [Validación de JWT con Spring Security](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html).
- [Oracle Free local en contenedores](https://docs.oracle.com/en/database/oracle/agent-memory/26.8/guide/run-locally.html).
