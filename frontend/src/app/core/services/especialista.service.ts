import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  QuejaResumenDTO,
  QuejaDetalleCompletoDTO,
  RegistrarInformeReparacionReq,
} from '../models/queja.model';

@Injectable({
  providedIn: 'root',
})
export class EspecialistaService {
  private apiUrl = 'http://localhost:8080/api/especialista';

  constructor(private http: HttpClient) {}

  // GET /api/especialista/mis-quejas
  obtenerMisQuejas(): Observable<QuejaResumenDTO[]> {
    return this.http.get<QuejaResumenDTO[]>(`${this.apiUrl}/mis-quejas`);
  }

  // GET /api/especialista/{quejaId}
  obtenerDetalleQueja(quejaId: number | string): Observable<QuejaDetalleCompletoDTO> {
    return this.http.get<QuejaDetalleCompletoDTO>(`${this.apiUrl}/${quejaId}`);
  }

  // POST /api/especialista/{quejaId}/informe-reparacion
  // Envía el informe de reparación técnica junto con las fotografías de prueba obligatorias
  registrarInformeReparacion(
    quejaId: number | string,
    datos: RegistrarInformeReparacionReq,
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
      return this.http.post<any>(`${this.apiUrl}/${quejaId}/informe-reparacion`, formData);
    }

    return this.http.post<any>(`${this.apiUrl}/${quejaId}/informe-reparacion`, datos);
  }
}
