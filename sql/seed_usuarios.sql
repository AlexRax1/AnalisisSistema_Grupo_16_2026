-- Seed mínimo de usuarios para una base nueva.
-- Ejecutar después de sql/db_MuniQuejas.sql.
-- Contraseña de todos los usuarios de demostración: Muni123456
-- CAMBIAR ESTAS CONTRASEÑAS antes de exponer el sistema a Internet.
-- El hash corresponde a BCrypt de Muni123456.

BEGIN;

INSERT INTO rol_user (rol_user_id, nombre_rol) VALUES
    (1, 'CIUDADANO'),
    (2, 'FUNCIONARIO_MUNICIPAL'),
    (3, 'INSPECTOR_CAMPO'),
    (4, 'ESPECIALISTA_TECNICO'),
    (5, 'ADMINISTRADOR')
ON CONFLICT DO NOTHING;

INSERT INTO credenciales (username, password, rol_id, estado, usuario_creacion)
SELECT 'admin@municipalidad.local',
       '$2b$10$EJBaBeRfyd1lUfg4fweSqODg78lzeMh/fXbm92RnXxZzC6nziBCe6',
       rol_user_id, 'ACTIVO', 'SEED'
FROM rol_user WHERE nombre_rol = 'ADMINISTRADOR'
ON CONFLICT (username) DO NOTHING;

INSERT INTO credenciales (username, password, rol_id, estado, usuario_creacion)
SELECT 'funcionario@municipalidad.local',
       '$2b$10$EJBaBeRfyd1lUfg4fweSqODg78lzeMh/fXbm92RnXxZzC6nziBCe6',
       rol_user_id, 'ACTIVO', 'SEED'
FROM rol_user WHERE nombre_rol = 'FUNCIONARIO_MUNICIPAL'
ON CONFLICT (username) DO NOTHING;

INSERT INTO credenciales (username, password, rol_id, estado, usuario_creacion)
SELECT 'inspector@municipalidad.local',
       '$2b$10$EJBaBeRfyd1lUfg4fweSqODg78lzeMh/fXbm92RnXxZzC6nziBCe6',
       rol_user_id, 'ACTIVO', 'SEED'
FROM rol_user WHERE nombre_rol = 'INSPECTOR_CAMPO'
ON CONFLICT (username) DO NOTHING;

INSERT INTO credenciales (username, password, rol_id, estado, usuario_creacion)
SELECT 'especialista@municipalidad.local',
       '$2b$10$EJBaBeRfyd1lUfg4fweSqODg78lzeMh/fXbm92RnXxZzC6nziBCe6',
       rol_user_id, 'ACTIVO', 'SEED'
FROM rol_user WHERE nombre_rol = 'ESPECIALISTA_TECNICO'
ON CONFLICT (username) DO NOTHING;

INSERT INTO credenciales (username, password, rol_id, estado, usuario_creacion)
SELECT 'ciudadano@municipalidad.local',
       '$2b$10$EJBaBeRfyd1lUfg4fweSqODg78lzeMh/fXbm92RnXxZzC6nziBCe6',
       rol_user_id, 'ACTIVO', 'SEED'
FROM rol_user WHERE nombre_rol = 'CIUDADANO'
ON CONFLICT (username) DO NOTHING;

-- Perfil administrativo
INSERT INTO usuarios (user_id, dpi, nombres, apellidos, correo, telefono, direccion, estado, dependencia_id, usuario_creacion)
SELECT c.user_id, '1000000000101', 'Administrador', 'General', 'admin@municipalidad.local',
       '22220001', 'Palacio Municipal', 'ACTIVO', d.dependencia_id, 'SEED'
FROM credenciales c
LEFT JOIN dependencias d ON d.codigo_dependencia = 'DSP'
WHERE c.username = 'admin@municipalidad.local'
  AND NOT EXISTS (SELECT 1 FROM usuarios u WHERE u.user_id = c.user_id)
ON CONFLICT DO NOTHING;

-- Perfil de funcionario
INSERT INTO usuarios (user_id, dpi, nombres, apellidos, correo, telefono, direccion, estado, dependencia_id, usuario_creacion)
SELECT c.user_id, '1000000000102', 'Funcionario', 'Municipal', 'funcionario@municipalidad.local',
       '22220002', 'Palacio Municipal', 'ACTIVO', d.dependencia_id, 'SEED'
FROM credenciales c
LEFT JOIN dependencias d ON d.codigo_dependencia = 'DSP'
WHERE c.username = 'funcionario@municipalidad.local'
  AND NOT EXISTS (SELECT 1 FROM usuarios u WHERE u.user_id = c.user_id)
ON CONFLICT DO NOTHING;

-- Perfil de inspector de campo
INSERT INTO usuarios (user_id, dpi, nombres, apellidos, correo, telefono, direccion, estado, dependencia_id, usuario_creacion)
SELECT c.user_id, '1000000000103', 'Inspector', 'Campo', 'inspector@municipalidad.local',
       '22220003', 'Palacio Municipal', 'ACTIVO', d.dependencia_id, 'SEED'
FROM credenciales c
LEFT JOIN dependencias d ON d.codigo_dependencia = 'VIAL'
WHERE c.username = 'inspector@municipalidad.local'
  AND NOT EXISTS (SELECT 1 FROM usuarios u WHERE u.user_id = c.user_id)
ON CONFLICT DO NOTHING;

-- Perfil de especialista técnico
INSERT INTO usuarios (user_id, dpi, nombres, apellidos, correo, telefono, direccion, estado, dependencia_id, usuario_creacion)
SELECT c.user_id, '1000000000104', 'Especialista', 'Técnico', 'especialista@municipalidad.local',
       '22220004', 'Palacio Municipal', 'ACTIVO', d.dependencia_id, 'SEED'
FROM credenciales c
LEFT JOIN dependencias d ON d.codigo_dependencia = 'VIAL'
WHERE c.username = 'especialista@municipalidad.local'
  AND NOT EXISTS (SELECT 1 FROM usuarios u WHERE u.user_id = c.user_id)
ON CONFLICT DO NOTHING;

-- Perfil de ciudadano
INSERT INTO usuarios (user_id, dpi, nombres, apellidos, correo, telefono, direccion, estado, dependencia_id, usuario_creacion)
SELECT c.user_id, '1000000000105', 'Ciudadano', 'Demo', 'ciudadano@municipalidad.local',
       '22220005', 'Zona 1', 'ACTIVO', NULL, 'SEED'
FROM credenciales c
WHERE c.username = 'ciudadano@municipalidad.local'
  AND NOT EXISTS (SELECT 1 FROM usuarios u WHERE u.user_id = c.user_id)
ON CONFLICT DO NOTHING;

COMMIT;
