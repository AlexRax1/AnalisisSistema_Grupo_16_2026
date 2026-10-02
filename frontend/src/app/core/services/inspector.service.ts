import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, timeout, catchError, throwError } from 'rxjs';
import {
  QuejaInspectorBandejaDTO,
  RegistroInspeccionDTO,
  MensajeResponse
} from '../models/queja.model';

@Injectable({
  providedIn: 'root'
})
export class InspectorService {
  private apiUrl = 'http://localhost:8080/api/inspector';

  constructor(private http: HttpClient) {}

  /**
   * GET /api/inspector/asignaciones
   * Consultar la bandeja de asignaciones activas para el inspector autenticado.
   */
  obtenerAsignaciones(): Observable<QuejaInspectorBandejaDTO[]> {
    return this.http.get<QuejaInspectorBandejaDTO[]>(`${this.apiUrl}/asignaciones`).pipe(
      timeout(10000),
      catchError((err) => {
        if (err.name === 'TimeoutError') {
          return throwError(() => new Error('Tiempo de espera agotado al conectar con el servidor (http://localhost:8080).'));
        }
        return throwError(() => err);
      })
    );
  }

  /**
   * POST /api/inspector/quejas/{quejaId}/informe-inspeccion
   * Registrar el informe de inspección técnica in situ.
   */
  registrarInspeccion(quejaId: number, dto: RegistroInspeccionDTO): Observable<MensajeResponse> {
    return this.http.post<MensajeResponse>(`${this.apiUrl}/quejas/${quejaId}/informe-inspeccion`, dto).pipe(
      timeout(12000),
      catchError((err) => {
        if (err.name === 'TimeoutError') {
          return throwError(() => new Error('Tiempo de espera agotado al registrar el informe en el servidor.'));
        }
        return throwError(() => err);
      })
    );
  }
}
