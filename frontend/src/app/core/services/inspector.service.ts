import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  QuejaResumenDTO,
  QuejaDetalleCompletoDTO,
  RegistrarInformeInspeccionReq,
} from '../models/queja.model';

@Injectable({
  providedIn: 'root',
})
export class InspectorService {
  private apiUrl = 'http://localhost:8080/api/inspector';

  constructor(private http: HttpClient) {}

  // GET /api/inspector/mis-quejas
  obtenerMisQuejas(): Observable<QuejaResumenDTO[]> {
    return this.http.get<QuejaResumenDTO[]>(`${this.apiUrl}/mis-quejas`);
  }

  // GET /api/inspector/{quejaId}
  obtenerDetalleQueja(quejaId: number | string): Observable<QuejaDetalleCompletoDTO> {
    return this.http.get<QuejaDetalleCompletoDTO>(`${this.apiUrl}/${quejaId}`);
  }

  // POST /api/inspector/{quejaId}/informe-inspeccion
  // Envía el informe técnico junto con las fotografías de prueba requeridas (Multipart / FormData o JSON)
  registrarInformeInspeccion(
    quejaId: number | string,
    datos: RegistrarInformeInspeccionReq,
    fotos: File[] = []
  ): Observable<any> {
    if (fotos && fotos.length > 0) {
      const formData = new FormData();
      formData.append(
        'datos',
        new Blob([JSON.stringify(datos)], { type: 'application/json' })
      );
      fotos.forEach((foto) => {
        formData.append('fotos', foto, foto.name);
      });
      return this.http.post<any>(`${this.apiUrl}/${quejaId}/informe-inspeccion`, formData);
    }

    return this.http.post<any>(`${this.apiUrl}/${quejaId}/informe-inspeccion`, datos);
  }
}
