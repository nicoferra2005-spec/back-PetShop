# Detalle de correcciones pendientes

Relevamiento del backend hecho sobre el commit `5ace0e7` (último de `main` al
cerrar el paso 6), contrastado contra `Consignas.txt` y la división de tareas.

El objetivo de este documento es que cada uno sepa exactamente qué le falta
sumar de su lado. **No hace falta rehacer nada**: lo que está hecho está bien
encaminado, lo que sigue son huecos concretos.

Estado general: el proyecto **compila** y la suite de tests pasa **16/16**
(después del fix de `fix: alinear el test de integracion con el formato
ErrorResponse`; antes de ese commit `main` estaba en rojo).

---

## Resumen por paso

| Paso | Responsable | Estado | Bloquea la entrega |
|------|-------------|--------|--------------------|
| 1 | Lu | Casi completo | Sí — falta `spring-boot-starter-validation` |
| 2 | Barza | Completo | No |
| 3 | Sol | Casi completo | No |
| 4 | Bauti | Completo | No |
| 5 | Fede | Completo | No |
| 6 | Joaquin | Incompleto | Sí — el handler de validación es código muerto |
| 7 | Luqui | En curso | — |
| 8 | Nico | Pendiente | — |

---

## Paso 1 — Lu · Preparación y arquitectura

**Lo que está bien:** los ocho paquetes existen (`controller`, `service`,
`repository`, `model`, `dto`, `exception`, `config`, `security`), los dos
últimos con `package-info.java` documentando para qué quedan reservados.
El `.gitignore` está correcto y `target/` ya no se versiona. El `pom.xml` tiene
Lombok declarado también en `annotationProcessorPaths`, que es lo correcto.

**Lo que falta:**

