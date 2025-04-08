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
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
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
