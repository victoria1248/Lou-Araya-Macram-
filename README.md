# Lou Araya Macramé

Aplicación móvil para la tienda artesanal **Lou Araya Macramé**, desarrollada con Kotlin y Jetpack Compose. Permite explorar el catálogo, gestionar productos, administrar solicitudes personalizadas y consultar ferias/showrooms.

## Integrantes

Completar con los nombres y apellidos oficiales de cada integrante del equipo antes de entregar.

## Tecnologías

- Android: Kotlin, Jetpack Compose y Material 3.
- Arquitectura de datos: repositorios, ViewModel, caché local SQLite/SharedPreferences y sincronización.
- Comunicación propia: Retrofit + OkHttp con API REST desarrollada en Kotlin y Spring Boot.
- Backend: Spring Boot 3, Spring Security, JWT, Spring Data JPA y PostgreSQL 16.
- Migraciones: Flyway.
- Documentación backend: OpenAPI / Swagger.
- API externa: Open-Meteo (clima actual de Santiago), consumida desde Android mediante Retrofit.
- Mapas: OSMDroid / OpenStreetMap.
- Pruebas: JUnit y pruebas instrumentadas de Android.

## Funcionalidades principales

### Cliente
- Bienvenida, registro e inicio de sesión.
- Catálogo con búsqueda, categorías y detalle de productos.
- Favoritos.
- Envío y seguimiento de solicitudes/encargos personalizados.
- Mensajes asociados a solicitudes.
- Promociones, notificaciones y perfil.
- Ferias y showrooms con mapa, ubicación y clima actual de Santiago.
- Caché local para consultar datos previamente sincronizados cuando no hay conexión.

### Administración
- Dashboard con métricas.
- Gestión CRUD de productos.
- Gestión de campañas/promociones.
- Gestión de solicitudes y cambio de estado.
- Gestión de ferias y showrooms.
- Gestión de cuentas y notificaciones.

## API propia (Spring Boot)

Base URL para el emulador Android: `http://10.0.2.2:8080/`

| Recurso | Métodos / endpoints |
|---|---|
| Autenticación | `POST /api/auth/login`, `POST /api/auth/registro` |
| Perfil | `GET /api/auth/perfil`, `PUT /api/auth/perfil` |
| Productos | `GET /api/productos`, `GET /api/productos/{id}`, `POST /api/productos`, `PUT /api/productos/{id}`, `DELETE /api/productos/{id}` |
| Favoritos | `GET /api/favoritos`, `POST /api/favoritos/{productoId}` |
| Solicitudes | `GET /api/solicitudes`, `POST /api/solicitudes`, `GET /api/solicitudes/{id}`, `PUT /api/solicitudes/{id}/estado` |
| Mensajes | `GET /api/solicitudes/{id}/mensajes`, `POST /api/solicitudes/{id}/mensajes` |
| Historial | `GET /api/solicitudes/{id}/historial` |
| Promociones | `GET /api/promociones`, `GET /api/promociones/todas`, `POST /api/promociones`, `DELETE /api/promociones/{id}` |
| Ferias | `GET /api/ferias`, `POST /api/ferias`, `PUT /api/ferias/{id}`, `DELETE /api/ferias/{id}`, `POST /api/ferias/{id}/inscribir` |
| Notificaciones | `GET /api/notificaciones`, `PUT /api/notificaciones/{id}/leida`, `PUT /api/notificaciones/marcar-todas-leidas` |
| Dashboard | `GET /api/admin/dashboard` |

La autenticación utiliza JWT. Los endpoints protegidos reciben `Authorization: Bearer <token>`.

## API externa (Retrofit)

Se consume Open-Meteo sin clave de API:

- Base URL: `https://api.open-meteo.com/`
- Endpoint: `GET /v1/forecast`
- Parámetros usados: `latitude=-33.4489`, `longitude=-70.6693`, `current=temperature_2m,relative_humidity_2m,precipitation,wind_speed_10m`, `timezone=auto`.
- La respuesta se muestra en la pantalla **Ferias y Showrooms** como información complementaria. Esta consulta es independiente del backend propio y no altera los productos ni solicitudes.

