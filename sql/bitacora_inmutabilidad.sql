-- Migración para bases existentes: completa metadatos históricos y vuelve
-- la bitácora inmutable. Ejecutar una sola vez sobre la base del sistema.
--
--   psql -U postgres -d db_analisis_sistemas2 -f sql/bitacora_inmutabilidad.sql

BEGIN;

-- 1. Descripción explícita del evento (usuario, fecha, hora e IP ya existen).
ALTER TABLE bitacora ADD COLUMN IF NOT EXISTS descripcion varchar(500);

-- 2. El user_id queda como snapshot. Sin FK, borrar una credencial no altera
--    las filas históricas de auditoría.
ALTER TABLE bitacora DROP CONSTRAINT IF EXISTS bitacora_user_id_fkey;

-- 3. Completar metadatos recuperables de filas antiguas.
UPDATE bitacora b
SET usuario_accion = c.username
FROM credenciales c
WHERE b.usuario_accion IS NULL
  AND b.user_id = c.user_id;

UPDATE bitacora
SET tabla_afectada = COALESCE(tabla_afectada, 'credenciales'),
    campo_afectado = COALESCE(campo_afectado, 'password')
WHERE accion = 'RESTABLECER_CONTRASENA';

UPDATE bitacora
SET descripcion = CONCAT_WS(' ',
        COALESCE(accion, 'EVENTO'),
        CASE WHEN tabla_afectada IS NOT NULL THEN 'en ' || tabla_afectada END,
        CASE WHEN campo_afectado IS NOT NULL THEN '(' || campo_afectado || ')' END,
        CASE WHEN id_afectado IS NOT NULL THEN ', registro ' || id_afectado END,
        CASE WHEN registro_nuevo IS NOT NULL THEN ': ' || LEFT(registro_nuevo, 200) END
    )
WHERE descripcion IS NULL;

-- 4. Bloquear cualquier modificación posterior.
DROP TRIGGER IF EXISTS trg_bitacora_sin_update_delete ON bitacora;
DROP TRIGGER IF EXISTS trg_bitacora_sin_truncate ON bitacora;

CREATE OR REPLACE FUNCTION fn_bitacora_inmutable()
RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'La bitacora es inmutable: no se permite % en %.', TG_OP, TG_TABLE_NAME;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_bitacora_sin_update_delete
BEFORE UPDATE OR DELETE ON bitacora
FOR EACH ROW EXECUTE FUNCTION fn_bitacora_inmutable();

CREATE TRIGGER trg_bitacora_sin_truncate
BEFORE TRUNCATE ON bitacora
FOR EACH STATEMENT EXECUTE FUNCTION fn_bitacora_inmutable();

-- 5. Retirar permisos de modificación al rol público. El trigger protege
--    incluso al propietario de la tabla.
REVOKE UPDATE, DELETE, TRUNCATE ON bitacora FROM PUBLIC;

COMMENT ON TABLE bitacora IS
    'Auditoría inmutable: sólo INSERT. UPDATE, DELETE y TRUNCATE bloqueados por trigger.';

COMMIT;
