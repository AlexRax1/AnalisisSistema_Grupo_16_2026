import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { EmpleadoDTO, GuardarEmpleadoDTO } from '../models/personal-admin.model';

@Injectable({
  providedIn: 'root',
})
export class PersonalAdminService {
  private apiUrl = 'http://localhost:8080/admin/usuarios-internos';

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      Authorization: `Bearer ${token}`,
    });
  }

  obtenerEmpleados(): Observable<EmpleadoDTO[]> {
    return this.http.get<EmpleadoDTO[]>(this.apiUrl, {
      headers: this.getAuthHeaders(),
    });
  }

  registrarEmpleado(datos: GuardarEmpleadoDTO): Observable<{ mensaje: string }> {
    return this.http.post<{ mensaje: string }>(this.apiUrl, datos, {
      headers: this.getAuthHeaders(),
    });
  }

  actualizarEmpleado(
    id: number,
    datos: Partial<GuardarEmpleadoDTO>,
  ): Observable<{ mensaje: string }> {
    return this.http.put<{ mensaje: string }>(`${this.apiUrl}/${id}`, datos, {
      headers: this.getAuthHeaders(),
    });
  }

  cambiarEstado(id: number, activo: boolean, motivo?: string): Observable<{ mensaje: string }> {
    return this.http.patch<{ mensaje: string }>(
      `${this.apiUrl}/${id}/estado`,
      { activo, motivo },
      {
        headers: this.getAuthHeaders(),
      },
    );
  }
}
