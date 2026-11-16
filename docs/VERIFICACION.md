# Verificación del proyecto

Revisión realizada el 16 de noviembre de 2026.

## Resultado

Los servicios de usuarios y productos compilaron y se ejecutaron por separado contra Oracle Database Free 23ai, versión 23.9. El esquema de usuarios conserva tres cuentas iniciales y el de productos conserva cinco productos.

## Comprobaciones realizadas

| Comprobación | Resultado |
|---|---|
| Maven verify | Compilación y empaquetado correctos |
| Integración de usuarios con H2 | 6 pruebas, sin fallos |
| Integración de productos con H2 | 5 pruebas, sin fallos |
| Colección Postman ejecutada con Newman contra Oracle | 26 solicitudes y 35 comprobaciones, sin fallos |
| Crear usuario mediante API y consultar con SQL | Registro encontrado en Oracle |
| Actualizar usuario mediante API y consultar con SQL | Nombre actualizado en Oracle |
| Eliminar usuario mediante API y consultar con SQL | Registro ausente en Oracle |
| Crear producto mediante API y consultar con SQL | Registro encontrado en Oracle |
| Actualizar producto mediante API y consultar con SQL | Precio y stock actualizados en Oracle |
| Eliminar producto mediante API y consultar con SQL | Registro ausente en Oracle |
| Datos al terminar las pruebas | 3 usuarios y 5 productos |
| Archivos sensibles | .env y archivos temporales excluidos de Git y del ZIP |

Las solicitudes de Postman comprobaron también el acceso del cliente, las operaciones reservadas al administrador, datos inválidos, correo duplicado, credenciales incorrectas y recursos inexistentes.

## Preparación local

Docker utiliza almacenamiento en D:. El contenedor del proyecto se llama tienda-semana2-oracle y expone el servicio FREEPDB1 en localhost:1521.

La primera preparación de Oracle en este equipo tomó varios minutos por las operaciones de disco. El inicializador espera hasta quince minutos y permite reutilizar la instancia que ya está ejecutándose.

Oracle Lite no incluía el tablespace USERS. Los scripts crean ese espacio cuando falta, antes de crear los esquemas.

Las claves de los esquemas y de firma están en el archivo local .env y no forman parte del repositorio. Los dos procesos Java se iniciaron en segundo plano para la revisión.

## Pendiente para la entrega académica

El estudiante debe conectar SQL Developer con los valores del README, practicar la explicación del código, grabar y publicar el video Kaltura de siete a diez minutos y adjuntar el ZIP y los enlaces en el AVA. Estas acciones no se consideran realizadas por las pruebas técnicas.
