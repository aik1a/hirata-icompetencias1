-- ============================================================
-- Script de base de datos: Empresa de Transporte Hirata
-- Sistema de Gestión de Flota
-- ============================================================

-- Crear la base de datos si no existe
CREATE DATABASE IF NOT EXISTS hirata_flota;

-- Seleccionar la base de datos a usar
USE hirata_flota;

-- Tabla principal para el registro de kilometraje por camión
CREATE TABLE IF NOT EXISTS registro_kilometraje (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    patente      VARCHAR(10)  NOT NULL,
    kilometraje  INT          NOT NULL,
    fecha        DATETIME     DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de conductores de la flota
CREATE TABLE IF NOT EXISTS conductores (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    nombre    VARCHAR(100) NOT NULL,
    rut       VARCHAR(20)  NOT NULL,
    telefono  VARCHAR(20),
    email     VARCHAR(100)
);

-- Tabla de camiones con relación opcional a conductor asignado
CREATE TABLE IF NOT EXISTS camiones (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    patente       VARCHAR(10) NOT NULL,
    marca         VARCHAR(50) NOT NULL,
    modelo        VARCHAR(50) NOT NULL,
    anio          INT         NOT NULL,
    conductor_id  INT,
    FOREIGN KEY (conductor_id) REFERENCES conductores(id)
);

-- Tabla de registros de mantenimiento por camión
CREATE TABLE IF NOT EXISTS mantenimiento (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    patente      VARCHAR(10)  NOT NULL,
    tipo         VARCHAR(50)  NOT NULL,
    descripcion  TEXT,
    fecha        DATE         NOT NULL
);
