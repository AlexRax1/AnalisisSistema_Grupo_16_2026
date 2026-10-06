export interface FiltroCatalogoAdminDTO {
  fechaDesde?: string;
  fechaHasta?: string;
  categoria?: string;
  estado?: string;
  prioridad?: string;
  busqueda?: string; // Correlativo o DPI del ciudadano
  pagina: number;
  tamanoPagina: number;
}

export interface ItemQuejaAdminDTO {
  quejaId: number;
  correlativo: string;
  fechaRegistro: string;
  dpiCiudadano: string;
  nombreCiudadano: string;
  categoria: string;
  direccionExacta: string;
  zona: string;
  prioridadDemanda: 'BAJA' | 'MEDIA' | 'ALTA' | 'URGENTE';
  inspectorAsignado?: string;
  estadoActual: string;
}

export interface RespuestaCatalogoPaginadoDTO {
  contenido: ItemQuejaAdminDTO[];
  totalElementos: number;
  totalPaginas: number;
  paginaActual: number;
}
