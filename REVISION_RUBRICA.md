# Revisión de entrega según la pauta DSY1105

Fecha de revisión: 21-11-2026.

| Indicador | Estado en el ZIP preparado | Pendiente para acreditar el 100% |
|---|---|---|
| IE 3.1.1 Pantallas, formularios y flujos (15%) | El proyecto contiene pantallas Compose para bienvenida, autenticación, catálogo, administración, solicitudes, campañas, ferias, perfil y notificaciones. | Ejecutar en emulador/dispositivo y probar todos los flujos, validaciones y navegación. |
| IE 3.1.2 Backend y base de datos (15%) | Se incluye backend Kotlin/Spring Boot, PostgreSQL, Flyway, JWT, controladores y Docker Compose. Se agregó wrapper Gradle propio al backend y se ajustó Dockerfile. | Ejecutar `docker compose up -d --build`, revisar Swagger y validar endpoints reales. |
| IE 3.1.3 Integración móvil-backend CRUD (15%) | La app contiene cliente Retrofit y repositorio de sincronización para productos, solicitudes, favoritos y otros recursos. | Probar altas, consultas, modificaciones y eliminaciones desde la app contra el backend activo. |
| IE 3.1.4 API externa Retrofit (15%) | Se incorporó Open-Meteo mediante Retrofit y una tarjeta de clima actual en Ferias y Showrooms. | Ejecutar con internet y confirmar que la respuesta aparece en pantalla. |
| IE 3.2.1 Pruebas unitarias >=80% (15%) | Se conservaron tests existentes y se agregaron tests para el formateo de la respuesta meteorológica. Se habilitó cobertura de pruebas unitarias en debug. | Ejecutar tests y reporte de cobertura en Android Studio. El 80% no se puede afirmar hasta revisar el reporte real. |
| IE 3.3.1 APK firmado + JKS (10%) | Se generó `louaraya-release.jks` y se configuró la firma Gradle mediante `keystore.properties`. | Compilar `assembleRelease`, verificar la firma e instalar el APK. No se pudo compilar aquí porque el entorno no pudo descargar Gradle y no tiene Android SDK. |
| IE 3.3.2 GitHub + Trello (15%) | Se agregó `PLAN_TRELLO.md` como guía de tarjetas y `README.md` actualizado. | El ZIP original no contenía `.git` ni evidencia de Trello. Subir el código al repositorio real, conservar commits auténticos por integrante y adjuntar capturas del tablero real. |

## Comprobaciones realizadas

- Se inspeccionó el código fuente Android y backend incluido en el ZIP.
- Se verificó que el manifiesto Android ya declaraba permiso de internet; se habilitó tráfico HTTP para el backend local del emulador (`10.0.2.2`). Para producción debe utilizarse HTTPS.
- Se verificó que el JKS generado contiene una entrada de clave privada con alias `louaraya-release`.
- Se intentó ejecutar `./gradlew testDebugUnitTest`, pero el entorno no pudo descargar Gradle desde `services.gradle.org` (sin conexión DNS) y no dispone de Android SDK. Por eso no se declara compilación ni tests aprobados.
- No se inventaron commits, capturas, integrantes ni resultados de cobertura.

## Antes de entregar

1. Abrir el proyecto en Android Studio con internet y Android SDK API 37.
2. Ejecutar el backend con Docker.
3. Ejecutar las pruebas y revisar cobertura >=80%.
4. Probar la app y los flujos CRUD con backend conectado.
5. Generar e instalar el APK release firmado.
6. Completar integrantes reales en README.
7. Adjuntar evidencia auténtica de Trello y commits de GitHub.
8. Entregar JKS por el canal privado que indique el docente; no publicar la llave ni sus contraseñas en un repositorio público.
