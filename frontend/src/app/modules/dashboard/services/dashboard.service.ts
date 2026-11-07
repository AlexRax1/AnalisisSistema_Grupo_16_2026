import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DashboardResponse } from '../models/dashboard.model';

@Injectable({
  providedIn: 'root',
})
export class DashboardService {
  private readonly apiUrl = 'http://localhost:8080/admin/dashboard';

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token') || localStorage.getItem('jwt');
    return new HttpHeaders({
      Authorization: `Bearer ${token}`,
    });
  }

  obtenerDashboard(limiteRecientes = 10): Observable<DashboardResponse> {
    return this.http.get<DashboardResponse>(this.apiUrl, {
      headers: this.getAuthHeaders(),
      params: { limiteRecientes: String(limiteRecientes) },
    });
  }

  exportarPdf(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/exportar/pdf`, {
      headers: this.getAuthHeaders(),
      responseType: 'blob',
    });
  }

  exportarExcel(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/exportar/excel`, {
      headers: this.getAuthHeaders(),
      responseType: 'blob',
    });
  }

  descargar(blob: Blob, nombre: string): void {
    const url = window.URL.createObjectURL(blob);
    const enlace = document.createElement('a');
    enlace.href = url;
    enlace.download = nombre;
    enlace.click();
    window.URL.revokeObjectURL(url);
  }
}
