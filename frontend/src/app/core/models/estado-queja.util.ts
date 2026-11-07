/**
 * Código de colores único para los estados de una queja.
 * Se usa en el dashboard, el catálogo de quejas y el detalle de reportes.
 */
export type GrupoEstado = 'NUEVOS' | 'EN_PROCESO' | 'PENDIENTES' | 'RESUELTOS' | 'RECHAZADOS';

export const GRUPO_ESTADO_COLOR: Record<GrupoEstado, string> = {
  NUEVOS: '#a16207',
  EN_PROCESO: '#c2410c',
  PENDIENTES: '#b91c1c',
  RESUELTOS: '#15803d',
  RECHAZADOS: '#7f1d1d',
};

export const ESTADO_A_GRUPO: Record<string, GrupoEstado> = {
  REGISTRADA: 'NUEVOS',
  'EN INSPECCIÓN': 'EN_PROCESO',
  EN_INSPECCION: 'EN_PROCESO',
  'EN REPARACIÓN TÉCNICA': 'EN_PROCESO',
  EN_REPARACION_TECNICA: 'EN_PROCESO',
  'EN VALIDACIÓN DE REPARACIÓN': 'EN_PROCESO',
  EN_VALIDACION_REPARACION: 'EN_PROCESO',
  'PENDIENTE DE CIERRE': 'PENDIENTES',
  PENDIENTE_CIERRE: 'PENDIENTES',
  'SOLUCIONADA / CERRADA': 'RESUELTOS',
  SOLUCIONADA: 'RESUELTOS',
  CERRADA: 'RESUELTOS',
  RECHAZADA: 'RECHAZADOS',
};

export function grupoEstado(estado: string): GrupoEstado {
  return ESTADO_A_GRUPO[estado] ?? 'PENDIENTES';
}

export function colorEstado(estado: string): string {
  return GRUPO_ESTADO_COLOR[grupoEstado(estado)];
}

export function colorGrupo(grupo: string): string {
  return GRUPO_ESTADO_COLOR[grupo as GrupoEstado] ?? '#64748b';
}

export function estiloEstado(estado: string): Record<string, string> {
  return {
    'background-color': colorEstado(estado),
    color: '#ffffff',
  };
}
