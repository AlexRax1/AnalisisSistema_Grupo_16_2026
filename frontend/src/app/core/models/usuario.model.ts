export interface PerfilUsuarioDTO {
  dpi: string;
  nombres: string;
  apellidos: string;
  telefono: string;
  direccion: string;
  correo: string;
}

export interface ActualizarPerfilDTO {
  telefono: string;
  direccion: string;
  correo: string;
  passwordActual?: string;
  nuevaPassword?: string;
  confirmarNuevaPassword?: string;
}