1. **Agregar `spring-boot-starter-validation` al `pom.xml`.** Hoy no está, y sin
   esa dependencia no existe `jakarta.validation` en el classpath: no se puede
   anotar ningún DTO ni usar `@Valid`. Esto es lo que deja sin efecto todo el
   trabajo de validación del paso 6.

   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-validation</artifactId>
   </dependency>
   ```

   > Nota: esta dependencia la agrego yo en el paso 7, porque la necesito para
   > validar `RegisterRequest`. Queda anotada igual para que se entienda por qué
   > el handler de validación nunca se disparaba.

2. **Documentar cómo levantar el proyecto.** No hay `README.md`. Para la entrega
   conviene uno corto con: requisitos (Java 17, MySQL), cómo setear
   `DB_PASSWORD`, y el comando de arranque. Suma en "estructura de código limpia
   y organizada".

---

## Paso 2 — Barza · DTOs

**Lo que está bien:** hay DTOs de request y response para todos los dominios
(Producto, Categoría, Marca, Especie, Carrito, Compra, Usuario). Ningún
Controller recibe ni devuelve entidades. `UsuarioResponseDTO` no expone `clave`,
y hay un test que lo verifica (`jsonPath("$.clave").doesNotExist()`).

**Lo que falta:**

1. **Unificar el estilo de los DTOs.** Hoy conviven dos estilos: `record`
   (`CarritoResponse`, `ItemCarritoResponse`, `ProductoRequest`...) y clase con
   Lombok `@Data @Builder` (`RegisterRequest`, `LoginRequest`,
   `UsuarioResponseDTO`). Funcionan las dos, pero en una revisión de "código
   limpio y consistente" salta a la vista. Recomendación: `record` para todos
   los response, que son inmutables por naturaleza.

2. **Renombrar `UsuarioResponseDTO` a `UsuarioResponse`.** Es el único DTO con
   sufijo `DTO`; todos los demás siguen el patrón `XxxRequest` / `XxxResponse`.

---

## Paso 3 — Sol · QueryMethods

**Lo que está bien:** `CategoriaRepository`, `MarcaRepository`,
`EspecieRepository`, `CompraRepository`, `CarritoRepository` e
`ItemCarritoRepository` tienen QueryMethods derivados bien armados
(`findByHabilitadaTrueOrderByNombreAsc`, `findByCompradorIdOrderByRealizadaEnDesc`,
`findFirstByCarrito_IdAndProducto_Id`, `existsByNombreIgnoreCase`, etc.).

**Lo que falta:**

1. **`ProductoRepository` usa un `@Query` JPQL gigante en vez de QueryMethods.**
   La consigna pide explícitamente QueryMethods y "reemplazar consultas manuales
   innecesarias". El `buscar(...)` con seis parámetros y `:x is null or ...`
   resuelve el filtrado dinámico, pero no es lo que se está evaluando.

   Opciones, de menor a mayor esfuerzo:
   - Agregar QueryMethods derivados para los casos frecuentes y dejar el `@Query`
     solo para el filtro combinado:
     ```java
     List<Producto> findByDisponibleTrueOrderByNombreAsc();
     List<Producto> findByCategoriaIdAndDisponibleTrue(Long categoriaId);
     List<Producto> findByMarcaIdAndDisponibleTrue(Long marcaId);
     List<Producto> findByNombreContainingIgnoreCase(String nombre);
     List<Producto> findByCreadorId(Long creadorId);
     ```
   - O reemplazar el `@Query` por `JpaSpecificationExecutor` + `Specification`,
     que es la forma idiomática de filtrado dinámico en Spring Data.

2. **`DetalleCompraRepository`, `ImagenProductoRepository` y
   `ProductoEspecieRepository` están vacíos.** Al menos los dos primeros tienen
   consultas naturales que hoy se resuelven navegando la entidad:
   ```java
   // DetalleCompraRepository
   List<DetalleCompra> findByCompraId(Long compraId);
   // ImagenProductoRepository
   List<ImagenProducto> findByProductoIdOrderByPosicionAsc(Long productoId);
   Optional<ImagenProducto> findByProductoIdAndPrincipalTrue(Long productoId);
   ```

3. **`UsuarioRepository` no tiene `findByNombreUsuario`.** Tiene
   `existsByNombreUsuario` pero no el `find`. Lo agrego yo en el paso 7 porque lo
   necesita el `UserDetailsService`.

---

## Paso 4 — Bauti · Controllers y ResponseEntity

**Lo que está bien:** los siete Controllers devuelven `ResponseEntity` en el
100% de los métodos (verificado por grep). Los códigos son coherentes: `201` en
las creaciones, `204` en los borrados, `200` en las lecturas. Ningún Controller
tiene lógica de negocio ni `try/catch`.

**Lo que falta:**

1. **Agregar `@Valid` en los `@RequestBody`.** Hoy ningún Controller lo usa, así
   que las validaciones declarativas no corren. Va de la mano con el punto 1 del
   paso 1 y el paso 6:
   ```java
   public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request)
   ```

2. **Devolver `Location` en los `201 CREATED`.** Hoy se devuelve el body pero no
   la cabecera. Es el detalle que suele pedirse como plus:
   ```java
   URI location = ServletUriComponentsBuilder.fromCurrentRequest()
           .path("/{id}").buildAndExpand(creado.id()).toUri();
   return ResponseEntity.created(location).body(creado);
   ```

---

## Paso 5 — Fede · Excepciones personalizadas

**Lo que está bien:** la jerarquía está bien pensada y categorizada, que es
justo lo que pedía la consigna. `RecursoNoEncontradoException`,
`UsuarioException`, `DatosInvalidosException` y `OperacionNoPermitidaException`
funcionan como categorías, y las específicas heredan de ellas
(`UsuarioNotFoundException extends RecursoNoEncontradoException`,
`CorreoYaRegistradoException extends UsuarioException`,
`CarritoVacioException extends DatosInvalidosException`).

**Lo que falta:**

1. **`StockInsuficienteException` y `ProductoNoDisponibleException` cuelgan de
   `RuntimeException` en vez de una categoría.** Son las dos únicas que quedaron
   sueltas. Deberían heredar de una categoría común, por ejemplo una
   `ProductoException extends RuntimeException`, o directamente de
   `DatosInvalidosException` si el código HTTP es siempre `400`. Así el
   `GlobalExceptionHandler` puede capturar la categoría en vez de enumerarlas.

2. **`DatosInvalidosException extends IllegalArgumentException`.** El comentario
   dice que es "para conservar compatibilidad", pero hoy ya no queda ningún
   `throw new IllegalArgumentException` propio en el código: se puede cortar esa
   herencia y hacerla `extends RuntimeException`. Heredar de
   `IllegalArgumentException` hace que cualquier `IllegalArgumentException` de
   una librería de terceros se reporte al cliente como un `400` de negocio.

---

## Paso 6 — Joaquin · @ControllerAdvice

**Lo que está bien:** una sola clase `@ControllerAdvice`, formato de respuesta
uniforme vía el record `ErrorResponse`, que además de lo que pedía la consigna
suma `path` y `validationErrors`. Los handlers agrupan por categoría y devuelven
`ResponseEntity`. El handler genérico de `Exception` devuelve un mensaje fijo y
no filtra el detalle interno al cliente — está bien resuelto.

**Lo que falta:**

1. **`handleValidation(MethodArgumentNotValidException)` es código muerto.**
   Está escrito y bien escrito, pero nunca se ejecuta: no existe
   `spring-boot-starter-validation` en el `pom.xml`, no hay una sola anotación
   `@NotBlank` / `@NotNull` / `@Email` en ningún DTO, y ningún Controller usa
   `@Valid`. Para que el paso quede realmente cumplido hacen falta las tres
   cosas.

2. **Falta un handler para `MethodArgumentTypeMismatchException`.** Hoy
   `GET /api/productos/abc` (id no numérico) cae en el handler genérico y
   devuelve `500`, cuando corresponde `400`.

3. **Loguear la excepción en `handleUnexpected`.** Se devuelve "Ocurrió un error
   interno" al cliente, que está perfecto, pero el stacktrace no se escribe en
   ningún lado, así que un `500` en producción es imposible de diagnosticar.
   Alcanza con un `log.error("Error no controlado", exception)`.

4. **Al romper el contrato de error hay que actualizar los tests.** El cambio de
   `String` plano a `ErrorResponse` dejó `main` en rojo: el
   `CatalogoApiIntegrationTest` seguía afirmando sobre `jsonPath("$")`. Ya está
   corregido, pero conviene correr `./mvnw test` antes de pushear.

---

## Paso 8 — Nico · Registro e integración final

Todavía no arrancado, pero para cuando toque: el paso 7 deja listos
`PasswordEncoder`, `Rol`, `UsuarioDetails`, `UsuarioDetailsService` y
`SecurityConfig`, y deja `UsuarioService.registrarUsuario` ya encriptando la
clave, asignando `ROLE_CLIENTE` y construyendo el `Usuario` con Builder.

Dos cosas a tener en cuenta:

1. **Los usuarios cargados antes del paso 7 tienen la clave en texto plano y no
   van a poder loguearse.** `BCrypt.matches()` contra un hash que no es BCrypt
   devuelve `false`. Hay que volver a registrarlos o limpiar la tabla `usuario`.

2. **`SecurityConfig` deja todos los endpoints abiertos a propósito** (con
   `httpBasic` cableado), para no romper la API existente ni el front. Restringir
   por rol (`GET /api/usuarios` solo `ROLE_ADMIN`, por ejemplo) es el siguiente
   paso natural y cuenta como funcionalidad extra.

---

## Checklist antes de generar el `.zip`

- [ ] `./mvnw clean test` en verde
- [ ] `spring-boot-starter-validation` en el `pom.xml`
- [ ] DTOs de request con anotaciones de validación y `@Valid` en los Controllers
- [ ] `README.md` con instrucciones de arranque
- [ ] `target/` fuera del repo (ya está en `.gitignore`)
- [ ] Repos de back y front separados y enlazados en la entrega de BSP
