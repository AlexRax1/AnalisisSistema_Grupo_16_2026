import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { QuejaDetalleDTO, RespuestaRegistroQueja } from '../models/queja.model';


@Injectable({
  providedIn: 'root',
})
export class QuejaService {
  private apiUrl = 'http://localhost:8080/quejas';

  constructor(private http: HttpClient) {}

  // Método auxiliar para adjuntar el JWT token
  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      Authorization: `Bearer ${token}`,
    });
  }
  // Consulta el historial de quejas del usuario autenticado (RN12)
  obtenerMisQuejas(correlativo?: string, estado?: string): Observable<QuejaDetalleDTO[]> {
    let params = new HttpParams();
    if (correlativo && correlativo.trim() !== '') {
      params = params.set('correlativo', correlativo.trim());
    }
    if (estado && estado !== 'TODAS') {
      params = params.set('estado', estado);
    }

    return this.http.get<QuejaDetalleDTO[]>(`${this.apiUrl}/mis-quejas`, {
      headers: this.getAuthHeaders(),
      params,
    });
  }

  // Consulta el detalle completo por correlativo
  obtenerPorCorrelativo(correlativo: string): Observable<QuejaDetalleDTO> {
    return this.http.get<QuejaDetalleDTO>(`${this.apiUrl}/detalle/${correlativo}`, {
      headers: this.getAuthHeaders(),
    });
  }

  // Registro de queja multipart
  registrarQueja(datos: any, fotos: File[]): Observable<RespuestaRegistroQueja> {
    const formData = new FormData();
    formData.append('datos', new Blob([JSON.stringify(datos)], { type: 'application/json' }));

    fotos.forEach((foto) => {
      formData.append('fotos', foto, foto.name);
    });

    return this.http.post<RespuestaRegistroQueja>(`${this.apiUrl}/registrar`, formData, {
      headers: this.getAuthHeaders(),
    });
  }

  descargarConstanciaPdf(correlativo: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${correlativo}/constancia-pdf`, {
      headers: this.getAuthHeaders(),
      responseType: 'blob',
    });
  }

  registrarQuejaDerivada(
    datos: { correlativoPadre: string; tipoDerivacion: string; descripcion: string },
    fotos: File[],
  ): Observable<RespuestaRegistroQueja> {
    const formData = new FormData();
    formData.append('datos', new Blob([JSON.stringify(datos)], { type: 'application/json' }));

    fotos.forEach((foto) => {
      formData.append('fotos', foto, foto.name);
    });

    return this.http.post<RespuestaRegistroQueja>(`${this.apiUrl}/reportar-derivada`, formData, {
      headers: this.getAuthHeaders(),
    });
  }
}
