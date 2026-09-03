export type EstadoQueja =
  | 'REGISTRADA'
  | 'EN_INSPECCION'
  | 'EN_REPARACION_TECNICA'
  | 'EN_VALIDACION_REPARACION'
  | 'PENDIENTE_CIERRE'
  | 'SOLUCIONADA'
  | 'CERRADA'
  | 'RECHAZADA';

export interface HistorialEstado {
  estado: EstadoQueja;
  fecha: Date;
  comentario: string;
  responsable: string;
}

export interface Queja {
  id: string;
  correlativo: string;
  titulo: string;
  descripcion: string;
  dpiCiudadano: string;
  zona: number;
  direccion: string;
  categoria: string;
  estado: EstadoQueja;
  prioridad: 'ALTA' | 'MEDIA' | 'BAJA' | 'URGENTE';
  fechaCreacion: Date;
  fotosAntes: string[];
  fotosDespues?: string[];
  historialEstados: HistorialEstado[];
}

/** Labels legibles para mostrar en la UI */
export const ESTADO_LABELS: Record<EstadoQueja, string> = {
  'REGISTRADA': 'Registrada',
  'EN_INSPECCION': 'En Inspección',
  'EN_REPARACION_TECNICA': 'En Reparación Técnica',
  'EN_VALIDACION_REPARACION': 'En Validación',
  'PENDIENTE_CIERRE': 'Pendiente de Cierre',
  'SOLUCIONADA': 'Solucionada',
  'CERRADA': 'Cerrada',
  'RECHAZADA': 'Rechazada'
};

/** Colores semánticos para badges de estado */
export const ESTADO_COLORS: Record<EstadoQueja, string> = {
  'REGISTRADA': 'badge-primary',
  'EN_INSPECCION': 'badge-warning',
  'EN_REPARACION_TECNICA': 'badge-info',
  'EN_VALIDACION_REPARACION': 'badge-warning',
  'PENDIENTE_CIERRE': 'badge-neutral',
  'SOLUCIONADA': 'badge-success',
  'CERRADA': 'badge-success',
  'RECHAZADA': 'badge-danger'
};
