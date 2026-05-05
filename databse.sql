-- ═══════════════════════════════════════════════
-- SubManager · Esquema Supabase (PostgreSQL)
-- MVVM + Clean Architecture
-- ═══════════════════════════════════════════════

-- Habilitar extensión para UUID (recomendado en Supabase)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ─────────────────────────────────────────────
-- ENUM TYPES
-- ─────────────────────────────────────────────

CREATE TYPE tipo_plan_enum              AS ENUM ('MENSUAL', 'ANUAL');

CREATE TYPE ciclo_facturacion_enum      AS ENUM (
    'MENSUAL', 'BIMESTRAL', 'TRIMESTRAL', 'SEMESTRAL', 'ANUAL'
);

CREATE TYPE ciclo_servicio_enum         AS ENUM ('MENSUAL', 'BIMESTRAL');

CREATE TYPE categoria_enum              AS ENUM (
    'ENTRETENIMIENTO', 'MUSICA', 'SOFTWARE', 'TRABAJO',
    'SALUD', 'GAMING', 'EDUCACION', 'OTRO'
);

CREATE TYPE metodo_pago_enum            AS ENUM (
    'TARJETA', 'TRANSFERENCIA', 'EFECTIVO', 'PAYPAL'
);

CREATE TYPE estado_pago_enum            AS ENUM ('PENDIENTE', 'PAGADO', 'VENCIDO');

CREATE TYPE nivel_confianza_enum        AS ENUM ('ALTO', 'MEDIO', 'BAJO');

-- ─────────────────────────────────────────────
-- TABLA: usuarios (solo usuarios Premium)
-- RF-25
-- ─────────────────────────────────────────────
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombre TEXT NOT NULL,
    correo TEXT NOT NULL UNIQUE,
    hash_contrasena TEXT NOT NULL, -- bcrypt recibido vía API
    tipo_plan tipo_plan_enum NOT NULL,
    fecha_inicio_plan DATE NOT NULL, -- ej: 2025-10-01
    fecha_renovacion DATE NOT NULL, -- ej: 2026-10-01
    esta_activo BOOLEAN NOT NULL DEFAULT TRUE, -- true=activo | false=cancelado
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON
TABLE usuarios IS 'Solo existe registro si el usuario es Premium y ha iniciado sesión. RF-25';

COMMENT ON COLUMN usuarios.hash_contrasena IS 'bcrypt en servidor; hash recibido vía API';

COMMENT ON COLUMN usuarios.esta_activo IS 'true=activo | false=cancelado';

-- ─────────────────────────────────────────────
-- TABLA: configuracion_app (Singleton por usuario)
-- RNF-12, RF-23, RF-25, RF-26
-- ─────────────────────────────────────────────
CREATE TABLE configuracion_app (
    id BIGINT PRIMARY KEY DEFAULT 1 CHECK (id = 1), -- registro único
    notificaciones_habilitadas BOOLEAN NOT NULL DEFAULT TRUE, -- RF-23
    hora_notificacion SMALLINT NOT NULL DEFAULT 9 CHECK (
        hora_notificacion BETWEEN 0 AND 23
    ),
    minuto_notificacion SMALLINT NOT NULL DEFAULT 0 CHECK (
        minuto_notificacion BETWEEN 0 AND 59
    ),
    tono_notificacion TEXT NOT NULL DEFAULT 'predeterminado',
    usuario_id BIGINT REFERENCES usuarios (id) ON DELETE SET NULL,
    ultima_sincronizacion TIMESTAMPTZ -- RF-26
);

COMMENT ON
TABLE configuracion_app IS 'Registro único (id siempre = 1). RNF-12';

COMMENT ON COLUMN configuracion_app.notificaciones_habilitadas IS 'RF-23';

COMMENT ON COLUMN configuracion_app.hora_notificacion IS 'Hora de entrega 0-23. RF-23';

COMMENT ON COLUMN configuracion_app.minuto_notificacion IS 'Minuto de entrega 0-59';

