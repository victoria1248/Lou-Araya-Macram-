# Guía de Pruebas Multi-Dispositivo y Sincronización — Lou Araya Macramé

Esta guía detalla cómo probar la sincronización en tiempo real entre dos dispositivos distintos:
- **Dispositivo A (Cliente):** Envía un encargo personalizado o explora el catálogo.
- **Dispositivo B (Administrador):** Recibe la solicitud, revisa detalles, aprueba/actualiza el estado de confección y descuenta stock automáticamente.

---

## 1. Poner en marcha el Backend y la Base de Datos

En tu computador (servidor local):

### Opción A: Mediante Docker (Recomendada)
```bash
cd backend
docker compose up -d --build
```
Esto iniciará:
- **PostgreSQL 16** en el puerto `5432` con la base de datos `louaraya_db`.
- **Backend Spring Boot 3** en el puerto `8080`.

### Opción B: Ejecución Local Directa
Si prefieres ejecutarlo con tu JDK local:
```bash
cd backend
./gradlew bootRun
```

### Verificación
Abre en tu navegador:
- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- Comprueba que la lista de endpoints aparezca disponible.

---

## 2. Configurar la Conexión en los Dispositivos Android

1. Obtén la dirección IP local de tu computador en la red Wi-Fi:
   - **En Windows:** Abre la consola y escribe `ipconfig` (busca la dirección `IPv4`, por ejemplo `192.168.1.45`).
   - Si usas el **Emulador Android Studio**, la IP por defecto ya es `10.0.2.2` (no necesitas cambiarla).
2. Si usas un **teléfono físico**, asegúrate de que esté conectado a la **misma red Wi-Fi** que tu computador.
3. El archivo `SessionManager.kt` (`data/local/SessionManager.kt`) utiliza `http://10.0.2.2:8080/` para el emulador. Para un teléfono físico en la red, puedes cambiar la IP por la de tu equipo o configurar `setBaseUrl("http://192.168.1.45:8080/")`.

---

## 3. Flujo de Prueba Paso a Paso (Cliente vs Administrador)

### Paso 1: Inicio de Sesión en Ambos Dispositivos
- **En Dispositivo A (Cliente):**
  - Abre la app y presiona **Iniciar sesión**.
  - Ingresa con la cuenta cliente de prueba: `usuario@gmail.com` / `Usuario1234!` (o crea una cuenta nueva en **Regístrate**).
- **En Dispositivo B (Administrador):**
  - Abre la app e inicia sesión con: `admin@admin.com` / `Admin1234!`.
  - Accederás directamente al **Panel de Administración** con las métricas del taller.

### Paso 2: Creación de Encargo Personalizado (Dispositivo Cliente)
1. En el Dispositivo A, ve a la pestaña **Catálogo**.
2. Selecciona una pieza (por ejemplo, *Tapiz Mural Bohemio*).
3. Presiona **Solicitar pieza**.
4. Activa el interruptor **"Quiero personalizar mi encargo"**.
5. Selecciona un color de la paleta (ej. *Terracota*) y un material (ej. *Algodón reciclado*).
6. Selecciona una fecha requerida y añade un comentario: *"Deseo flecos extra largos"*.
7. Presiona **Revisar encargo** y luego **Confirmar solicitud**.
8. Verás el aviso de éxito y la solicitud aparecerá en estado **"Pendiente"** con el badge terracota.

### Paso 3: Recepción y Actualización por el Artesano (Dispositivo Administrador)
1. En el Dispositivo B, en el **Dashboard de Administración**, observarás que el contador de **"Pendientes"** se incrementa automáticamente.
2. Toca la tarjeta de métrica o entra en **"Ver solicitudes y pedidos"**.
3. En la lista verás la nueva solicitud del cliente con sus especificaciones de personalización (*Color Terracota · Algodón reciclado*).
4. Toca **Actualizar estado** y selecciona **Confirmado**.
5. El servidor actualizará el estado de forma atómica y **descontará automáticamente las unidades del inventario central en PostgreSQL**.

### Paso 4: Sincronización en el Dispositivo Cliente
1. En el Dispositivo A, ve a la sección **Pedidos** (o presiona el botón de sincronización ⟳).
2. Observarás que el badge de la solicitud cambia inmediatamente a **"Confirmado"** (en verde bosque), confirmando que el artesano aprobó el encargo.
3. Si el cliente revisa el catálogo, verá reflejada la reducción de stock disponible.

---

## 4. Prueba de Resiliencia y Modo Offline

1. En el dispositivo móvil, desactiva el Wi-Fi o apaga temporalmente el backend.
2. Abre la aplicación: notarás el indicador **"Modo Local"** en la parte superior.
3. La aplicación continúa funcionando fluidamente, permitiendo explorar el catálogo y consultar solicitudes previamente guardadas en la base de datos local SQLite (`LocalCacheDb`).
4. Al restablecer la conexión Wi-Fi, la app sincroniza los cambios pendientes con PostgreSQL automáticamente.
