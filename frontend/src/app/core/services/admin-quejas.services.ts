import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  FiltroCatalogoAdminDTO,
  RespuestaCatalogoPaginadoDTO,
} from '../models/catalogo-admin.model';

@Injectable({
  providedIn: 'root',
})
export class AdminQuejasService {
  private apiUrl = 'http://localhost:8080/admin/quejas';

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      Authorization: `Bearer ${token}`,
    });
  }

  consultarCatalogo(filtros: FiltroCatalogoAdminDTO): Observable<RespuestaCatalogoPaginadoDTO> {
    let params = new HttpParams()
      .set('pagina', filtros.pagina.toString())
      .set('tamanoPagina', filtros.tamanoPagina.toString());

    if (filtros.fechaDesde) params = params.set('fechaDesde', filtros.fechaDesde);
    if (filtros.fechaHasta) params = params.set('fechaHasta', filtros.fechaHasta);
    if (filtros.categoria && filtros.categoria !== 'TODAS')
      params = params.set('categoria', filtros.categoria);
    if (filtros.estado && filtros.estado !== 'TODAS') params = params.set('estado', filtros.estado);
    if (filtros.prioridad && filtros.prioridad !== 'TODAS')
      params = params.set('prioridad', filtros.prioridad);
    if (filtros.busqueda) params = params.set('busqueda', filtros.busqueda.trim());

    return this.http.get<RespuestaCatalogoPaginadoDTO>(`${this.apiUrl}/catalogo`, {
      headers: this.getAuthHeaders(),
      params,
    });
  }

  exportarExcel(filtros: FiltroCatalogoAdminDTO): Observable<Blob> {
    let params = this.construirParamsExportacion(filtros);
    return this.http.get(`${this.apiUrl}/exportar/excel`, {
      headers: this.getAuthHeaders(),
      params,
      responseType: 'blob',
    });
  }

  exportarPdf(filtros: FiltroCatalogoAdminDTO): Observable<Blob> {
    let params = this.construirParamsExportacion(filtros);
    return this.http.get(`${this.apiUrl}/exportar/pdf`, {
      headers: this.getAuthHeaders(),
      params,
      responseType: 'blob',
    });
  }

  private construirParamsExportacion(filtros: FiltroCatalogoAdminDTO): HttpParams {
    let params = new HttpParams();
    if (filtros.fechaDesde) params = params.set('fechaDesde', filtros.fechaDesde);
    if (filtros.fechaHasta) params = params.set('fechaHasta', filtros.fechaHasta);
    if (filtros.categoria && filtros.categoria !== 'TODAS')
      params = params.set('categoria', filtros.categoria);
    if (filtros.estado && filtros.estado !== 'TODAS') params = params.set('estado', filtros.estado);
    if (filtros.prioridad && filtros.prioridad !== 'TODAS')
      params = params.set('prioridad', filtros.prioridad);
    if (filtros.busqueda) params = params.set('busqueda', filtros.busqueda.trim());
    return params;
  }
}
