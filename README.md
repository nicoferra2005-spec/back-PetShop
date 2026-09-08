# Back PetShop

API REST para un e-commerce de productos para mascotas.
Permite registrar usuarios, iniciar sesion, publicar productos y consultar el catalogo.
Incluye gestion de categorias, marcas, especies, carrito de compras y checkout.
El checkout valida stock, registra la compra, guarda sus detalles y descuenta unidades.
La API permite consultas completas y filtradas de productos y compras.
El proyecto implementa la capa de persistencia sobre el modelo relacional del trabajo practico.

## Aspectos tecnicos

- Java 17
- Spring Boot 3.4.5
- Spring Web
- Spring Data JPA
- MySQL para ejecucion local
- H2 para tests de integracion
- Lombok
- Maven Wrapper

## Configuracion

La aplicacion usa MySQL por defecto:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/catalogo_mascotas?createDatabaseIfNotExist=true&serverTimezone=America/Argentina/Buenos_Aires
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD:}
```

Para definir la clave de MySQL:

```bash
export DB_PASSWORD=tu_password
```

El repo incluye `.mvn/settings-tp.xml` para usar Maven Central y evitar depender de configuraciones globales de Maven.

## Ejecutar

```bash
./mvnw spring-boot:run
```

## Tests

```bash
./mvnw test
```

Los tests incluyen pruebas unitarias/controlador y pruebas de integracion con `MockMvc` + H2 para el flujo:

```text
registro -> login -> productos -> carrito -> checkout
```

## Endpoints principales

- `POST /api/usuarios/registro`
- `POST /api/usuarios/login`
- `GET /api/usuarios`
- `GET /api/productos`
- `GET /api/productos?soloDisponibles=true&categoriaId=1&marcaId=1&especieId=1&nombre=snack`
- `POST /api/productos`
- `PUT /api/productos/{id}?usuarioId={usuarioId}`
- `PATCH /api/productos/{id}/stock?usuarioId={usuarioId}`
- `DELETE /api/productos/{id}?usuarioId={usuarioId}`
- `GET /api/carrito/{usuarioId}`
- `POST /api/carrito/{usuarioId}/items`
- `PUT /api/carrito/{usuarioId}/items/{itemId}`
- `DELETE /api/carrito/{usuarioId}/items/{itemId}`
- `DELETE /api/carrito/{usuarioId}`
- `POST /api/compras/checkout/{usuarioId}`
- `GET /api/compras`
- `GET /api/compras?usuarioId={usuarioId}`
- `GET /api/compras/{id}`
- `GET|POST|PUT|DELETE /api/categorias`
- `GET|POST|PUT|DELETE /api/marcas`
- `GET|POST|PUT|DELETE /api/especies`

## Respuestas HTTP validadas

- `200 OK`: consultas y operaciones exitosas.
- `201 Created`: registro, alta de recursos y checkout.
- `204 No Content`: eliminaciones.
- `400 Bad Request`: datos invalidos, carrito vacio, stock insuficiente o producto no disponible.
- `401 Unauthorized`: credenciales invalidas.
- `403 Forbidden`: gestion de producto por usuario no creador.
- `404 Not Found`: recurso inexistente.
