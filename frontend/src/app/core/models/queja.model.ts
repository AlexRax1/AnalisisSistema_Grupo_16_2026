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
