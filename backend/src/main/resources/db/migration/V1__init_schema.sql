-- =============================================================================
-- ESQUEMA INICIAL DE BASE DE DATOS POSTGRESQL — LOU ARAYA MACRAMÉ
-- =============================================================================

-- Tabla de Usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    correo VARCHAR(150) NOT NULL UNIQUE,
    contrasena_hash VARCHAR(255) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    usuario VARCHAR(60) NOT NULL UNIQUE,
    telefono VARCHAR(30),
    fecha_nacimiento VARCHAR(20),
    rol VARCHAR(20) NOT NULL DEFAULT 'CLIENTE', -- 'ADMIN' o 'CLIENTE'
    foto_url TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_usuarios_correo ON usuarios(correo);
CREATE INDEX IF NOT EXISTS idx_usuarios_usuario ON usuarios(usuario);

-- Tabla de Productos de Macramé
CREATE TABLE IF NOT EXISTS productos (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    categoria VARCHAR(50) NOT NULL,
    material VARCHAR(100) NOT NULL,
    dimensiones VARCHAR(100) NOT NULL,
    precio INT NOT NULL CHECK (precio >= 0),
    stock INT NOT NULL DEFAULT 0 CHECK (stock >= 0),
    imagen_key VARCHAR(50) NOT NULL DEFAULT 'hogar',
    fotos TEXT NOT NULL DEFAULT '[]', -- Lista de fotos en formato JSON
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_productos_categoria ON productos(categoria);
CREATE INDEX IF NOT EXISTS idx_productos_codigo ON productos(codigo);

-- Tabla de Favoritos por Usuario
CREATE TABLE IF NOT EXISTS favoritos (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    producto_id INT NOT NULL REFERENCES productos(id) ON DELETE CASCADE,
    creado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_usuario_producto UNIQUE (usuario_id, producto_id)
);

CREATE INDEX IF NOT EXISTS idx_favoritos_usuario ON favoritos(usuario_id);

-- Tabla de Solicitudes y Pedidos Personalizados
CREATE TABLE IF NOT EXISTS solicitudes (
    id VARCHAR(64) PRIMARY KEY, -- UUID
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    producto_id INT NOT NULL REFERENCES productos(id) ON DELETE RESTRICT,
    unidades INT NOT NULL CHECK (unidades > 0),
    fecha_requerida VARCHAR(30) NOT NULL,
    comentario TEXT,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE', -- PENDIENTE, REVISION, APROBADA, ELABORACION, LISTA, ENTREGADA, CANCELADA
    personalizada BOOLEAN NOT NULL DEFAULT FALSE,
    color VARCHAR(60) NOT NULL DEFAULT 'Original',
    material VARCHAR(60) NOT NULL DEFAULT 'Original',
    medidas VARCHAR(60) NOT NULL DEFAULT 'Original',
    precio_estimado INT,
    creada_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    actualizada_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_solicitudes_usuario ON solicitudes(usuario_id);
CREATE INDEX IF NOT EXISTS idx_solicitudes_estado ON solicitudes(estado);

-- Historial / Trazabilidad de Estados de Solicitudes
CREATE TABLE IF NOT EXISTS historial_solicitudes (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id VARCHAR(64) NOT NULL REFERENCES solicitudes(id) ON DELETE CASCADE,
    estado_anterior VARCHAR(30),
    estado_nuevo VARCHAR(30) NOT NULL,
    cambiado_por_id BIGINT REFERENCES usuarios(id),
    nota TEXT,
    fecha TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_historial_solicitud ON historial_solicitudes(solicitud_id);

-- Mensajería entre Cliente y Administrador por Solicitud
CREATE TABLE IF NOT EXISTS mensajes_solicitud (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id VARCHAR(64) NOT NULL REFERENCES solicitudes(id) ON DELETE CASCADE,
    emisor_id BIGINT NOT NULL REFERENCES usuarios(id),
    contenido TEXT NOT NULL,
    leido BOOLEAN NOT NULL DEFAULT FALSE,
    fecha TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_mensajes_solicitud ON mensajes_solicitud(solicitud_id);

-- Tabla de Campañas / Colecciones
CREATE TABLE IF NOT EXISTS campanas (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    orden INT NOT NULL DEFAULT 0
);

-- Tabla de Promociones y Descuentos
CREATE TABLE IF NOT EXISTS promociones (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    mensaje TEXT NOT NULL,
    tipo VARCHAR(60) NOT NULL,
    fin_millis BIGINT NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    creada_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Ferias y Eventos
CREATE TABLE IF NOT EXISTS ferias (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    fecha VARCHAR(50) NOT NULL,
    fecha_fin VARCHAR(50) DEFAULT '',
    lugar VARCHAR(255) NOT NULL,
    latitud DOUBLE PRECISION NOT NULL,
    longitud DOUBLE PRECISION NOT NULL,
    descripcion TEXT DEFAULT ''
);

-- Tabla de Inscripciones a Ferias
CREATE TABLE IF NOT EXISTS inscripciones_feria (
    id BIGSERIAL PRIMARY KEY,
    feria_id INT NOT NULL REFERENCES ferias(id) ON DELETE CASCADE,
    usuario_id BIGINT REFERENCES usuarios(id) ON DELETE CASCADE,
    correo VARCHAR(150) NOT NULL,
    fecha TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_feria_correo UNIQUE (feria_id, correo)
);

-- Tabla de Notificaciones
CREATE TABLE IF NOT EXISTS notificaciones (
    id VARCHAR(64) PRIMARY KEY, -- UUID
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    titulo VARCHAR(150) NOT NULL,
    mensaje TEXT NOT NULL,
    leida BOOLEAN NOT NULL DEFAULT FALSE,
    tipo VARCHAR(50) DEFAULT 'GENERAL',
    referencia_id VARCHAR(64),
    fecha TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notificaciones_usuario ON notificaciones(usuario_id, leida);

-- Tabla de Tokens para Recuperación de Contraseña
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    token VARCHAR(100) NOT NULL UNIQUE,
    expira_en TIMESTAMP WITH TIME ZONE NOT NULL,
    usado BOOLEAN NOT NULL DEFAULT FALSE
);
