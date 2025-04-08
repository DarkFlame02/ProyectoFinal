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
    FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id),
    FOREIGN KEY (taller_id) REFERENCES talleres(id)
);

-- Insertar usuario admin por defecto
INSERT INTO usuarios (username, password, email) 
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@example.com'); 