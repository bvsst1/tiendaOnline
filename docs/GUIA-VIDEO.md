# Guía para la presentación en Kaltura

Duración objetivo: nueve minutos. Ensaya con el proyecto funcionando y usa esta pauta como recordatorio. Explica con tus propias palabras.

## Antes de grabar

Abre dos terminales con los servicios, Postman y las dos conexiones de SQL Developer. Comprueba que haya al menos tres usuarios y cinco productos. Inicia sesión nuevamente para que los tokens no venzan durante la grabación.

No muestres .env, claves Oracle, la clave de firma ni tokens completos. En SQL consulta ID, nombre, correo y rol, sin exponer hashes completos.

## Secuencia sugerida

| Tiempo | Qué mostrar |
|---|---|
| 0:00 a 0:40 | Caso de tienda, servicios elegidos y alcance |
| 0:40 a 1:25 | Dos proyectos, sus capas, esquemas y puertos |
| 1:25 a 2:05 | Git: historial real, archivos y repositorio actualizado |
| 2:05 a 3:20 | Login administrador, crear usuario, consultarlo y actualizarlo |
| 3:20 a 3:50 | Consultar el usuario en Oracle y eliminar el usuario temporal |
| 3:50 a 5:35 | Cinco productos, crear uno temporal, consultar y actualizar |
| 5:35 a 6:20 | Mostrar los cambios en Oracle, eliminar el producto y comprobar ausencia |
| 6:20 a 7:40 | Datos inválidos 400, recurso inexistente 404 y correo duplicado 409 |
| 7:40 a 8:35 | Cliente consulta; cliente no puede crear producto 403; sin token 401 |
| 8:35 a 9:00 | Confirmar datos mínimos, scripts SQL y resumen del funcionamiento |

## Detalles que deben verse

- Ambos servicios realmente iniciados.
- Solicitudes ejecutadas y respuestas legibles.
- Respuestas JSON sin contraseñas ni hashes.
- Tablas Oracle con los datos correspondientes.
- Cambios de un mismo registro antes y después.
- Permisos efectivos usando cuentas de distinto rol.
- Un repositorio que corresponda al código mostrado.

La colección completa sirve para revisar antes de grabar. Durante el video ejecuta manualmente las solicitudes necesarias y detente para mostrar Oracle. No intentes narrar las 26 solicitudes si eso impide explicar el funcionamiento dentro del tiempo disponible.

## Después de grabar

Comprueba que dure entre siete y diez minutos, se escuche claramente y los datos sean legibles. Publica en Kaltura con los permisos necesarios para el docente y verifica el enlace antes de adjuntarlo en el AVA.
