-- ============================================================
-- Datos de muestra — Empresa de Transporte Hirata
-- Ejecutar después del schema: source database/seed.sql
-- ============================================================

USE hirata_flota;

-- Limpiar datos previos (mantiene el schema intacto)
DELETE FROM mantenimiento;
DELETE FROM registro_kilometraje;
DELETE FROM camiones;
DELETE FROM conductores;

-- ── Conductores ───────────────────────────────────────────────
INSERT INTO conductores (nombre, rut, telefono, email) VALUES
  ('Carlos Muñoz Reyes',   '12345678-9', '+56912345678', 'carlos.munoz@hirata.cl'),
  ('Ana González Vidal',   '23456789-0', '+56923456789', 'ana.gonzalez@hirata.cl'),
  ('Pedro Soto Fuentes',   '34567890-1', '+56934567890', 'pedro.soto@hirata.cl'),
  ('Laura Díaz Moreno',    '45678901-2', '+56945678901', 'laura.diaz@hirata.cl');

-- ── Camiones ──────────────────────────────────────────────────
-- Camiones 1 y 2 con conductor asignado; 3 y 4 sin asignar
INSERT INTO camiones (patente, marca, modelo, anio, conductor_id) VALUES
  ('ABCD12', 'Volvo',         'FH16',   2020, 1),
  ('EFGH34', 'Mercedes-Benz', 'Actros', 2019, 2),
  ('IJKL56', 'Scania',        'R500',   2021, NULL),
  ('MNOP78', 'MAN',           'TGX',    2018, NULL);

-- ── Kilometraje ───────────────────────────────────────────────
-- ABCD12: 6200 km acumulados → activa alerta (umbral 5000)
INSERT INTO registro_kilometraje (patente, kilometraje) VALUES
  ('ABCD12', 3000),
  ('ABCD12', 2000),
  ('ABCD12', 1200);

-- EFGH34: 5100 km acumulados → activa alerta
INSERT INTO registro_kilometraje (patente, kilometraje) VALUES
  ('EFGH34', 2500),
  ('EFGH34', 2600);

-- IJKL56: 3800 km → sin alerta
INSERT INTO registro_kilometraje (patente, kilometraje) VALUES
  ('IJKL56', 2000),
  ('IJKL56', 1800);

-- MNOP78: 900 km → sin alerta
INSERT INTO registro_kilometraje (patente, kilometraje) VALUES
  ('MNOP78', 900);

-- ── Mantenimiento ─────────────────────────────────────────────
INSERT INTO mantenimiento (patente, tipo, descripcion, fecha) VALUES
  ('ABCD12', 'Preventivo', 'Cambio de aceite y filtros cada 5000 km', '2024-01-15'),
  ('EFGH34', 'Correctivo', 'Reparación de frenos traseros por desgaste anormal', '2024-02-20'),
  ('IJKL56', 'Revisión',   'Revisión técnica semestral obligatoria', '2024-03-10'),
  ('MNOP78', 'Preventivo', 'Cambio de neumáticos y alineación', '2024-04-05');
