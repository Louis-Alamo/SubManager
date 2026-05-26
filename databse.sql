





CREATE EXTENSION IF NOT EXISTS "pgcrypto";





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





CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombre TEXT NOT NULL,
    correo TEXT NOT NULL UNIQUE,
    hash_contrasena TEXT NOT NULL,
    tipo_plan tipo_plan_enum NOT NULL,
    fecha_inicio_plan DATE NOT NULL,
    fecha_renovacion DATE NOT NULL,
    esta_activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);












CREATE TABLE configuracion_app (
    id BIGINT PRIMARY KEY DEFAULT 1 CHECK (id = 1),
    notificaciones_habilitadas BOOLEAN NOT NULL DEFAULT TRUE,
    hora_notificacion SMALLINT NOT NULL DEFAULT 9 CHECK (
        hora_notificacion BETWEEN 0 AND 23
    ),
    minuto_notificacion SMALLINT NOT NULL DEFAULT 0 CHECK (
        minuto_notificacion BETWEEN 0 AND 59
    ),
    tono_notificacion TEXT NOT NULL DEFAULT 'predeterminado',
    usuario_id BIGINT REFERENCES usuarios (id) ON DELETE SET NULL,
    ultima_sincronizacion TIMESTAMPTZ
);




















CREATE TABLE suscripciones (
    id BIGSERIAL PRIMARY KEY,
    nombre TEXT NOT NULL,
    monto NUMERIC(12, 2) NOT NULL,
    ciclo_facturacion ciclo_facturacion_enum NOT NULL,
    color TEXT NOT NULL,
    categoria categoria_enum NOT NULL,
    metodo_pago metodo_pago_enum NOT NULL,
    fecha_primer_cobro DATE NOT NULL,
    fecha_proximo_cobro DATE NOT NULL,
    fecha_limite_cancelacion DATE,
    recordatorio_habilitado BOOLEAN NOT NULL DEFAULT TRUE,
    dias_anticipacion SMALLINT NOT NULL DEFAULT 3,
    notificacion_silenciada BOOLEAN NOT NULL DEFAULT FALSE,
    esta_activa BOOLEAN NOT NULL DEFAULT TRUE,
    icono_res_id INTEGER,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);






















CREATE TABLE servicios_fisicos (
    id BIGSERIAL PRIMARY KEY,
    nombre TEXT NOT NULL,
    monto_estimado NUMERIC(12, 2) NOT NULL DEFAULT 0,
    monto_variable BOOLEAN NOT NULL DEFAULT FALSE,
    ciclo_facturacion ciclo_servicio_enum NOT NULL,
    fecha_proximo_cobro DATE NOT NULL,
    es_compartido BOOLEAN NOT NULL DEFAULT FALSE,
    monto_total_recibo NUMERIC(12, 2),
    monto_parte_usuario NUMERIC(12, 2),
    recordatorio_habilitado BOOLEAN NOT NULL DEFAULT TRUE,
    dias_anticipacion SMALLINT NOT NULL DEFAULT 3,
    notificacion_silenciada BOOLEAN NOT NULL DEFAULT FALSE,
    ruta_imagen_comprobante TEXT,
    esta_activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);




















CREATE TABLE terceros_compartidos (
    id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    servicio_id BIGINT NOT NULL REFERENCES servicios_fisicos (id) ON DELETE CASCADE,
    nombre_tercero TEXT NOT NULL,
    monto_aportacion NUMERIC(12, 2) NOT NULL,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);








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





CREATE TABLE registros_pago (
    id                  BIGSERIAL           PRIMARY KEY,
    suscripcion_id      BIGINT              REFERENCES suscripciones(id)   ON DELETE SET NULL,
    servicio_id         BIGINT              REFERENCES servicios_fisicos(id) ON DELETE SET NULL,
    nombre_origen       TEXT                NOT NULL,
    color_origen        TEXT                NOT NULL,
    categoria           TEXT                NOT NULL,
    monto               NUMERIC(12, 2)      NOT NULL,
    estado              estado_pago_enum    NOT NULL DEFAULT 'PENDIENTE',
    fecha_vencimiento   DATE                NOT NULL,
    fecha_pago          TIMESTAMPTZ,
    mes_facturacion     SMALLINT            NOT NULL
                                            CHECK (mes_facturacion BETWEEN 1 AND 12),
    anio_facturacion    SMALLINT            NOT NULL,
    creado_en           TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    actualizado_en      TIMESTAMPTZ         NOT NULL DEFAULT NOW(),


CONSTRAINT chk_un_origen CHECK (
        (suscripcion_id IS NOT NULL AND servicio_id IS NULL)
        OR
        (suscripcion_id IS NULL AND servicio_id IS NOT NULL)
    )
);
















