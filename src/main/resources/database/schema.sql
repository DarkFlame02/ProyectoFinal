-- Crear la base de datos
CREATE DATABASE IF NOT EXISTS gestion_reparaciones;
USE gestion_reparaciones;

-- Tabla de usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de talleres
CREATE TABLE IF NOT EXISTS talleres (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(200) NOT NULL,
    telefono VARCHAR(20),
    email VARCHAR(100),
    especialidad VARCHAR(100),
    valoracion_media DECIMAL(3,2) DEFAULT 0,
    activo BOOLEAN DEFAULT TRUE
);

-- Tabla de vehículos
CREATE TABLE IF NOT EXISTS vehiculos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    marca VARCHAR(50) NOT NULL,
    modelo VARCHAR(50) NOT NULL,
    matricula VARCHAR(10) NOT NULL UNIQUE,
    anio INT NOT NULL,
    kilometros INT DEFAULT 0,
    km_mensuales INT DEFAULT 0,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- Tabla de mantenimientos por defecto
CREATE TABLE IF NOT EXISTS mantenimientos_default (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tipo VARCHAR(100) NOT NULL,
    descripcion TEXT,
    intervalo_km INT NOT NULL,
    criticidad VARCHAR(20) DEFAULT 'Media' -- Alta, Media, Baja
);

-- Tabla de notificaciones de mantenimiento
CREATE TABLE IF NOT EXISTS notificaciones_mantenimiento (
    id INT AUTO_INCREMENT PRIMARY KEY,
    vehiculo_id INT NOT NULL,
    mantenimiento_id INT NOT NULL,
    km_estimados_restantes INT NOT NULL,
    fecha_estimada DATE,
    estado VARCHAR(20) DEFAULT 'Pendiente', -- Pendiente, Notificado, Completado
    FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id),
    FOREIGN KEY (mantenimiento_id) REFERENCES mantenimientos_default(id)
);

-- Tabla de reparaciones
CREATE TABLE IF NOT EXISTS reparaciones (
    id INT AUTO_INCREMENT PRIMARY KEY,
    vehiculo_id INT NOT NULL,
    taller_id INT NOT NULL,
    descripcion TEXT NOT NULL,
    fecha_reparacion DATE NOT NULL,
    costo DECIMAL(10,2) NOT NULL,
    estado VARCHAR(50) DEFAULT 'Pendiente',
    FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id),
    FOREIGN KEY (taller_id) REFERENCES talleres(id)
);

-- Tabla de valoraciones
CREATE TABLE IF NOT EXISTS valoraciones (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    taller_id INT NOT NULL,
    puntuacion INT NOT NULL,
    comentario TEXT,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (taller_id) REFERENCES talleres(id)
);

-- Insertar talleres de ejemplo
INSERT INTO talleres (nombre, direccion, telefono, email, especialidad, activo) VALUES 
('Talleres Martínez', 'Calle San Juan 123, Madrid', '912345678', 'info@talleresmartinez.es', 'Mecánica general', TRUE),
('AutoService Express', 'Avenida Principal 45, Barcelona', '934567890', 'contacto@autoservice.es', 'Chapa y pintura', TRUE),
('Neumáticos Rápido', 'Calle Toledo 67, Sevilla', '955678901', 'info@neumaticosrapido.es', 'Neumáticos', TRUE),
('ElectroAuto', 'Calle Mayor 34, Valencia', '963456789', 'servicio@electroauto.es', 'Electricidad', TRUE),
('TallerMec Premium', 'Avenida Diagonal 123, Barcelona', '932345678', 'info@tallermec.es', 'Vehículos de lujo', TRUE),
('Diesel Profesional', 'Calle Gran Vía 56, Madrid', '915678901', 'contacto@dieselpro.es', 'Motores diésel', TRUE),
('Talleres García Hermanos', 'Avenida de la Constitución 78, Zaragoza', '976789012', 'info@garciahnos.es', 'Mecánica general', TRUE),
('Servicio Técnico Oficial', 'Calle Industria 45, Bilbao', '944567890', 'sto@serviciotecnico.es', 'Concesionario oficial', TRUE),
('Motos y Más', 'Avenida del Mediterráneo 67, Alicante', '965678901', 'contacto@motosymas.es', 'Motocicletas', TRUE),
('ReparAuto 24h', 'Calle Doctor Fleming 23, Málaga', '952345678', 'info@reparauto24.es', 'Servicio 24 horas', TRUE);

