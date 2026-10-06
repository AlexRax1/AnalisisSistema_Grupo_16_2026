export interface EmpleadoDTO {
  id?: number;
  dpi: string;
  nombres: string;
  apellidos: string;
  correoInstitucional: string;
  telefono: string;
  rol: 'Funcionario Municipal' | 'Inspector de Campo' | 'Especialista Técnico' | 'Administrador';
  dependencia: string;
  activo: boolean;
}

export interface GuardarEmpleadoDTO {
  dpi: string;
  nombres: string;
  apellidos: string;
  correoInstitucional: string;
  telefono: string;
  rol: string;
  dependencia: string;
}
