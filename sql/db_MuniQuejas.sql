CREATE TABLE rol_user (
    rol_user_id serial PRIMARY KEY,
    nombre_rol varchar(50) NOT NULL UNIQUE
);

INSERT INTO rol_user (nombre_rol) VALUES
('CIUDADANO'),
('FUNCIONARIO_MUNICIPAL'),
('INSPECTOR_CAMPO'),
('ESPECIALISTA_TECNICO'),
('ADMINISTRADOR');

CREATE TABLE credenciales (
    user_id serial PRIMARY KEY,
    username varchar(200) UNIQUE NOT NULL,
    password varchar(255) NOT NULL,
    rol_id int NOT NULL REFERENCES rol_user(rol_user_id),
    estado varchar(20) DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO', 'INACTIVO', 'BLOQUEADO')),
    fecha_creacion timestamp DEFAULT CURRENT_TIMESTAMP,
    usuario_creacion varchar(150),
    fecha_modificacion timestamp,
    usuario_modificacion varchar(150)
);

CREATE TABLE bitacora (
    registro_id serial PRIMARY KEY,
    user_id int REFERENCES credenciales(user_id),
    microservicio_afectado varchar(50),
    endpoint varchar(150),
    accion varchar(200),
    id_afectado varchar(150),
    fecha_hora timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE recuperacion_password (
    recuperacion_id serial PRIMARY KEY,
    user_id int NOT NULL REFERENCES credenciales(user_id),
    codigo_verificacion varchar(10) NOT NULL,
    fecha_expiracion timestamp NOT NULL,
    utilizado boolean DEFAULT FALSE,
    fecha_creacion timestamp DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE dependencias (
    dependencia_id serial PRIMARY KEY,
    nombre_dependencia varchar(100) NOT NULL UNIQUE,
    codigo_dependencia varchar(20) UNIQUE NOT NULL,
    descripcion text,
    activo boolean DEFAULT TRUE
);

INSERT INTO dependencias (nombre_dependencia, codigo_dependencia, descripcion) VALUES
('Dirección de Servicios Públicos', 'DSP', 'Coordinación general de servicios municipales'),
('Unidad de Alumbrado Público', 'ALUM', 'Red eléctrica y luminarias públicas'),
('Fontanería y Drenajes', 'DREN', 'Alcantarillado, tragantes y fugas de agua'),
('Bacheo y Pavimentación', 'VIAL', 'Mantenimiento de cinta asfáltica y adoquines'),
('Limpieza y Recolección', 'RESID', 'Manejo de desechos sólidos y basureros'),
('Parques y Ornato', 'ORNAT', 'Áreas verdes, poda y mobiliario urbano');

CREATE TABLE usuarios (
    usuario_id serial PRIMARY KEY,
    user_id int REFERENCES credenciales(user_id),
    dpi varchar(13) UNIQUE NOT NULL,
    nombres varchar(100) NOT NULL,
    apellidos varchar(100) NOT NULL,
    correo varchar(150) UNIQUE NOT NULL,
    telefono varchar(8) NOT NULL,
    direccion varchar(200),
    estado varchar(20) DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO', 'INACTIVO', 'SUSPENDIDO')),
    dependencia_id int REFERENCES dependencias(dependencia_id),
    fecha_creacion timestamp DEFAULT CURRENT_TIMESTAMP,
    usuario_creacion varchar(150),
    fecha_modificacion timestamp,
    usuario_modificacion varchar(150)
);


CREATE TABLE categorias_queja (
    categoria_id serial PRIMARY KEY,
    nombre_categoria varchar(100) NOT NULL UNIQUE,
    descripcion varchar(255),
    estado varchar(20) DEFAULT 'ACTIVO'
);

CREATE TABLE subcategorias_queja (
    subcategoria_id serial PRIMARY KEY,
    categoria_id int NOT NULL REFERENCES categorias_queja(categoria_id),
    nombre_subcategoria varchar(100) NOT NULL,
    estado varchar(20) DEFAULT 'ACTIVO'
);

INSERT INTO categorias_queja (categoria_id, nombre_categoria, descripcion) VALUES
(1, 'Alumbrado Público', 'Problemas relacionados con luminarias, postes y cableado eléctrico'),
(2, 'Drenajes y Alcantarillado', 'Problemas relacionados con drenajes, tragantes y fugas de agua'),
(3, 'Vialidad y Espacios Públicos', 'Problemas relacionados con calles, banquetas y baches'),
(4, 'Limpieza y Áreas Verdes', 'Problemas relacionados con residuos, basureros y áreas públicas');

INSERT INTO subcategorias_queja (categoria_id, nombre_subcategoria) VALUES
-- Alumbrado Público
(1, 'Luminaria apagada / quemada'),
(1, 'Poste inclinado o dañado'),
(1, 'Cableado expuesto'),
-- Drenajes y Alcantarillado
(2, 'Tragante obstruido'),
(2, 'Inundación por drenaje'),
(2, 'Falta de tapadera de alcantarilla'),
(2, 'Mal olor proveniente de drenaje'),
-- Vialidad y Espacios Públicos
(3, 'Bache en calle'),
(3, 'Señalización vial dañada'),
(3, 'Banqueta dañada'),
(3, 'Obstáculo en vía pública'),
-- Limpieza y Áreas Verdes
(4, 'Acumulación de basura en vía pública'),
(4, 'Basurero clandestino'),
(4, 'Problema con recolección de residuos'),
(4, 'Árbol con riesgo de caída');


CREATE TABLE quejas (
    queja_id serial PRIMARY KEY,
    correlativo varchar(30) UNIQUE NOT NULL,
    tipo_registro varchar(20) DEFAULT 'PRINCIPAL' CHECK (
        tipo_registro IN ('PRINCIPAL', 'AGRAVAMIENTO', 'REINCIDENCIA')
    ),
    queja_origen_id int REFERENCES quejas(queja_id) ON DELETE SET NULL,
    ciudadano_id int NOT NULL REFERENCES usuarios(usuario_id),
    categoria_id int NOT NULL REFERENCES categorias_queja(categoria_id),
    subcategoria_id int REFERENCES subcategorias_queja(subcategoria_id),
    direccion_exacta text NOT NULL,
    punto_referencia text,
    latitud numeric(10, 7) NOT NULL,
    longitud numeric(10, 7) NOT NULL,
    descripcion text NOT NULL,
    estado_actual varchar(40) NOT NULL DEFAULT 'REGISTRADA' CHECK (
        estado_actual IN (
            'REGISTRADA',
            'EN INSPECCIÓN',
            'EN REPARACIÓN TÉCNICA',
            'EN VALIDACIÓN DE REPARACIÓN',
            'PENDIENTE DE CIERRE',
            'SOLUCIONADA / CERRADA',
            'RECHAZADA'
        )
    ),
    prioridad_sugerida varchar(20) DEFAULT 'MEDIA' CHECK (prioridad_sugerida IN ('BAJA', 'MEDIA', 'ALTA', 'URGENTE')),
    prioridad_confirmada varchar(20) DEFAULT 'MEDIA' CHECK (prioridad_confirmada IN ('BAJA', 'MEDIA', 'ALTA', 'URGENTE')),
    funcionario_id int REFERENCES usuarios(usuario_id),
    inspector_id int REFERENCES usuarios(usuario_id),
    especialista_id int REFERENCES usuarios(usuario_id),
    dependencia_asignada_id int REFERENCES dependencias(dependencia_id),
    motivo_rechazo text,
    fecha_cierre timestamp,
    fecha_registro timestamp DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion timestamp,
    CONSTRAINT chk_descripcion_longitud CHECK (length(descripcion) >= 20 AND length(descripcion) <= 500)
);


CREATE TABLE informes_queja (
    informe_id serial PRIMARY KEY,
    queja_id int NOT NULL REFERENCES quejas(queja_id) ON DELETE CASCADE,
    tipo_informe varchar(30) NOT NULL CHECK (
        tipo_informe IN (
            'INSPECCION_INICIAL',
            'SOLUCION_TECNICA',
            'DICTAMEN_CONFORMIDAD',
            'CIERRE_ADMINISTRATIVO'
        )
    ),
    autor_id int NOT NULL REFERENCES usuarios(usuario_id),
    problema_verificado boolean,
    gravedad varchar(20) CHECK (gravedad IN ('LEVE', 'MODERADA', 'GRAVE', 'CRITICA')),
    recursos_sugeridos text,
    instrucciones_cuadrilla text,
    materiales_utilizados text,
    horas_trabajadas numeric(5, 2),
    fecha_fin_trabajo timestamp,
    dictamen_calidad varchar(20) CHECK (dictamen_calidad IN ('CONFORME', 'NO_CONFORME')),
    descripcion text NOT NULL,
    fecha_registro timestamp DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_informe_desc_min CHECK (length(descripcion) >= 20)
);

CREATE TABLE historial_estados_queja (
    historial_id serial PRIMARY KEY,
    queja_id int NOT NULL REFERENCES quejas(queja_id) ON DELETE CASCADE,
    estado_anterior varchar(40),
    estado_nuevo varchar(40) NOT NULL,
    cambiado_por_id int NOT NULL REFERENCES usuarios(usuario_id),
    comentario text,
    fecha_cambio timestamp DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE evidencias_digitales (
    evidencia_id serial PRIMARY KEY,
    queja_id int NOT NULL REFERENCES quejas(queja_id) ON DELETE CASCADE,
    informe_id int REFERENCES informes_queja(informe_id) ON DELETE SET NULL,
    etapa varchar(40) NOT NULL CHECK (
        etapa IN (
            'REPORTE_CIUDADANO',
            'INSPECCION_INICIAL',
            'SOLUCION_TECNICA',
            'CONFORMIDAD_CALIDAD',
            'AGRAVAMIENTO',
            'REINCIDENCIA'
        )
    ),
    url_archivo varchar(300) NOT NULL,
    nombre_archivo varchar(200),
    formato varchar(10),
    tamanio_bytes bigint,
    subido_por_usuario_id int REFERENCES usuarios(usuario_id),
    fecha_subida timestamp DEFAULT CURRENT_TIMESTAMP
);
