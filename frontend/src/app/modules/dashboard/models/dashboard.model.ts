export interface DashboardContador {
  clave: string;
  etiqueta: string;
  valor: number;
  porcentaje: number;
  icono: string;
}

export interface DashboardEstado {
  estado: string;
  grupo: string;
  total: number;
  porcentaje: number;
}

export interface DashboardQueja {
  quejaId: number;
  correlativo: string;
  fechaRegistro: string | null;
  ciudadano: string;
  categoria: string;
  estadoActual: string;
  prioridad: string;
  dependencia: string;
}

export interface DashboardSerie {
  etiqueta: string;
  valor: number;
  porcentaje: number;
}

export interface DashboardResponse {
  fechaActualizacion: string;
  total: number;
  contadores: DashboardContador[];
  estados: DashboardEstado[];
  recientes: DashboardQueja[];
}