-- Insertar datos de mantenimientos por defecto
INSERT INTO mantenimientos_default (tipo, descripcion, intervalo_km, criticidad) VALUES
('Cambio de aceite y filtro', 'Cambio de aceite y filtro del motor', 10000, 'Alta'),
('Cambio de filtro de aire', 'Sustitución del filtro de aire del motor', 15000, 'Media'),
('Cambio de pastillas de freno', 'Sustitución de las pastillas de freno', 30000, 'Alta'),
('Cambio de discos de freno', 'Sustitución de los discos de freno', 60000, 'Alta'),
('Cambio de líquido de frenos', 'Sustitución del líquido de frenos', 30000, 'Alta'),
('Cambio de correa de distribución', 'Sustitución de la correa de distribución', 80000, 'Alta'),
('Cambio de neumáticos', 'Sustitución de neumáticos', 40000, 'Alta'),
('Cambio de batería', 'Sustitución de la batería', 60000, 'Media'),
('Cambio de bujías', 'Sustitución de las bujías', 40000, 'Media'),
('Cambio de líquido refrigerante', 'Sustitución del líquido refrigerante', 60000, 'Media');

-- Insertar vehículos de ejemplo para el usuario1
INSERT INTO vehiculos (usuario_id, marca, modelo, matricula, anio, kilometros, km_mensuales) VALUES
(1, 'Seat', 'Ibiza', '1234ABC', 2018, 45000, 800),
(1, 'Volkswagen', 'Golf', '5678DEF', 2020, 15000, 600),
(1, 'Toyota', 'Corolla', '9012GHI', 2019, 30000, 750),
(1, 'Ford', 'Focus', '3456JKL', 2017, 60000, 900),
(1, 'Renault', 'Clio', '7890MNO', 2021, 8000, 500);

-- Insertar reparaciones de ejemplo
INSERT INTO reparaciones (vehiculo_id, taller_id, descripcion, fecha_reparacion, costo, estado) VALUES
(1, 1, 'Cambio de aceite y filtro', '2023-01-15', 80.50, 'Completada'),
(1, 3, 'Cambio de neumáticos', '2023-03-20', 450.00, 'Completada'),
(2, 2, 'Reparación de chapa por golpe', '2023-02-10', 320.75, 'Completada'),
(3, 4, 'Problema eléctrico en el cuadro de mandos', '2023-04-05', 180.00, 'Completada'),
(4, 1, 'Revisión general', '2023-05-12', 150.00, 'Completada'),
(5, 6, 'Problema en el sistema de inyección', '2023-06-18', 290.50, 'Completada'),
(1, 7, 'Cambio de pastillas de freno', '2023-07-22', 120.00, 'Completada'),
(2, 5, 'Revisión pre-ITV', '2023-08-30', 60.00, 'Programada'),
(3, 8, 'Cambio de correa de distribución', '2023-09-15', 520.00, 'Pendiente'),
(4, 10, 'Reparación de urgencia por avería', '2023-10-01', 350.00, 'En progreso');

-- Insertar valoraciones de ejemplo
INSERT INTO valoraciones (usuario_id, taller_id, puntuacion, comentario) VALUES
(1, 1, 5, 'Excelente servicio y atención'),
(1, 3, 4, 'Buen servicio pero un poco caro'),
(1, 2, 3, 'El trabajo quedó bien pero tardaron más de lo prometido'),
(1, 4, 5, 'Solucionaron el problema eléctrico rápidamente'),
(1, 7, 4, 'Buen trato y profesionalidad');

-- Actualizar las valoraciones medias de los talleres
UPDATE talleres SET valoracion_media = 5.0 WHERE id = 1;
UPDATE talleres SET valoracion_media = 3.0 WHERE id = 2;
UPDATE talleres SET valoracion_media = 4.0 WHERE id = 3;
UPDATE talleres SET valoracion_media = 5.0 WHERE id = 4;
UPDATE talleres SET valoracion_media = 0.0 WHERE id = 5;
UPDATE talleres SET valoracion_media = 0.0 WHERE id = 6;
UPDATE talleres SET valoracion_media = 4.0 WHERE id = 7;
UPDATE talleres SET valoracion_media = 0.0 WHERE id = 8;
UPDATE talleres SET valoracion_media = 0.0 WHERE id = 9;
UPDATE talleres SET valoracion_media = 0.0 WHERE id = 10;
