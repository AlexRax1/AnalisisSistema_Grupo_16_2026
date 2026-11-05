export interface ReporteFiltro {
  fechaDesde?: string | null;
  fechaHasta?: string | null;
  estado?: string | null;
  categoriaId?: number | null;
  prioridad?: string | null;
  agrupacion?: string | null;
}

export interface ReporteOpcion {
  id: number;
  nombre: string;
  categoriaId?: number | null;
}

export interface ReporteOpciones {
  categorias: ReporteOpcion[];
  subcategorias: ReporteOpcion[];
  dependencias: ReporteOpcion[];
  especialistas: ReporteOpcion[];
  estados: string[];
  prioridades: string[];
  agrupaciones: string[];
}

export interface ReporteKpi {
  etiqueta: string;
  valor: string;
  icono: string;
  tono: string;
}

export interface ReporteSerie {
  etiqueta: string;
  valor: number;
  porcentaje: number;
}

export interface ReporteFila {
  etiqueta: string;
  total: number;
  cerradas: number;
  enProceso: number;
  rechazadas: number;
  porcentaje: number;
}

export interface ReporteTabla {
  titulo: string;
  columnas: string[];
  filas: ReporteFila[];
}

export interface ReporteDetalle {
  quejaId: number;
  correlativo: string;
  fechaRegistro: string | null;
  ciudadano: string;
  dpi: string | null;
  categoria: string;
  subcategoria: string;
  estadoActual: string;
  prioridad: string;
  inspector: string;
  especialista: string;
  dependencia: string;
  fechaCierre: string | null;
  direccionExacta: string;
}

export interface ReporteResponse {
  titulo: string;
  subtitulo: string;
  fechaEmision: string;
  filtrosAplicados: Record<string, string>;
  kpis: ReporteKpi[];
  series: ReporteSerie[];
  tabla: ReporteTabla;
  detalle: ReporteDetalle[];
  totalDetalle: number;
  limiteDetalle: number;
}
