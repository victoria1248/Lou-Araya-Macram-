# Lou Araya Macramé — Backend Profesional (Kotlin + Spring Boot 3 + PostgreSQL)

Backend REST independiente para la tienda artesanal **Lou Araya Macramé**, desarrollado con arquitectura modular en Kotlin, Spring Boot 3, Spring Security 6 con JWT, Flyway y PostgreSQL.

---

## 1. Características Técnicas

- **Lenguaje:** Kotlin 1.9 / 2.x
- **Framework:** Spring Boot 3.3.4
- **Seguridad:** Spring Security con Stateless Session y JWT (HMAC-SHA256).
- **Control de Acceso:** Roles `ADMIN` y `CLIENTE` con anotaciones `@PreAuthorize`.
- **Cifrado de Contraseñas:** BCrypt (fuerza 10).
- **Base de Datos:** PostgreSQL 16 con migraciones automáticas mediante Flyway.
- **Documentación de API:** OpenAPI / Swagger UI integrada.

---

## 2. Puesta en Marcha con Docker (Recomendada)

Para iniciar PostgreSQL y la API en un solo comando:

```bash
cd backend
docker compose up -d --build
```

Esto levantará:
- **PostgreSQL 16** en el puerto `5432` con la base de datos `louaraya_db`.
- **Backend Spring Boot** en el puerto `8080`.

Para detener los servicios:
```bash
docker compose down
```

---

## 3. Puesta en Marcha Local (Desarrollo)

Si ya tienes PostgreSQL ejecutándose en tu máquina:

1. Configura las variables de entorno o usa los valores por defecto en `src/main/resources/application.yml`:
   ```bash
   export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/louaraya_db
   export SPRING_DATASOURCE_USERNAME=louaraya_user
   export SPRING_DATASOURCE_PASSWORD=macrame_secure_2026
   export JWT_SECRET=CAMBIAR_POR_UN_SECRETO_LARGO_Y_PRIVADO
   ```

2. Ejecuta la aplicación:
   ```bash
   ./gradlew bootRun
   ```

---

## 4. Cuentas Iniciales de Demostración (Seed Flyway V2)

| Rol | Correo / Usuario | Contraseña |
| :--- | :--- | :--- |
| **Administrador** | `admin@admin.com` / `Admin` | `Admin1234!` |
| **Cliente** | `usuario@gmail.com` / `Usuario` | `Usuario1234!` |

*(En producción, estas credenciales de demostración deben ser sustituidas o eliminadas).*

---

## 5. Documentación Interactiva (Swagger UI)

Una vez iniciado el servidor, accede en tu navegador a:
- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

Para probar endpoints protegidos en Swagger:
1. Envía `POST /api/auth/login` con las credenciales.
2. Copia el `token` de la respuesta.
3. Haz clic en **Authorize** en la esquina superior de Swagger y pega el token.

---

## 6. Endpoints Principales

### Autenticación (`/api/auth`)
- `POST /login`: Inicio de sesión (devuelve JWT).
- `POST /registro`: Registro de nuevos clientes.
- `GET /perfil`: Datos del usuario conectado.
- `PUT /perfil`: Edición de perfil y contraseña.
- `POST /recuperar-password`: Genera token de recuperación.
- `POST /reset-password`: Restablece contraseña con token.

### Catálogo de Macramé (`/api/productos`)
- `GET /`: Listar productos activos (soporta parámetro de búsqueda `?q=`).
- `GET /{id}`: Detalle de producto.
- `POST /`: Crear producto (Solo `ADMIN`).
- `PUT /{id}`: Editar producto (Solo `ADMIN`).
- `DELETE /{id}`: Desactivar producto (Solo `ADMIN`).

### Solicitudes y Encargos (`/api/solicitudes`)
- `POST /`: Crear solicitud personalizada (cliente).
- `GET /`: Listar solicitudes (cliente ve sus pedidos; admin ve todas).
- `PUT /{id}/estado`: Cambiar estado de confección con ajuste de inventario y trazabilidad (Solo `ADMIN`).
- `GET /{id}/historial`: Trazabilidad cronológica de estados.
- `POST /{id}/mensajes`: Chat directo entre cliente y artesano.
- `GET /{id}/mensajes`: Historial de mensajes del pedido.

### Métricas (`/api/admin/dashboard`)
- `GET /`: Total de productos, en stock, stock bajo, solicitudes por estado y clientes.