CREATE TABLE escaneos_ocr (
    id BIGSERIAL PRIMARY KEY,
    servicio_id BIGINT NOT NULL REFERENCES servicios_fisicos (id) ON DELETE CASCADE,
    ruta_imagen TEXT NOT NULL,
    monto_detectado NUMERIC(12, 2),
    fecha_detectada DATE,
    nivel_confianza nivel_confianza_enum,
    confirmado BOOLEAN NOT NULL DEFAULT FALSE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);















CREATE INDEX idx_registros_estado_vencimiento ON registros_pago (estado, fecha_vencimiento);

CREATE INDEX idx_registros_mes_anio ON registros_pago (
    mes_facturacion,
    anio_facturacion
);

CREATE INDEX idx_suscripciones_proximo_cobro ON suscripciones (fecha_proximo_cobro);

CREATE INDEX idx_servicios_proximo_cobro ON servicios_fisicos (fecha_proximo_cobro);


CREATE INDEX idx_registros_categoria_periodo ON registros_pago (
    categoria,
    anio_facturacion,
    mes_facturacion
);


CREATE INDEX idx_terceros_servicio ON terceros_compartidos (servicio_id);


CREATE INDEX idx_escaneos_servicio ON escaneos_ocr (servicio_id);





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






ALTER TABLE usuarios ENABLE ROW LEVEL SECURITY;

ALTER TABLE configuracion_app ENABLE ROW LEVEL SECURITY;

ALTER TABLE suscripciones ENABLE ROW LEVEL SECURITY;

ALTER TABLE servicios_fisicos ENABLE ROW LEVEL SECURITY;

ALTER TABLE terceros_compartidos ENABLE ROW LEVEL SECURITY;

ALTER TABLE registros_pago ENABLE ROW LEVEL SECURITY;

ALTER TABLE escaneos_ocr ENABLE ROW LEVEL SECURITY;






ALTER TYPE tipo_plan_enum ADD VALUE 'GRATIS';


ALTER TABLE usuarios ALTER COLUMN fecha_inicio_plan DROP NOT NULL;

ALTER TABLE usuarios ALTER COLUMN fecha_renovacion DROP NOT NULL;


ALTER TABLE suscripciones RENAME COLUMN icono_res_id TO icono_nombre;


ALTER TABLE suscripciones ALTER COLUMN icono_nombre TYPE TEXT USING icono_nombre::text;

ALTER TABLE suscripciones ALTER COLUMN categoria TYPE text USING categoria::text;

ALTER TABLE suscripciones ALTER COLUMN ciclo_facturacion TYPE text USING ciclo_facturacion::text;

ALTER TABLE suscripciones ALTER COLUMN metodo_pago TYPE text USING metodo_pago::text;

ALTER TABLE servicios_fisicos ALTER COLUMN ciclo_facturacion TYPE text USING ciclo_facturacion::text;


CREATE POLICY "Permitir lectura publica a usuarios" ON usuarios FOR
SELECT USING (true);

ALTER TABLE usuarios DISABLE ROW LEVEL SECURITY;


ALTER TABLE servicios_fisicos ADD COLUMN IF NOT EXISTS usuario_id BIGINT REFERENCES usuarios(id) ON DELETE CASCADE;
ALTER TABLE terceros_compartidos ADD COLUMN IF NOT EXISTS usuario_id BIGINT REFERENCES usuarios(id) ON DELETE CASCADE;
ALTER TABLE registros_pago ADD COLUMN IF NOT EXISTS usuario_id BIGINT REFERENCES usuarios(id) ON DELETE CASCADE;
ALTER TABLE suscripciones ADD COLUMN IF NOT EXISTS usuario_id BIGINT REFERENCES usuarios(id) ON DELETE CASCADE;