import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  GestionTareaDTO,
  AsignarInspectorReq,
  AutorizarReparacionReq,
  CierreAdministrativoReq,
  RechazarDevolverReq,
  UsuarioCatDTO,
} from '../models/queja.model';

@Injectable({
  providedIn: 'root',
})
export class GestionMunicipalService {
  private apiGestionUrl = 'http://localhost:8080/api/gestion-municipal';
  private apiUsuariosUrl = 'http://localhost:8080/api/usuarios';

  constructor(private http: HttpClient) {}

  private parseResponse(res: string): any {
    if (!res) return {};
    try {
      return JSON.parse(res);
    } catch {
      return { mensaje: res };
    }
  }

  // POST /api/gestion-municipal/siguiente-tarea
  obtenerSiguienteTarea(): Observable<GestionTareaDTO> {
    return this.http
      .post(`${this.apiGestionUrl}/siguiente-tarea`, {}, { responseType: 'text' })
      .pipe(map((res) => this.parseResponse(res) as GestionTareaDTO));
  }

  // POST /api/gestion-municipal/asignar-inspector
  asignarInspector(payload: AsignarInspectorReq): Observable<any> {
    return this.http
      .post(`${this.apiGestionUrl}/asignar-inspector`, payload, { responseType: 'text' })
      .pipe(map((res) => this.parseResponse(res)));
  }

  // POST /api/gestion-municipal/autorizar-reparacion
  autorizarReparacion(payload: AutorizarReparacionReq): Observable<any> {
    return this.http
      .post(`${this.apiGestionUrl}/autorizar-reparacion`, payload, { responseType: 'text' })
      .pipe(map((res) => this.parseResponse(res)));
  }

  // POST /api/gestion-municipal/cierre-administrativo
  cierreAdministrativo(payload: CierreAdministrativoReq): Observable<any> {
    return this.http
      .post(`${this.apiGestionUrl}/cierre-administrativo`, payload, { responseType: 'text' })
      .pipe(map((res) => this.parseResponse(res)));
  }

  // POST /api/gestion-municipal/rechazar-devolver
  rechazarDevolver(payload: RechazarDevolverReq): Observable<any> {
    return this.http
      .post(`${this.apiGestionUrl}/rechazar-devolver`, payload, { responseType: 'text' })
      .pipe(map((res) => this.parseResponse(res)));
  }

  // GET /api/usuarios/inspectores
  obtenerInspectores(): Observable<UsuarioCatDTO[]> {
    return this.http.get<UsuarioCatDTO[]>(`${this.apiUsuariosUrl}/inspectores`);
  }

  // GET /api/usuarios/especialistas
  obtenerEspecialistas(): Observable<UsuarioCatDTO[]> {
    return this.http.get<UsuarioCatDTO[]>(`${this.apiUsuariosUrl}/especialistas`);
  }
}
