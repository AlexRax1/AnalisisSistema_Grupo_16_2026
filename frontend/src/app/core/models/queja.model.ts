export type EstadoQueja =
  | 'REGISTRADA'
  | 'EN_INSPECCION'
  | 'ASIGNADA_CUADRILLA'
  | 'EN_PROCESO'
  | 'REPARADA'
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
  prioridad: 'ALTA' | 'MEDIA' | 'BAJA';
  fechaCreacion: Date;
  fotosAntes: string[];
  fotosDespues?: string[];
  historialEstados: HistorialEstado[];
}
