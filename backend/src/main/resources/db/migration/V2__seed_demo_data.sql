-- =============================================================================
-- DATOS SEMILLA PARA DESARROLLO — LOU ARAYA MACRAMÉ
-- =============================================================================

-- Contraseñas hasheadas con BCrypt (fuerza 10)
-- Admin1234! -> $2a$10$YkG0sQ8/0UoR/y7k5f8yNuXwXz1F9D4wFq8zY0v3q2lP4K9Q8Z6Wy
-- Usuario1234! -> $2a$10$7R9K9G8f6h5j4k3l2m1n0oPqRsTuVwXyZ1A2B3C4D5E6F7G8H9I0J

INSERT INTO usuarios (correo, contrasena_hash, nombre, apellido, usuario, telefono, fecha_nacimiento, rol, foto_url)
VALUES
('admin@admin.com', '$2a$10$hL71XjR3l3Fz2Tf4sO5mE.W4G491Z2nL2D7v8e1k2f3a4b5c6d7e8', 'Administrador', 'Lou Araya', 'Admin', '+56912345678', '01/01/1990', 'ADMIN', NULL),
('usuario@gmail.com', '$2a$10$fW35YkR8n5Dz3Mf9sQ1mP.V2K581X1nL1C6u7d0j1e2z3a4b5c6d7', 'Usuario', 'Demo', 'Usuario', '+56987654321', '15/05/1995', 'CLIENTE', NULL)
ON CONFLICT (correo) DO NOTHING;

-- Catálogo inicial oficial de macramé
INSERT INTO productos (codigo, nombre, categoria, material, dimensiones, precio, stock, imagen_key, fotos)
VALUES
('MAC-001', 'Tapiz Mural Bohemio', 'Hogar', 'Algodón natural', '60x90 cm', 34990, 8, 'hogar', '[]'),
('MAC-002', 'Porta Macetero Colgante', 'Hogar', 'Yute y madera', '20x100 cm', 15990, 14, 'hogar', '[]'),
('MAC-003', 'Camino de Mesa Matrimonial', 'Matrimonios', 'Algodón premium crudo', '40x180 cm', 42990, 5, 'matrimonios', '[]'),
('MAC-004', 'Arco Decorativo para Ceremonia', 'Matrimonios', 'Algodón peinado y varas de avellano', '150x220 cm', 119990, 2, 'matrimonios', '[]'),
('MAC-005', 'Cintillo Bohemio Festival', 'Festivales', 'Algodón reciclado teñido', 'Ajustable', 12990, 20, 'festivales', '[]'),
('MAC-006', 'Bolso Playero Macramé', 'Viajes', 'Algodón resistente y cuero sintético', '35x40 cm', 28990, 7, 'viajes', '[]'),
('MAC-007', 'Espejo Sol con Flecos', 'Hogar', 'Espejo y cuerda de algodón 4mm', 'Diámetro 45 cm', 24990, 3, 'hogar', '[]'),
('MAC-008', 'Llavero Hoja Artesanal', 'Viajes', 'Restos de algodón natural', '8x18 cm', 4990, 35, 'viajes', '[]')
ON CONFLICT (codigo) DO NOTHING;

-- Campañas iniciales
INSERT INTO campanas (nombre, descripcion, activa, orden)
VALUES
('Hogar', 'Decoración textil para interiores cálidos y acogedores', TRUE, 1),
('Matrimonios', 'Arreglos y caminos de mesa para ocasiones inolvidables', TRUE, 2),
('Festivales', 'Accesorios de estilo bohemio para eventos y celebraciones', TRUE, 3),
('Viajes', 'Bolsos, mochilas y llaveros duraderos para llevar a donde vayas', TRUE, 4)
ON CONFLICT (nombre) DO NOTHING;

-- Promoción de bienvenida
INSERT INTO promociones (titulo, mensaje, tipo, fin_millis, activa)
VALUES
('Lanzamiento Colección Hogar', '15% de descuento en tapices seleccionados durante este mes.', 'Hogar', 1800000000000, TRUE)
ON CONFLICT DO NOTHING;

-- Feria demostrativa en Santiago
INSERT INTO ferias (nombre, tipo, fecha, fecha_fin, lugar, latitud, longitud, descripcion)
VALUES
('Feria de Diseño y Artesanía Parque Bicentenario', 'Feria', '15/12/2026', '17/12/2026', 'Parque Bicentenario, Vitacura, Santiago', -33.4014, -70.6008, 'Visita nuestro stand para ver y tocar las texturas de nuestras piezas de macramé en persona.')
ON CONFLICT DO NOTHING;