COMMENT ON COLUMN configuracion_app.tono_notificacion IS 'URI del tono del sistema o "predeterminado"';

COMMENT ON COLUMN configuracion_app.usuario_id IS 'NULL si usuario básico. RF-25';

COMMENT ON COLUMN configuracion_app.ultima_sincronizacion IS 'Timestamp de la última sincronización exitosa. RF-26';

-- ─────────────────────────────────────────────
-- TABLA: suscripciones (Vida Digital)
-- SCR-02, SCR-03 · RF-01 a RF-05
-- ─────────────────────────────────────────────
CREATE TABLE suscripciones (
    id BIGSERIAL PRIMARY KEY,
    nombre TEXT NOT NULL, -- Netflix, Spotify, etc. RF-01
    monto NUMERIC(12, 2) NOT NULL,
    ciclo_facturacion ciclo_facturacion_enum NOT NULL, -- RN-03
    color TEXT NOT NULL, -- Hex ej: #2563EB
    categoria categoria_enum NOT NULL, -- RF-04
    metodo_pago metodo_pago_enum NOT NULL, -- RF-05
    fecha_primer_cobro DATE NOT NULL, -- Base para ciclos futuros. RN-03
    fecha_proximo_cobro DATE NOT NULL, -- Calculada automáticamente. RF-02
    fecha_limite_cancelacion DATE, -- Opcional. Alerta si <3 días. RF-03
    recordatorio_habilitado BOOLEAN NOT NULL DEFAULT TRUE, -- RF-21
    dias_anticipacion SMALLINT NOT NULL DEFAULT 3, -- RF-21
    notificacion_silenciada BOOLEAN NOT NULL DEFAULT FALSE, -- RF-24
    esta_activa BOOLEAN NOT NULL DEFAULT TRUE, -- false=archivada
    icono_res_id INTEGER,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON
TABLE suscripciones IS 'Módulo Vida Digital. SCR-02, SCR-03. RF-01 a RF-05';

COMMENT ON COLUMN suscripciones.color IS 'Hex ej: #2563EB. Paleta de 8 colores. RF-01';

COMMENT ON COLUMN suscripciones.fecha_primer_cobro IS 'Base para calcular ciclos futuros. RN-03';

COMMENT ON COLUMN suscripciones.fecha_proximo_cobro IS 'Calculada automáticamente al guardar o editar. RF-02';

COMMENT ON COLUMN suscripciones.fecha_limite_cancelacion IS 'Muestra alerta visual si quedan menos de 3 días. RF-03';

COMMENT ON COLUMN suscripciones.dias_anticipacion IS 'Días antes del vencimiento: 1, 3, 5 o N personalizado. RF-21';

COMMENT ON COLUMN suscripciones.notificacion_silenciada IS 'Silenciada sin borrar config. RF-24';

COMMENT ON COLUMN suscripciones.esta_activa IS 'false=archivada | true=activa';

-- ─────────────────────────────────────────────
-- TABLA: servicios_fisicos (Servicios del Hogar)
-- SCR-04, SCR-05 · RF-06 a RF-09
-- ─────────────────────────────────────────────
CREATE TABLE servicios_fisicos (
    id BIGSERIAL PRIMARY KEY,
    nombre TEXT NOT NULL, -- Luz, Agua, Gas, Renta, Internet. RF-06
    monto_estimado NUMERIC(12, 2) NOT NULL DEFAULT 0,
    monto_variable BOOLEAN NOT NULL DEFAULT FALSE, -- RF-07
    ciclo_facturacion ciclo_servicio_enum NOT NULL, -- RF-06
    fecha_proximo_cobro DATE NOT NULL,
    es_compartido BOOLEAN NOT NULL DEFAULT FALSE, -- RF-08
    monto_total_recibo NUMERIC(12, 2), -- Antes del reparto. RF-08
    monto_parte_usuario NUMERIC(12, 2), -- Total - suma aportaciones. RN-02
    recordatorio_habilitado BOOLEAN NOT NULL DEFAULT TRUE,
    dias_anticipacion SMALLINT NOT NULL DEFAULT 3,
    notificacion_silenciada BOOLEAN NOT NULL DEFAULT FALSE, -- RF-24
    ruta_imagen_comprobante TEXT, -- Ruta local del recibo. RF-13
    esta_activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON
TABLE servicios_fisicos IS 'Módulo Servicios Físicos. SCR-04, SCR-05. RF-06 a RF-09';

COMMENT ON COLUMN servicios_fisicos.monto_variable IS 'false=fijo | true=varía cada ciclo. Toggle SCR-05. RF-07';

COMMENT ON COLUMN servicios_fisicos.es_compartido IS 'false=no compartido | true=gasto compartido. RF-08';

COMMENT ON COLUMN servicios_fisicos.monto_total_recibo IS 'Monto completo del recibo antes del reparto. RF-08';

COMMENT ON COLUMN servicios_fisicos.monto_parte_usuario IS 'Parte calculada: total - suma de aportaciones de terceros. RN-02';

COMMENT ON COLUMN servicios_fisicos.notificacion_silenciada IS 'Silencia sin borrar la configuración. RF-24';

COMMENT ON COLUMN servicios_fisicos.ruta_imagen_comprobante IS 'Ruta local de la foto del recibo adjuntada. RF-13';

-- ─────────────────────────────────────────────
-- TABLA: terceros_compartidos
-- RF-09 · RN-02
-- ─────────────────────────────────────────────
CREATE TABLE terceros_compartidos (
    id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    servicio_id BIGINT NOT NULL REFERENCES servicios_fisicos (id) ON DELETE CASCADE,
    nombre_tercero TEXT NOT NULL,
    monto_aportacion NUMERIC(12, 2) NOT NULL,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON
TABLE terceros_compartidos IS 'Soporta múltiples terceros por servicio. RF-09';

COMMENT ON COLUMN terceros_compartidos.monto_aportacion IS 'RN-02: la suma de aportaciones nunca puede superar monto_total_recibo';

-- Restricción RN-02: la suma de aportaciones no puede superar monto_total_recibo
-- Se recomienda validar en la capa de aplicación o con un trigger:
CREATE OR REPLACE FUNCTION validar_aportaciones_terceros()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
    total_recibo    NUMERIC(12, 2);
    suma_aportaciones NUMERIC(12, 2);
BEGIN
    SELECT monto_total_recibo INTO total_recibo
    FROM servicios_fisicos
    WHERE id = NEW.servicio_id;

    SELECT COALESCE(SUM(monto_aportacion), 0) INTO suma_aportaciones
    FROM terceros_compartidos
    WHERE servicio_id = NEW.servicio_id
      AND id <> COALESCE(NEW.id, -1);

    IF total_recibo IS NOT NULL AND (suma_aportaciones + NEW.monto_aportacion) > total_recibo THEN
        RAISE EXCEPTION 'RN-02: La suma de aportaciones (%) supera el monto total del recibo (%)',
            (suma_aportaciones + NEW.monto_aportacion), total_recibo;
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_validar_aportaciones
BEFORE INSERT OR UPDATE ON terceros_compartidos
FOR EACH ROW EXECUTE FUNCTION validar_aportaciones_terceros();

-- ─────────────────────────────────────────────
-- TABLA: registros_pago (Historial + Estado)
-- RF-14, RF-15, RF-18 · RN-01, RN-04
-- ─────────────────────────────────────────────
CREATE TABLE registros_pago (
    id                  BIGSERIAL           PRIMARY KEY,
    suscripcion_id      BIGINT              REFERENCES suscripciones(id)   ON DELETE SET NULL,
    servicio_id         BIGINT              REFERENCES servicios_fisicos(id) ON DELETE SET NULL,
    nombre_origen       TEXT                NOT NULL,                -- Desnormalizado para historial. RF-14
    color_origen        TEXT                NOT NULL,                -- Color desnormalizado del ítem
    categoria           TEXT                NOT NULL,                -- Categoría en el momento del pago
    monto               NUMERIC(12, 2)      NOT NULL,
    estado              estado_pago_enum    NOT NULL DEFAULT 'PENDIENTE', -- RN-04
    fecha_vencimiento   DATE                NOT NULL,
    fecha_pago          TIMESTAMPTZ,                                 -- NULL hasta que el usuario pague. RF-15
    mes_facturacion     SMALLINT            NOT NULL
                                            CHECK (mes_facturacion BETWEEN 1 AND 12),
    anio_facturacion    SMALLINT            NOT NULL,
    creado_en           TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    actualizado_en      TIMESTAMPTZ         NOT NULL DEFAULT NOW(),

-- RN-01: un registro debe pertenecer a exactamente una fuente
CONSTRAINT chk_un_origen CHECK (
        (suscripcion_id IS NOT NULL AND servicio_id IS NULL)
        OR
        (suscripcion_id IS NULL AND servicio_id IS NOT NULL)
    )
);

COMMENT ON
TABLE registros_pago IS 'Un registro por ítem por ciclo de facturación. RF-14, RF-15, RF-18. RN-01, RN-04';

COMMENT ON COLUMN registros_pago.nombre_origen IS 'Desnormalizado: conserva historial si se elimina el ítem';

COMMENT ON COLUMN registros_pago.color_origen IS 'Color desnormalizado del ítem en el momento del registro';

COMMENT ON COLUMN registros_pago.monto IS 'Monto efectivo del periodo (monto_parte_usuario si es compartido)';

COMMENT ON COLUMN registros_pago.fecha_pago IS 'NULL si no ha sido pagado. RF-15';

-- ─────────────────────────────────────────────
-- TABLA: escaneos_ocr (Comprobantes · ML Kit)
-- SCR-06 · RF-10 a RF-13
-- ─────────────────────────────────────────────
CREATE TABLE escaneos_ocr (
    id BIGSERIAL PRIMARY KEY,
    servicio_id BIGINT NOT NULL REFERENCES servicios_fisicos (id) ON DELETE CASCADE,
    ruta_imagen TEXT NOT NULL, -- Ruta local de la imagen. RF-10
    monto_detectado NUMERIC(12, 2), -- NULL si no se detectó. RF-11
    fecha_detectada DATE, -- NULL si no se detectó. RF-11
    nivel_confianza nivel_confianza_enum, -- Indicador visual en SCR-06
    confirmado BOOLEAN NOT NULL DEFAULT FALSE, -- false=descartado | true=confirmado. RF-12
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON
TABLE escaneos_ocr IS 'Registro del proceso OCR por cada escaneo. RF-10 a RF-13. SCR-06';

COMMENT ON COLUMN escaneos_ocr.ruta_imagen IS 'Ruta local de la imagen capturada con la cámara. RF-10';

COMMENT ON COLUMN escaneos_ocr.monto_detectado IS 'Monto extraído por ML Kit Text Recognition. NULL si no se detectó. RF-11';

COMMENT ON COLUMN escaneos_ocr.confirmado IS 'false=descartado | true=confirmado por el usuario en SCR-06. RF-12';

-- ═══════════════════════════════════════════════
-- ÍNDICES
-- ═══════════════════════════════════════════════

-- Dashboard (RF-14, RF-16)
CREATE INDEX idx_registros_estado_vencimiento ON registros_pago (estado, fecha_vencimiento);

CREATE INDEX idx_registros_mes_anio ON registros_pago (
    mes_facturacion,
    anio_facturacion
);

CREATE INDEX idx_suscripciones_proximo_cobro ON suscripciones (fecha_proximo_cobro);

CREATE INDEX idx_servicios_proximo_cobro ON servicios_fisicos (fecha_proximo_cobro);

-- Historial por categoría (RF-18, RF-19)
CREATE INDEX idx_registros_categoria_periodo ON registros_pago (
    categoria,
    anio_facturacion,
    mes_facturacion
);

-- Búsqueda rápida de terceros por servicio
CREATE INDEX idx_terceros_servicio ON terceros_compartidos (servicio_id);

-- Escaneos por servicio
CREATE INDEX idx_escaneos_servicio ON escaneos_ocr (servicio_id);

-- ═══════════════════════════════════════════════
-- TRIGGERS: updated_at automático
-- ═══════════════════════════════════════════════

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.actualizado_en = NOW();
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_usuarios_updated
    BEFORE UPDATE ON usuarios
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_suscripciones_updated
    BEFORE UPDATE ON suscripciones
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_servicios_updated
    BEFORE UPDATE ON servicios_fisicos
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_registros_updated
    BEFORE UPDATE ON registros_pago
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ═══════════════════════════════════════════════
-- ROW LEVEL SECURITY (RLS) — Supabase
-- Ajusta las políticas según tu modelo de auth
-- ═══════════════════════════════════════════════

ALTER TABLE usuarios ENABLE ROW LEVEL SECURITY;

ALTER TABLE configuracion_app ENABLE ROW LEVEL SECURITY;

ALTER TABLE suscripciones ENABLE ROW LEVEL SECURITY;

ALTER TABLE servicios_fisicos ENABLE ROW LEVEL SECURITY;

ALTER TABLE terceros_compartidos ENABLE ROW LEVEL SECURITY;

ALTER TABLE registros_pago ENABLE ROW LEVEL SECURITY;

ALTER TABLE escaneos_ocr ENABLE ROW LEVEL SECURITY;

-- Ejemplo de política básica (ajusta según tu lógica de auth):
-- CREATE POLICY "usuario_propio" ON usuarios
--     USING (auth.uid()::text = correo);

-- 1. Añadimos el valor 'GRATIS' a tu lista de planes permitidos (ENUM)
ALTER TYPE tipo_plan_enum ADD VALUE 'GRATIS';

-- 2. Quitamos la regla de "obligatorio" (NOT NULL) para las fechas, para que acepten vacío
ALTER TABLE usuarios ALTER COLUMN fecha_inicio_plan DROP NOT NULL;

ALTER TABLE usuarios ALTER COLUMN fecha_renovacion DROP NOT NULL;

-- Cambiamos el nombre de la columna para que coincida con Android
ALTER TABLE suscripciones RENAME COLUMN icono_res_id TO icono_nombre;

-- Cambiamos el tipo de dato de Números a Texto
ALTER TABLE suscripciones ALTER COLUMN icono_nombre TYPE TEXT USING icono_nombre::text;

ALTER TABLE suscripciones ALTER COLUMN categoria TYPE text USING categoria::text;

ALTER TABLE suscripciones ALTER COLUMN ciclo_facturacion TYPE text USING ciclo_facturacion::text;

ALTER TABLE suscripciones ALTER COLUMN metodo_pago TYPE text USING metodo_pago::text;

ALTER TABLE servicios_fisicos ALTER COLUMN ciclo_facturacion TYPE text USING ciclo_facturacion::text;

-- Permitir que la aplicación lea los usuarios (necesario para el login)
CREATE POLICY "Permitir lectura publica a usuarios" ON usuarios FOR
SELECT USING (true);

ALTER TABLE usuarios DISABLE ROW LEVEL SECURITY;

-- 3. Añadir la columna de usuario_id a las tablas restantes para aislamiento de datos
ALTER TABLE servicios_fisicos ADD COLUMN IF NOT EXISTS usuario_id BIGINT REFERENCES usuarios(id) ON DELETE CASCADE;
ALTER TABLE terceros_compartidos ADD COLUMN IF NOT EXISTS usuario_id BIGINT REFERENCES usuarios(id) ON DELETE CASCADE;
ALTER TABLE registros_pago ADD COLUMN IF NOT EXISTS usuario_id BIGINT REFERENCES usuarios(id) ON DELETE CASCADE;
ALTER TABLE suscripciones ADD COLUMN IF NOT EXISTS usuario_id BIGINT REFERENCES usuarios(id) ON DELETE CASCADE;