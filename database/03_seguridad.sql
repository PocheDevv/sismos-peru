-- =====================================================================
-- SismosPerú (PA4) - Seguridad
-- Solo para una base sismos_peru que ya existía antes de la PA4: agrega la
-- tabla usuario sin tocar los demás datos. Si va a ejecutar 01_schema.sql,
-- no hace falta este script (01 ya crea la tabla usuario).
-- =====================================================================
SET NAMES utf8mb4;
USE sismos_peru;

-- ---------------------------------------------------------------------
-- Usuarios (HU-19): la contraseña se guarda solo como hash BCrypt.
-- ADMIN registra, edita y elimina; CONSULTA solo puede ver.
-- Credenciales de prueba en el README.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario (
    id            BIGINT       PRIMARY KEY AUTO_INCREMENT,
    username      VARCHAR(40)  NOT NULL,
    nombre        VARCHAR(100) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    rol           VARCHAR(20)  NOT NULL,
    estado        VARCHAR(10)  NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT uq_usuario_username UNIQUE (username),
    CONSTRAINT chk_usuario_rol CHECK (rol IN ('ADMIN', 'CONSULTA')),
    CONSTRAINT chk_usuario_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

INSERT IGNORE INTO usuario (username, nombre, password_hash, rol, estado) VALUES
('admin',    'Administrador del sistema', '$2a$10$w1CcxLk1lkPc06dtmY7Ttu01WLtTo/UnF/QFNDeGmoLVT21ib5DTq', 'ADMIN',    'ACTIVO'),
('consulta', 'Usuario de consulta',       '$2a$10$UwaH4A17yY/Ylyu0d6dWvuoOstCthihQAo1cAkYBdCgkz6MG946cK', 'CONSULTA', 'ACTIVO');
