-- =====================================================================
-- SismosPerú (PA3) - Esquema de la base de datos
-- Ejecutar una sola vez (o cada vez que se quiera empezar de cero).
-- Los catálogos (ubigeo, tipo_alerta, tipo_dano) se cargan después desde
-- la pantalla "Importar CSV" de la aplicación.
-- =====================================================================
SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS sismos_peru CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sismos_peru;

DROP TABLE IF EXISTS seguimiento_sismo;
DROP TABLE IF EXISTS reporte_dano;
DROP TABLE IF EXISTS sismo;
DROP TABLE IF EXISTS estacion_monitoreo;
DROP TABLE IF EXISTS tipo_dano;
DROP TABLE IF EXISTS tipo_alerta;
DROP TABLE IF EXISTS ubigeo;

-- ---------------------------------------------------------------------
-- Ubigeo: catálogo jerárquico (departamento > provincia > distrito).
-- ubigeo.csv carga los departamentos; provincias y distritos se registran
-- desde la pantalla de Ubigeos (HU-09).
-- ---------------------------------------------------------------------
CREATE TABLE ubigeo (
    id        BIGINT       PRIMARY KEY AUTO_INCREMENT,
    codigo    VARCHAR(6)   NOT NULL,
    nombre    VARCHAR(80)  NOT NULL,
    tipo      VARCHAR(30)  NOT NULL,
    estado    VARCHAR(10)  NOT NULL DEFAULT 'ACTIVO',
    padre_id  BIGINT       NULL,
    CONSTRAINT uq_ubigeo_codigo UNIQUE (codigo),
    CONSTRAINT fk_ubigeo_padre FOREIGN KEY (padre_id) REFERENCES ubigeo (id),
    CONSTRAINT chk_ubigeo_tipo CHECK (tipo IN ('DEPARTAMENTO', 'PROVINCIA_CONSTITUCIONAL', 'PROVINCIA', 'DISTRITO')),
    CONSTRAINT chk_ubigeo_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

-- ---------------------------------------------------------------------
-- Tipo de alerta: TipoAlerta 1 ── N Sismo
-- ---------------------------------------------------------------------
CREATE TABLE tipo_alerta (
    id          BIGINT       PRIMARY KEY AUTO_INCREMENT,
    codigo      VARCHAR(10)  NOT NULL,
    nombre      VARCHAR(60)  NOT NULL,
    color       VARCHAR(15)  NOT NULL,
    prioridad   INT          NOT NULL,
    descripcion VARCHAR(200) NULL,
    estado      VARCHAR(10)  NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT uq_tipo_alerta_codigo UNIQUE (codigo),
    CONSTRAINT chk_tipo_alerta_color CHECK (color IN ('VERDE', 'AMARILLO', 'NARANJA', 'ROJO')),
    CONSTRAINT chk_tipo_alerta_prioridad CHECK (prioridad > 0),
    CONSTRAINT chk_tipo_alerta_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

-- ---------------------------------------------------------------------
-- Tipo de daño: TipoDano 1 ── N ReporteDano
-- ---------------------------------------------------------------------
CREATE TABLE tipo_dano (
    id          BIGINT       PRIMARY KEY AUTO_INCREMENT,
    codigo      VARCHAR(10)  NOT NULL,
    nombre      VARCHAR(60)  NOT NULL,
    sector      VARCHAR(40)  NOT NULL,
    descripcion VARCHAR(200) NULL,
    estado      VARCHAR(10)  NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT uq_tipo_dano_codigo UNIQUE (codigo),
    CONSTRAINT chk_tipo_dano_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

-- ---------------------------------------------------------------------
-- Estaciones de monitoreo: Ubigeo 1 ── N Estación
-- ---------------------------------------------------------------------
CREATE TABLE estacion_monitoreo (
    id                BIGINT       PRIMARY KEY AUTO_INCREMENT,
    codigo            VARCHAR(10)  NOT NULL,
    nombre            VARCHAR(100) NOT NULL,
    ubigeo_id         BIGINT       NOT NULL,
    latitud           DOUBLE       NOT NULL,
    longitud          DOUBLE       NOT NULL,
    fecha_instalacion DATE         NOT NULL,
    estado            VARCHAR(10)  NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT uq_estacion_codigo UNIQUE (codigo),
    CONSTRAINT fk_estacion_ubigeo FOREIGN KEY (ubigeo_id) REFERENCES ubigeo (id),
    CONSTRAINT chk_estacion_estado CHECK (estado IN ('ACTIVO', 'INACTIVO')),
    CONSTRAINT chk_estacion_latitud CHECK (latitud BETWEEN -90 AND 90),
    CONSTRAINT chk_estacion_longitud CHECK (longitud BETWEEN -180 AND 180)
);

-- ---------------------------------------------------------------------
-- Sismos: Ubigeo 1 ── N Sismo, TipoAlerta 1 ── N Sismo,
--         Estación 1 ── N Sismo (la estación es opcional)
-- ---------------------------------------------------------------------
CREATE TABLE sismo (
    id             BIGINT       PRIMARY KEY AUTO_INCREMENT,
    codigo         VARCHAR(20)  NOT NULL,
    fecha_hora     DATETIME     NOT NULL,
    magnitud       DOUBLE       NOT NULL,
    profundidad    DOUBLE       NOT NULL,
    latitud        DOUBLE       NOT NULL,
    longitud       DOUBLE       NOT NULL,
    referencia     VARCHAR(150) NULL,
    intensidad     VARCHAR(10)  NULL,
    estado         VARCHAR(20)  NOT NULL DEFAULT 'Registrado',
    relevante      BOOLEAN      NOT NULL DEFAULT FALSE,
    ubigeo_id      BIGINT       NOT NULL,
    tipo_alerta_id BIGINT       NOT NULL,
    estacion_id    BIGINT       NULL,
    CONSTRAINT uq_sismo_codigo UNIQUE (codigo),
    CONSTRAINT fk_sismo_ubigeo FOREIGN KEY (ubigeo_id) REFERENCES ubigeo (id),
    CONSTRAINT fk_sismo_tipo_alerta FOREIGN KEY (tipo_alerta_id) REFERENCES tipo_alerta (id),
    CONSTRAINT fk_sismo_estacion FOREIGN KEY (estacion_id) REFERENCES estacion_monitoreo (id),
    CONSTRAINT chk_sismo_magnitud CHECK (magnitud > 0),
    CONSTRAINT chk_sismo_profundidad CHECK (profundidad >= 0),
    CONSTRAINT chk_sismo_latitud CHECK (latitud BETWEEN -90 AND 90),
    CONSTRAINT chk_sismo_longitud CHECK (longitud BETWEEN -180 AND 180),
    CONSTRAINT chk_sismo_estado CHECK (estado IN ('Registrado', 'En evaluación', 'En seguimiento', 'Cerrado'))
);

CREATE INDEX ix_sismo_fecha_hora ON sismo (fecha_hora);
CREATE INDEX ix_sismo_magnitud ON sismo (magnitud);
CREATE INDEX ix_sismo_estado ON sismo (estado);

-- ---------------------------------------------------------------------
-- Reportes de daño: Sismo 1 ── N ReporteDano, TipoDano 1 ── N ReporteDano
-- Al eliminar un sismo se eliminan también sus reportes.
-- ---------------------------------------------------------------------
CREATE TABLE reporte_dano (
    id                BIGINT       PRIMARY KEY AUTO_INCREMENT,
    sismo_id          BIGINT       NOT NULL,
    tipo_dano_id      BIGINT       NOT NULL,
    fecha_reporte     DATETIME     NOT NULL,
    cantidad_afectada INT          NOT NULL,
    descripcion       VARCHAR(500) NOT NULL,
    nivel             VARCHAR(10)  NOT NULL DEFAULT 'MEDIO',
    estado            VARCHAR(12)  NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT fk_reporte_sismo FOREIGN KEY (sismo_id) REFERENCES sismo (id) ON DELETE CASCADE,
    CONSTRAINT fk_reporte_tipo_dano FOREIGN KEY (tipo_dano_id) REFERENCES tipo_dano (id),
    CONSTRAINT chk_reporte_cantidad CHECK (cantidad_afectada >= 0),
    CONSTRAINT chk_reporte_nivel CHECK (nivel IN ('BAJO', 'MEDIO', 'ALTO', 'CRITICO')),
    CONSTRAINT chk_reporte_estado CHECK (estado IN ('PENDIENTE', 'VERIFICADO', 'CERRADO'))
);

-- ---------------------------------------------------------------------
-- Seguimiento: Sismo 1 ── N Seguimiento (bitácora de un sismo relevante)
-- ---------------------------------------------------------------------
CREATE TABLE seguimiento_sismo (
    id                  BIGINT       PRIMARY KEY AUTO_INCREMENT,
    sismo_id            BIGINT       NOT NULL,
    fecha_actualizacion DATETIME     NOT NULL,
    responsable         VARCHAR(100) NOT NULL,
    observaciones       VARCHAR(500) NOT NULL,
    acciones_realizadas VARCHAR(500) NOT NULL,
    CONSTRAINT fk_seguimiento_sismo FOREIGN KEY (sismo_id) REFERENCES sismo (id) ON DELETE CASCADE
);
