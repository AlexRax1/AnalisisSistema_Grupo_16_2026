export type EstadoQueja =
  | 'REGISTRADA'
  | 'EN INSPECCIÓN'
  | 'EN_INSPECCION'
  | 'EN REPARACIÓN TÉCNICA'
  | 'EN_REPARACION_TECNICA'
  | 'EN VALIDACIÓN DE REPARACIÓN'
  | 'EN_VALIDACION_REPARACION'
  | 'PENDIENTE_CIERRE'
  | 'SOLUCIONADA / CERRADA'
  | 'SOLUCIONADA'
  | 'CERRADA'
  | 'RECHAZADA';

export interface HistorialEstado {
  estado: EstadoQueja;
  fecha: Date | string;
  comentario: string;
  responsable: string;
}

// Interfaz Queja con propiedades opcionales/obligatorias ajustadas para los Mocks
export interface Queja {
  id: string; // Tipo estricto string para solucionar TS2345
  correlativo: string;
  titulo?: string;
  descripcion: string;
  dpiCiudadano?: string;
  zona: number;
  direccion?: string;
  direccionExacta?: string;
  categoria: string;
  estado: EstadoQueja;
  estadoActual?: string;
  prioridad: 'ALTA' | 'MEDIA' | 'BAJA' | 'URGENTE';
  fechaCreacion?: Date | string;
  fechaRegistro?: Date | string;
  fotosAntes: string[]; // Obligatorio para evitar TS2532 en plantillas HTML
  fotosDespues?: string[];
  historialEstados: HistorialEstado[]; // Obligatorio para evitar TS18048 en queja-mock.service.ts
}

// DTOs para comunicación con Spring Boot (Portal Ciudadano)
export interface EvidenciaDTO {
  urlArchivo: string;
  nombreArchivo: string;
}

export interface QuejaDetalleDTO {
  quejaId: number;
  correlativo: string;
  categoria: string;
  subcategoria?: string;
  zona: number;
  direccionExacta: string;
  puntoReferencia?: string;
  latitud: number;
  longitud: number;
  descripcion: string;
  estadoActual: string;
  prioridadConfirmada: string;
  fechaRegistro: string | Date;
  evidencias?: EvidenciaDTO[];
}

export interface RespuestaRegistroQueja {
  correlativo: string;
  mensaje: string;
}

export const ESTADO_COLORS: Record<string, string> = {
  REGISTRADA: 'badge-primary',
  'EN INSPECCIÓN': 'badge-warning',
  EN_INSPECCION: 'badge-warning',
  'EN REPARACIÓN TÉCNICA': 'badge-info',
  EN_REPARACION_TECNICA: 'badge-info',
  'EN VALIDACIÓN DE REPARACIÓN': 'badge-warning',
  EN_VALIDACION_REPARACION: 'badge-warning',
  PENDIENTE_CIERRE: 'badge-neutral',
  'SOLUCIONADA / CERRADA': 'badge-success',
  SOLUCIONADA: 'badge-success',
  CERRADA: 'badge-success',
  RECHAZADA: 'badge-danger',
};

// --- DTOs según Especificación de API para Frontend: Módulo de Gestión de Quejas ---

export interface QuejaResumenDTO {
  quejaId: number;
  correlativo: string;
  tipoRegistro: string;
  categoria: string;
  subcategoria: string;
  zona: number;
  direccionExacta: string;
  estadoActual: string;
  prioridadConfirmada: string;
  fechaRegistro: string;
  ciudadanoNombre: string;
}

export interface EvidenciaDetalleDTO {
  evidenciaId?: number;
  urlArchivo: string;
  nombreArchivo?: string;
  formato?: string;
  fechaSubida?: string;
}

export interface InformeDTO {
  informeId?: number;
  tipoInforme: 'INSPECCION_INICIAL' | 'REPARACION_FINAL' | string;
  autorNombre: string;
  autorRol: 'INSPECTOR_CAMPO' | 'ESPECIALISTA_TECNICO' | string;
  problemaVerificado?: boolean | null;
  gravedad?: 'LEVE' | 'MODERADA' | 'GRAVE' | 'CRITICA' | string;
  recursosSugeridos?: string | null;
  instruccionesCuadrilla?: string | null;
  materialesUtilizados?: string | null;
  horasTrabajadas?: number | null;
  fechaFinTrabajo?: string | null;
  dictamenCalidad?: string | null;
  descripcion: string;
  fechaRegistro: string;
  evidencias?: (EvidenciaDetalleDTO | string)[];
  fotos?: string[];
}