## Requisitos

- Android Studio compatible con el Android Gradle Plugin configurado.
- JDK 17 o superior.
- Android SDK API 37.
- Docker Desktop para ejecutar PostgreSQL y el backend mediante Compose (o PostgreSQL 16 instalado localmente).
- Conexión a internet para descargar dependencias y consultar la API externa.

## Ejecutar el backend

Desde la carpeta `backend`:

```bash
docker compose up -d --build
```

Servicios:
- PostgreSQL: `localhost:5432`
- API REST: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Para detener:

```bash
docker compose down
```

Las credenciales de demostración se encuentran documentadas en `backend/README.md`. Son únicamente para pruebas académicas; deben cambiarse antes de cualquier despliegue real.

## Ejecutar la aplicación móvil

1. Abrir en Android Studio la carpeta raíz que contiene `settings.gradle.kts`.
2. Esperar la sincronización de Gradle.
3. Iniciar PostgreSQL y el backend.
4. Ejecutar la configuración `app` en un emulador Android.
5. En el emulador estándar, `10.0.2.2` apunta al computador anfitrión. Para un teléfono físico, cambiar la URL del backend en la configuración de sesión por la IP local del computador y usar la misma red Wi-Fi.

## Pruebas

Pruebas unitarias de Android:

```bash
./gradlew testDebugUnitTest
```

Reporte de cobertura (si la versión de Android Gradle Plugin instalada ofrece la tarea correspondiente):

```bash
./gradlew createDebugUnitTestCoverageReport
```

Pruebas instrumentadas (requieren emulador o dispositivo):

```bash
./gradlew connectedDebugAndroidTest
```

Pruebas del backend:

```bash
cd backend
./gradlew test
```

**Importante:** el requisito de la pauta es alcanzar al menos 80% de cobertura de la lógica. Ejecutar las pruebas y revisar el reporte de cobertura en Android Studio antes de afirmar que se alcanzó ese porcentaje. La existencia de tests por sí sola no demuestra el 80%.

## Generar APK firmado

La configuración de firma ya está preparada en `app/build.gradle.kts`. Se generó el archivo privado `louaraya-release.jks` y se dejó configurado mediante `keystore.properties` en la raíz del proyecto. El respaldo de las credenciales está en `FIRMA_PRIVADA.txt`.

Luego, en Android Studio:
1. Abrir **Build > Generate Signed Bundle / APK**.
2. Seleccionar **APK**.
3. Seleccionar el archivo JKS y completar alias y contraseñas.
4. Elegir la variante `release`.
5. Generar el APK.
6. Verificar la firma y probar el APK en un dispositivo.

También se puede generar con Gradle después de configurar `keystore.properties`:

```bash
./gradlew assembleRelease
```

El APK se genera normalmente en `app/build/outputs/apk/release/`.

**Seguridad:** `.gitignore` excluye `keystore.properties`, `FIRMA_PRIVADA.txt` y `*.jks` para evitar publicar contraseñas y la llave privada en GitHub. Entregar el JKS por un canal privado indicado por el docente si la pauta lo solicita. Respaldar la llave en un lugar seguro. La llave es necesaria para futuras actualizaciones de la misma aplicación.

## GitHub y Trello

El repositorio debe mostrar los commits reales de cada integrante y el tablero Trello debe evidenciar tareas asignadas, responsables, fechas y estados. No se deben fabricar commits ni capturas. Antes de entregar:

- Añadir los nombres reales del equipo en este README.
- Confirmar que ambos integrantes aparecen como colaboradores y autores de commits.
- Añadir el enlace al repositorio público.
- Añadir evidencia real del tablero Trello.
- Adjuntar APK firmado y evidencia de la firma.
- Verificar que la entrega AVA contiene el mismo código fuente que el repositorio.

## Estado de entrega

Este repositorio contiene el código fuente Android y backend. La compilación, pruebas, cobertura real, ejecución conectada con PostgreSQL, APK firmado y evidencia de GitHub/Trello deben verificarse en el entorno del equipo antes de la defensa.
