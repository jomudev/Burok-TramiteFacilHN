-- Script de base de datos para Microsoft SQL Server (T-SQL / SSMS / LocalDB)
IF DB_ID('burok_db') IS NULL
BEGIN
    CREATE DATABASE burok_db;
END
GO

USE burok_db;
GO

IF OBJECT_ID('dbo.emprendedores', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.emprendedores (
        codigo VARCHAR(20) PRIMARY KEY,
        nombre_completo NVARCHAR(120) NOT NULL,
        dni VARCHAR(25) NOT NULL UNIQUE,
        telefono VARCHAR(25) NOT NULL,
        correo VARCHAR(100) NOT NULL,
        direccion NVARCHAR(200) NOT NULL
    );
END
GO

IF OBJECT_ID('dbo.empresas', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.empresas (
        codigo VARCHAR(20) PRIMARY KEY,
        nombre_comercial NVARCHAR(150) NOT NULL,
        rtn VARCHAR(25) NOT NULL,
        forma_juridica NVARCHAR(60) NOT NULL,
        codigo_ciiu NVARCHAR(80) NOT NULL,
        categoria_riesgo INT NOT NULL,
        municipio NVARCHAR(80) NOT NULL,
        capital_semilla DECIMAL(12,2) NOT NULL,
        emprendedor_codigo VARCHAR(20) NOT NULL,
        CONSTRAINT FK_empresas_emprendedores FOREIGN KEY (emprendedor_codigo)
            REFERENCES dbo.emprendedores(codigo) ON DELETE CASCADE
    );
END
GO

IF OBJECT_ID('dbo.tramites_catalogo', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.tramites_catalogo (
        codigo VARCHAR(20) PRIMARY KEY,
        entidad NVARCHAR(80) NOT NULL,
        etapa NVARCHAR(40) NOT NULL,
        nombre NVARCHAR(160) NOT NULL,
        horas_tradicionales INT NOT NULL,
        horas_burok INT NOT NULL,
        costo_oficial_lps DECIMAL(12,2) NOT NULL,
        plazo_sla_dias INT NOT NULL
    );
END
GO

IF OBJECT_ID('dbo.expedientes', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.expedientes (
        numero_expediente VARCHAR(20) PRIMARY KEY,
        fecha_creacion VARCHAR(20) NOT NULL,
        empresa_codigo VARCHAR(20) NOT NULL,
        horas_tradicionales_est INT NOT NULL,
        horas_burok_est INT NOT NULL,
        estado_global VARCHAR(30) NOT NULL,
        liquidado BIT NOT NULL DEFAULT 0,
        CONSTRAINT FK_expedientes_empresas FOREIGN KEY (empresa_codigo)
            REFERENCES dbo.empresas(codigo) ON DELETE CASCADE
    );
END
GO

IF OBJECT_ID('dbo.expediente_tramites', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.expediente_tramites (
        id INT IDENTITY(1,1) PRIMARY KEY,
        numero_expediente VARCHAR(20) NOT NULL,
        tramite_codigo VARCHAR(20) NOT NULL,
        dias_transcurridos INT NOT NULL DEFAULT 0,
        estado VARCHAR(30) NOT NULL,
        silencio_positivo BIT NOT NULL DEFAULT 0,
        CONSTRAINT FK_exptram_expedientes FOREIGN KEY (numero_expediente)
            REFERENCES dbo.expedientes(numero_expediente) ON DELETE CASCADE,
        CONSTRAINT FK_exptram_tramites FOREIGN KEY (tramite_codigo)
            REFERENCES dbo.tramites_catalogo(codigo)
    );
END
GO

IF OBJECT_ID('dbo.expediente_documentos', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.expediente_documentos (
        id INT IDENTITY(1,1) PRIMARY KEY,
        empresa_codigo VARCHAR(20) NOT NULL,
        codigo_doc VARCHAR(20) NOT NULL,
        nombre_documento NVARCHAR(140) NOT NULL,
        entidad_emisora NVARCHAR(80) NOT NULL,
        validado BIT NOT NULL DEFAULT 0,
        CONSTRAINT FK_expdocs_empresas FOREIGN KEY (empresa_codigo)
            REFERENCES dbo.empresas(codigo) ON DELETE CASCADE
    );
END
GO

IF OBJECT_ID('dbo.liquidaciones', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.liquidaciones (
        numero_liquidacion VARCHAR(20) PRIMARY KEY,
        fecha VARCHAR(20) NOT NULL,
        numero_expediente VARCHAR(20) NOT NULL,
        subtotal_municipal DECIMAL(12,2) NOT NULL,
        subtotal_central DECIMAL(12,2) NOT NULL,
        timbres_honorarios DECIMAL(12,2) NOT NULL,
        total_lps DECIMAL(12,2) NOT NULL,
        monto_recibido DECIMAL(12,2) NOT NULL,
        cambio DECIMAL(12,2) NOT NULL,
        CONSTRAINT FK_liquidaciones_expedientes FOREIGN KEY (numero_expediente)
            REFERENCES dbo.expedientes(numero_expediente) ON DELETE CASCADE
    );
END
GO

-- Datos semilla iniciales (Honduras 2026)
IF NOT EXISTS (SELECT 1 FROM dbo.tramites_catalogo WHERE codigo = 'TR-01')
INSERT INTO dbo.tramites_catalogo (codigo, entidad, etapa, nombre, horas_tradicionales, horas_burok, costo_oficial_lps, plazo_sla_dias) VALUES
('TR-01', N'Notariado & IP', N'Apertura', N'Escritura Pública y Matrícula Mercantil (IP)', 120, 24, 3850.00, 5),
('TR-02', N'SAR Honduras', N'Apertura', N'Inscripción Tributaria (RTN) y Régimen Facturación CAI', 180, 16, 200.00, 3),
('TR-03', N'Alcaldía Municipal', N'Apertura', N'Compatibilidad Uso de Suelo y Catastro Municipal', 320, 20, 950.00, 10),
('TR-04', N'Alcaldía & Bomberos', N'Funcionamiento', N'Permiso de Operación y Dictamen de Bomberos', 430, 32, 2450.00, 15),
('TR-05', N'MiAmbiente', N'Funcionamiento', N'Constancia Ambiental Categoría 1 (Bajo Impacto)', 480, 24, 1500.00, 15),
('TR-06', N'ARSA', N'Funcionamiento', N'Licencia Sanitaria de Establecimiento', 403, 44, 1800.00, 15);
GO

IF NOT EXISTS (SELECT 1 FROM dbo.emprendedores WHERE codigo = 'EMP-001')
INSERT INTO dbo.emprendedores (codigo, nombre_completo, dni, telefono, correo, direccion) VALUES
('EMP-001', N'Hector Joann Murillo', '0801-1994-12345', '9988-7766', 'hector.murillo@burok.hn', N'Col. Palmira, Tegucigalpa, F.M.'),
('EMP-002', N'Valeria Sofía Mendoza', '0501-1991-04521', '9844-1122', 'valeria.mendoza@valledelnorte.hn', N'Barrio Río de Piedras, San Pedro Sula, Cortés');
GO

IF NOT EXISTS (SELECT 1 FROM dbo.empresas WHERE codigo = 'PYM-001')
INSERT INTO dbo.empresas (codigo, nombre_comercial, rtn, forma_juridica, codigo_ciiu, categoria_riesgo, municipio, capital_semilla, emprendedor_codigo) VALUES
('PYM-001', N'Inversiones Valle del Norte S. de R.L.', '08019024567891', N'Sociedad de Resp. Ltda.', N'4711 - Supermercados', 1, N'San Pedro Sula', 120000.00, 'EMP-002'),
('PYM-002', N'Café & Tostaduría Maya', '08011994123450', N'Comerciante Individual', N'5610 - Restaurantes y Cafés', 1, N'Distrito Central (Tegucigalpa)', 95000.00, 'EMP-001'),
('PYM-003', N'Distribuidora Sula Express S.A.', '05019018765432', N'Sociedad Anónima (S.A.)', N'5229 - Logística y Carga', 2, N'Villanueva (Cortés)', 250000.00, 'EMP-002');
GO

IF NOT EXISTS (SELECT 1 FROM dbo.expediente_documentos WHERE empresa_codigo = 'PYM-001')
INSERT INTO dbo.expediente_documentos (empresa_codigo, codigo_doc, nombre_documento, entidad_emisora, validado) VALUES
('PYM-001', 'DOC-01', N'DNI del Representante Legal', N'RNP', 1),
('PYM-001', 'DOC-02', N'Testimonio de Escritura Pública', N'Notario / IP', 1),
('PYM-001', 'DOC-03', N'Contrato de Arrendamiento Local', N'Privado / Notariado', 1),
('PYM-001', 'DOC-04', N'Croquis de Ubicación Catastral', N'Propietario', 1),
('PYM-002', 'DOC-01', N'DNI del Comerciante Individual', N'RNP', 1),
('PYM-002', 'DOC-02', N'Escritura de Comerciante Individual', N'Notario / IP', 1),
('PYM-002', 'DOC-03', N'Contrato de Arrendamiento Local', N'Privado', 0),
('PYM-002', 'DOC-04', N'Croquis de Ubicación Catastral', N'Propietario', 0);
GO

IF NOT EXISTS (SELECT 1 FROM dbo.expedientes WHERE numero_expediente = 'EXP-001')
INSERT INTO dbo.expedientes (numero_expediente, fecha_creacion, empresa_codigo, horas_tradicionales_est, horas_burok_est, estado_global, liquidado) VALUES
('EXP-001', '2026-11-10', 'PYM-001', 1933, 160, 'EN_REVISION', 0),
('EXP-002', '2026-11-12', 'PYM-002', 1933, 160, 'PENDIENTE', 0),
('EXP-003', '2026-11-01', 'PYM-003', 1933, 160, 'APROBADO', 1);
GO

IF NOT EXISTS (SELECT 1 FROM dbo.expediente_tramites WHERE numero_expediente = 'EXP-001')
INSERT INTO dbo.expediente_tramites (numero_expediente, tramite_codigo, dias_transcurridos, estado, silencio_positivo) VALUES
('EXP-001', 'TR-01', 5, 'APROBADO', 0),
('EXP-001', 'TR-02', 3, 'APROBADO', 0),
('EXP-001', 'TR-03', 8, 'APROBADO', 0),
('EXP-001', 'TR-04', 11, 'EN_REVISION', 0),
('EXP-001', 'TR-05', 4, 'PENDIENTE', 0),
('EXP-001', 'TR-06', 0, 'PENDIENTE', 0),
('EXP-002', 'TR-01', 4, 'APROBADO', 0),
('EXP-002', 'TR-02', 2, 'EN_REVISION', 0),
('EXP-002', 'TR-03', 0, 'PENDIENTE', 0),
('EXP-002', 'TR-04', 0, 'PENDIENTE', 0),
('EXP-003', 'TR-01', 5, 'APROBADO', 0),
('EXP-003', 'TR-02', 3, 'APROBADO', 0),
('EXP-003', 'TR-03', 10, 'APROBADO', 0),
('EXP-003', 'TR-04', 14, 'APROBADO', 0),
('EXP-003', 'TR-05', 15, 'SILENCIO_POSITIVO', 1),
('EXP-003', 'TR-06', 12, 'APROBADO', 0);
GO

IF NOT EXISTS (SELECT 1 FROM dbo.liquidaciones WHERE numero_liquidacion = 'LIQ-001')
INSERT INTO dbo.liquidaciones (numero_liquidacion, fecha, numero_expediente, subtotal_municipal, subtotal_central, timbres_honorarios, total_lps, monto_recibido, cambio) VALUES
('LIQ-001', '2026-11-05', 'EXP-003', 3400.00, 7350.00, 0.00, 10750.00, 11000.00, 250.00);
GO
