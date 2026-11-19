CREATE DATABASE IF NOT EXISTS burok_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE burok_db;

CREATE TABLE IF NOT EXISTS emprendedores (
    codigo VARCHAR(20) PRIMARY KEY,
    nombre_completo VARCHAR(120) NOT NULL,
    dni VARCHAR(25) NOT NULL UNIQUE,
    telefono VARCHAR(25) NOT NULL,
    correo VARCHAR(100) NOT NULL,
    direccion VARCHAR(200) NOT NULL
);

CREATE TABLE IF NOT EXISTS empresas (
    codigo VARCHAR(20) PRIMARY KEY,
    nombre_comercial VARCHAR(150) NOT NULL,
    rtn VARCHAR(25) NOT NULL,
    forma_juridica VARCHAR(60) NOT NULL,
    codigo_ciiu VARCHAR(80) NOT NULL,
    categoria_riesgo INT NOT NULL,
    municipio VARCHAR(80) NOT NULL,
    capital_semilla DECIMAL(12,2) NOT NULL,
    emprendedor_codigo VARCHAR(20) NOT NULL,
    FOREIGN KEY (emprendedor_codigo) REFERENCES emprendedores(codigo) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tramites_catalogo (
    codigo VARCHAR(20) PRIMARY KEY,
    entidad VARCHAR(80) NOT NULL,
    etapa VARCHAR(40) NOT NULL,
    nombre VARCHAR(160) NOT NULL,
    horas_tradicionales INT NOT NULL,
    horas_burok INT NOT NULL,
    costo_oficial_lps DECIMAL(12,2) NOT NULL,
    plazo_sla_dias INT NOT NULL
);

CREATE TABLE IF NOT EXISTS expedientes (
    numero_expediente VARCHAR(20) PRIMARY KEY,
    fecha_creacion VARCHAR(20) NOT NULL,
    empresa_codigo VARCHAR(20) NOT NULL,
    horas_tradicionales_est INT NOT NULL,
    horas_burok_est INT NOT NULL,
    estado_global VARCHAR(30) NOT NULL,
    liquidado BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (empresa_codigo) REFERENCES empresas(codigo) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS expediente_tramites (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero_expediente VARCHAR(20) NOT NULL,
    tramite_codigo VARCHAR(20) NOT NULL,
    dias_transcurridos INT NOT NULL DEFAULT 0,
    estado VARCHAR(30) NOT NULL,
    silencio_positivo BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (numero_expediente) REFERENCES expedientes(numero_expediente) ON DELETE CASCADE,
    FOREIGN KEY (tramite_codigo) REFERENCES tramites_catalogo(codigo)
);

CREATE TABLE IF NOT EXISTS expediente_documentos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    empresa_codigo VARCHAR(20) NOT NULL,
    codigo_doc VARCHAR(20) NOT NULL,
    nombre_documento VARCHAR(140) NOT NULL,
    entidad_emisora VARCHAR(80) NOT NULL,
    validado BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (empresa_codigo) REFERENCES empresas(codigo) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS liquidaciones (
    numero_liquidacion VARCHAR(20) PRIMARY KEY,
    fecha VARCHAR(20) NOT NULL,
    numero_expediente VARCHAR(20) NOT NULL,
    subtotal_municipal DECIMAL(12,2) NOT NULL,
    subtotal_central DECIMAL(12,2) NOT NULL,
    timbres_honorarios DECIMAL(12,2) NOT NULL,
    total_lps DECIMAL(12,2) NOT NULL,
    monto_recibido DECIMAL(12,2) NOT NULL,
    cambio DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (numero_expediente) REFERENCES expedientes(numero_expediente) ON DELETE CASCADE
);

-- Datos semilla iniciales (Honduras 2026)
INSERT IGNORE INTO tramites_catalogo (codigo, entidad, etapa, nombre, horas_tradicionales, horas_burok, costo_oficial_lps, plazo_sla_dias) VALUES
('TR-01', 'Notariado & IP', 'Apertura', 'Escritura Pública y Matrícula Mercantil (IP)', 120, 24, 3850.00, 5),
('TR-02', 'SAR Honduras', 'Apertura', 'Inscripción Tributaria (RTN) y Régimen Facturación CAI', 180, 16, 200.00, 3),
('TR-03', 'Alcaldía Municipal', 'Apertura', 'Compatibilidad Uso de Suelo y Catastro Municipal', 320, 20, 950.00, 10),
('TR-04', 'Alcaldía & Bomberos', 'Funcionamiento', 'Permiso de Operación y Dictamen de Bomberos', 430, 32, 2450.00, 15),
('TR-05', 'MiAmbiente', 'Funcionamiento', 'Constancia Ambiental Categoría 1 (Bajo Impacto)', 480, 24, 1500.00, 15),
('TR-06', 'ARSA', 'Funcionamiento', 'Licencia Sanitaria de Establecimiento', 403, 44, 1800.00, 15);

INSERT IGNORE INTO emprendedores (codigo, nombre_completo, dni, telefono, correo, direccion) VALUES
('EMP-001', 'Hector Joann Murillo', '0801-1994-12345', '9988-7766', 'hector.murillo@burok.hn', 'Col. Palmira, Tegucigalpa, F.M.'),
('EMP-002', 'Valeria Sofía Mendoza', '0501-1991-04521', '9844-1122', 'valeria.mendoza@valledelnorte.hn', 'Barrio Río de Piedras, San Pedro Sula, Cortés');

INSERT IGNORE INTO empresas (codigo, nombre_comercial, rtn, forma_juridica, codigo_ciiu, categoria_riesgo, municipio, capital_semilla, emprendedor_codigo) VALUES
('PYM-001', 'Inversiones Valle del Norte S. de R.L.', '08019024567891', 'Sociedad de Resp. Ltda.', '4711 - Supermercados', 1, 'San Pedro Sula', 120000.00, 'EMP-002'),
('PYM-002', 'Café & Tostaduría Maya', '08011994123450', 'Comerciante Individual', '5610 - Restaurantes y Cafés', 1, 'Distrito Central (Tegucigalpa)', 95000.00, 'EMP-001'),
('PYM-003', 'Distribuidora Sula Express S.A.', '05019018765432', 'Sociedad Anónima (S.A.)', '5229 - Logística y Carga', 2, 'Villanueva (Cortés)', 250000.00, 'EMP-002');

INSERT IGNORE INTO expediente_documentos (empresa_codigo, codigo_doc, nombre_documento, entidad_emisora, validado) VALUES
('PYM-001', 'DOC-01', 'DNI del Representante Legal', 'RNP', TRUE),
('PYM-001', 'DOC-02', 'Testimonio de Escritura Pública', 'Notario / IP', TRUE),
('PYM-001', 'DOC-03', 'Contrato de Arrendamiento Local', 'Privado / Notariado', TRUE),
('PYM-001', 'DOC-04', 'Croquis de Ubicación Catastral', 'Propietario', TRUE),
('PYM-002', 'DOC-01', 'DNI del Comerciante Individual', 'RNP', TRUE),
('PYM-002', 'DOC-02', 'Escritura de Comerciante Individual', 'Notario / IP', TRUE),
('PYM-002', 'DOC-03', 'Contrato de Arrendamiento Local', 'Privado', FALSE),
('PYM-002', 'DOC-04', 'Croquis de Ubicación Catastral', 'Propietario', FALSE);

INSERT IGNORE INTO expedientes (numero_expediente, fecha_creacion, empresa_codigo, horas_tradicionales_est, horas_burok_est, estado_global, liquidado) VALUES
('EXP-001', '2026-11-10', 'PYM-001', 1933, 160, 'EN_REVISION', FALSE),
('EXP-002', '2026-11-12', 'PYM-002', 1933, 160, 'PENDIENTE', FALSE),
('EXP-003', '2026-11-01', 'PYM-003', 1933, 160, 'APROBADO', TRUE);

INSERT IGNORE INTO expediente_tramites (numero_expediente, tramite_codigo, dias_transcurridos, estado, silencio_positivo) VALUES
('EXP-001', 'TR-01', 5, 'APROBADO', FALSE),
('EXP-001', 'TR-02', 3, 'APROBADO', FALSE),
('EXP-001', 'TR-03', 8, 'APROBADO', FALSE),
('EXP-001', 'TR-04', 11, 'EN_REVISION', FALSE),
('EXP-001', 'TR-05', 4, 'PENDIENTE', FALSE),
('EXP-001', 'TR-06', 0, 'PENDIENTE', FALSE),
('EXP-002', 'TR-01', 4, 'APROBADO', FALSE),
('EXP-002', 'TR-02', 2, 'EN_REVISION', FALSE),
('EXP-002', 'TR-03', 0, 'PENDIENTE', FALSE),
('EXP-002', 'TR-04', 0, 'PENDIENTE', FALSE),
('EXP-003', 'TR-01', 5, 'APROBADO', FALSE),
('EXP-003', 'TR-02', 3, 'APROBADO', FALSE),
('EXP-003', 'TR-03', 10, 'APROBADO', FALSE),
('EXP-003', 'TR-04', 14, 'APROBADO', FALSE),
('EXP-003', 'TR-05', 15, 'SILENCIO_POSITIVO', TRUE),
('EXP-003', 'TR-06', 12, 'APROBADO', FALSE);

INSERT IGNORE INTO liquidaciones (numero_liquidacion, fecha, numero_expediente, subtotal_municipal, subtotal_central, timbres_honorarios, total_lps, monto_recibido, cambio) VALUES
('LIQ-001', '2026-11-05', 'EXP-003', 3400.00, 7350.00, 0.00, 10750.00, 11000.00, 250.00);
