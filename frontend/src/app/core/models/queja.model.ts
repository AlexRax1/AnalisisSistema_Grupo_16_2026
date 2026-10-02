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

// --- DTOs para Gestión Municipal (Pull Model) ---

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

