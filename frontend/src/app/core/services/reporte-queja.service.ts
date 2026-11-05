import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ReporteFiltro,
  ReporteOpciones,
  ReporteResponse,
} from '../models/reporte-queja.model';

@Injectable({
  providedIn: 'root',
})
export class ReporteQuejaService {
  private apiUrl = 'http://localhost:8080/admin/reportes';

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token') || localStorage.getItem('jwt');
    return new HttpHeaders({
      Authorization: `Bearer ${token}`,
    });
  }

  consultar(filtros: ReporteFiltro): Observable<ReporteResponse> {
    return this.http.post<ReporteResponse>(
      `${this.apiUrl}/quejas/consultar`,
      filtros,
      { headers: this.getAuthHeaders() },
    );
  }

  obtenerOpciones(): Observable<ReporteOpciones> {
    return this.http.get<ReporteOpciones>(`${this.apiUrl}/opciones`, {
      headers: this.getAuthHeaders(),
    });
  }

  exportarExcel(filtros: ReporteFiltro): Observable<Blob> {
    return this.http.post(`${this.apiUrl}/quejas/exportar/excel`, filtros, {
      headers: this.getAuthHeaders(),
      responseType: 'blob',
    });
  }

  exportarPdf(filtros: ReporteFiltro): Observable<Blob> {
    return this.http.post(`${this.apiUrl}/quejas/exportar/pdf`, filtros, {
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
