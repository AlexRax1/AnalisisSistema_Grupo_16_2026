import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PerfilUsuarioDTO, ActualizarPerfilDTO } from '../models/usuario.model'; // Ajusta la ruta a tu modelo si varía

@Injectable({
  providedIn: 'root',
})
export class UsuarioService {
  private apiUrl = 'http://localhost:8080/usuarios';

  constructor(private http: HttpClient) {}

  /**
   * Obtiene la información del perfil del ciudadano autenticado (RN06)
   */
  obtenerPerfil(): Observable<PerfilUsuarioDTO> {
    return this.http.get<PerfilUsuarioDTO>(`${this.apiUrl}/perfil`);
  }

  /**
   * Envía los datos actualizados del perfil y/o cambio de contraseña
   */
  actualizarPerfil(dto: ActualizarPerfilDTO): Observable<{ mensaje: string }> {
    return this.http.put<{ mensaje: string }>(`${this.apiUrl}/perfil`, dto);
  }
}
