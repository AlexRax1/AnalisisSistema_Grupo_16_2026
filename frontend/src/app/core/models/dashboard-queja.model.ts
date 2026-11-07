import { ReporteDetalle } from './reporte-queja.model';

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

export interface DashboardResponse {
  fechaActualizacion: string;
  total: number;
  contadores: DashboardContador[];
  estados: DashboardEstado[];
  recientes: ReporteDetalle[];
}
