# Revisión de la entrega sumativa

El caso de trabajo es la tienda en línea indicado por el profesor en el foro de semana 2. Se seleccionaron usuarios/inicio de sesión y gestión de productos.

## Requisitos y evidencias

| Criterio | Puntos | Evidencia a presentar |
|---|---:|---|
| Git y organización | 10 | Repositorio actualizado e historial real de cambios |
| CRUD en Oracle | 20 | Crear, consultar, actualizar y eliminar; verificar con SQL |
| Dos microservicios | 30 | Usuarios y productos ejecutándose por separado |
| JSON y Postman | 25 | Solicitudes, respuestas y errores esperados |
| Presentación | 15 | Video Kaltura de 7 a 10 minutos con demostración real |

## Comprobaciones

- [ ] Oracle está iniciado y las conexiones SQL Developer funcionan.
- [ ] Los dos servicios se ejecutan.
- [ ] La tabla usuarios contiene al menos tres cuentas.
- [ ] La tabla productos contiene al menos cinco productos.
- [ ] El administrador gestiona usuarios y productos.
- [ ] El cliente consulta productos y no puede administrarlos.
- [ ] El registro y el inicio de sesión funcionan.
- [ ] Las respuestas no contienen contraseñas ni hashes.
- [ ] Postman comprueba todos los endpoints de los servicios elegidos.
- [ ] Los datos inválidos no se guardan.
- [ ] Los cambios se comprobaron directamente en Oracle.
- [ ] La colección vuelve a pasar después de los últimos cambios.
- [ ] El repositorio remoto contiene el código que se entrega.
- [ ] El ZIP contiene código fuente y scripts SQL.
- [ ] El ZIP excluye claves, tokens, cachés y binarios de compilación.
- [ ] El video dura de 7 a 10 minutos y muestra operaciones reales.
- [ ] Los enlaces Git y Kaltura funcionan para el docente.
- [ ] La nomenclatura y fecha de entrega se revisaron directamente en el AVA.

## Diferenciar pruebas y evidencia

Las pruebas automáticas utilizan H2 para comprobar reglas, controladores y seguridad. Es necesario demostrar también el funcionamiento contra Oracle; H2 no cumple por sí sola el requisito de la evaluación.

El video, su publicación en Kaltura y el envío en AVA requieren la participación del estudiante. Esta lista no se considera completada solo por tener el código.

