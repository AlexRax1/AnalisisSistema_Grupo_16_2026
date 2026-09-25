import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { QuejaDetalleDTO, RespuestaRegistroQueja } from '../models/queja.model';


@Injectable({
  providedIn: 'root',
})
export class QuejaService {
  private apiUrl = 'http://localhost:8080/quejas'; // Ajusta la URL de tu backend

  constructor(private http: HttpClient) {}

  // Consulta el historial de quejas del usuario autenticado (RN12)
  obtenerMisQuejas(): Observable<QuejaDetalleDTO[]> {
    return this.http.get<QuejaDetalleDTO[]>(`${this.apiUrl}/mis-quejas`);
  }

  // Consulta el detalle completo por correlativo
  obtenerPorCorrelativo(correlativo: string): Observable<QuejaDetalleDTO> {
    return this.http.get<QuejaDetalleDTO>(`${this.apiUrl}/detalle/${correlativo}`);
  }

  // Registro de queja multipart
  registrarQueja(datos: any, fotos: File[]): Observable<RespuestaRegistroQueja> {
    const formData = new FormData();
    formData.append('datos', new Blob([JSON.stringify(datos)], { type: 'application/json' }));

    fotos.forEach((foto) => {
      formData.append('fotos', foto, foto.name);
    });

    return this.http.post<RespuestaRegistroQueja>(`${this.apiUrl}/registrar`, formData);
  }

  descargarConstanciaPdf(correlativo: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${correlativo}/constancia-pdf`, {
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

    return this.http.post<RespuestaRegistroQueja>(`${this.apiUrl}/reportar-derivada`, formData);
  }
}
