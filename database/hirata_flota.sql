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