export interface HistorialCambioDTO {
  historialId?: number;
  estadoAnterior: string | null;
  estadoNuevo: string;
  cambiadoPorNombre: string;
  comentario: string;
  fechaCambio: string;
}

export interface QuejaDetalleCompletoDTO {
  quejaId: number;
  correlativo: string;
  tipoRegistro: string;
  correlativoOrigen?: string | null;
  ciudadanoId?: number;
  ciudadanoNombre: string;
  ciudadanoDpi?: string;
  ciudadanoTelefono?: string;
  ciudadanoCorreo?: string;
  categoriaId?: number;
  categoria: string;
  subcategoriaId?: number;
  subcategoria: string;
  zona: number;
  direccionExacta: string;
  puntoReferencia?: string;
  latitud?: number;
  longitud?: number;
  descripcion: string;
  estadoActual: string;
  prioridadSugerida?: string;
  prioridadConfirmada: string;
  funcionarioId?: number | null;
  funcionarioNombre?: string | null;
  inspectorId?: number | null;
  inspectorNombre?: string | null;
  especialistaId?: number | null;
  especialistaNombre?: string | null;
  dependenciaId?: number | null;
  dependenciaNombre?: string | null;
  fechaRegistro: string;
  fechaModificacion?: string | null;
  fechaCierre?: string | null;
  motivoRechazo?: string | null;
  evidencias: EvidenciaDetalleDTO[];
  informes: InformeDTO[];
  historial: HistorialCambioDTO[];
  accionesDisponibles: string[];
}

export interface DependenciaMunicipalDTO {
  dependenciaId: number;
  nombreDependencia: string;
  codigoDependencia: string;
}

export interface AsignarInspectorActionReq {
  instrucciones?: string;
}

export interface AutorizarReparacionActionReq {
  dependenciaId: number;
  instrucciones?: string;
}

export interface CierreAdministrativoActionReq {
  descripcionCierre: string;
}

export interface RechazarDevolverActionReq {
  esRechazoDefinitivo: boolean;
  motivoRechazo: string;
  descripcionDetallada: string;
}

export interface RegistrarInformeInspeccionReq {
  problemaVerificado: boolean;
  gravedad: 'LEVE' | 'MODERADA' | 'GRAVE' | 'CRITICA';
  descripcion: string;
  recursosSugeridos?: string;
}

export interface RegistrarInformeReparacionReq {
  descripcion: string;
  materialesUtilizados: string;
  horasTrabajadas: number;
  instruccionesCuadrilla?: string;
}

// --- DTOs legacy para compatibilidad ---

export type FaseAdministrativa =
  | 'ASIGNAR_INSPECTOR'
  | 'AUTORIZAR_REPARACION'
  | 'CIERRE_ADMINISTRATIVO'
  | 'FASE_1_INSPECCION'
  | 'FASE_2_REPARACION'
  | 'FASE_3_CIERRE'
  | string;

export interface GestionTareaDTO {
  quejaId: number | string;
  correlativo: string;
  estadoActual?: string;
  faseAdministrativa?: FaseAdministrativa;
  faseRequerida?: string;
  prioridadConfirmada?: string;
  prioridad?: 'ALTA' | 'MEDIA' | 'BAJA' | 'URGENTE' | string;
  categoriaId?: number;
  subcategoriaId?: number;
  categoria?: string;
  subcategoria?: string;
  zona: number;
  direccionExacta: string;
  puntoReferencia?: string;
  descripcion: string;
  latitud?: number;
  longitud?: number;
  fechaRegistro?: string | Date;
  ciudadanoNombre?: string;
  dpiCiudadano?: string;
  telefonoCiudadano?: string;
  correoCiudadano?: string;
  mensaje?: string;
  fotos?: string[];
  evidencias?: (EvidenciaDTO | string)[];
}

export interface UsuarioCatDTO {
  usuarioId?: number;
  id?: number | string;
  nombreCompleto?: string;
  nombre?: string;
  dpi?: string;
  email?: string;
  correo?: string;
  especialidad?: string;
  zonaAsignada?: number;
}

export interface AsignarInspectorReq {
  quejaId: number | string;
  inspectorId: number | string;
}

export interface AutorizarReparacionReq {
  quejaId: number | string;
  dependenciaId: number | string;
  especialistaId: number | string;
}

export interface CierreAdministrativoReq {
  quejaId: number | string;
  descripcionCierre: string;
}

export interface RechazarDevolverReq {
  quejaId: number | string;
  motivoRechazo: string;
  descripcionDetallada: string;
  esRechazoDefinitivo: boolean;
}


