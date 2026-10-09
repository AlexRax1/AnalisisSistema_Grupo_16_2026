import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  QuejaResumenDTO,
  QuejaDetalleCompletoDTO,
  DependenciaMunicipalDTO,
  AsignarInspectorActionReq,
  AutorizarReparacionActionReq,
  CierreAdministrativoActionReq,
  RechazarDevolverActionReq,
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
  private apiFuncionarioUrl = 'http://localhost:8080/api/funcionario';
  private apiGestionLegacyUrl = 'http://localhost:8080/api/gestion-municipal';
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

  // ── Endpoints Funcionario Municipal (/api/funcionario) ──

  // GET /api/funcionario/mis-quejas
  obtenerMisQuejas(): Observable<QuejaResumenDTO[]> {
    return this.http.get<QuejaResumenDTO[]>(`${this.apiFuncionarioUrl}/mis-quejas`);
  }

  // GET /api/funcionario/{quejaId}
  obtenerDetalleQueja(quejaId: number | string): Observable<QuejaDetalleCompletoDTO> {
    return this.http.get<QuejaDetalleCompletoDTO>(`${this.apiFuncionarioUrl}/${quejaId}`);
  }

  // GET /api/funcionario/dependencias
  obtenerDependencias(): Observable<DependenciaMunicipalDTO[]> {
    return this.http.get<DependenciaMunicipalDTO[]>(`${this.apiFuncionarioUrl}/dependencias`);
  }

  // POST /api/funcionario/{quejaId}/asignar-inspector
  asignarInspectorNuevo(quejaId: number | string, req: AsignarInspectorActionReq = {}): Observable<any> {
    return this.http.post<any>(`${this.apiFuncionarioUrl}/${quejaId}/asignar-inspector`, req);
  }

  // POST /api/funcionario/{quejaId}/autorizar-reparacion
  autorizarReparacionNueva(quejaId: number | string, req: AutorizarReparacionActionReq): Observable<any> {
    return this.http.post<any>(`${this.apiFuncionarioUrl}/${quejaId}/autorizar-reparacion`, req);
  }

  // POST /api/funcionario/{quejaId}/cierre
  cierreAdministrativoNuevo(quejaId: number | string, req: CierreAdministrativoActionReq): Observable<any> {
    return this.http.post<any>(`${this.apiFuncionarioUrl}/${quejaId}/cierre`, req);
  }

  // POST /api/funcionario/{quejaId}/rechazar-devolver
  rechazarDevolverNuevo(quejaId: number | string, req: RechazarDevolverActionReq): Observable<any> {
    return this.http.post<any>(`${this.apiFuncionarioUrl}/${quejaId}/rechazar-devolver`, req);
  }

  // ── Métodos Legacy (para mantener compatibilidad si algún componente los requiere) ──

  obtenerSiguienteTarea(): Observable<GestionTareaDTO> {
    return this.http
      .post(`${this.apiGestionLegacyUrl}/siguiente-tarea`, {}, { responseType: 'text' })
      .pipe(map((res) => this.parseResponse(res) as GestionTareaDTO));
  }

  asignarInspector(payload: AsignarInspectorReq): Observable<any> {
    return this.http
      .post(`${this.apiGestionLegacyUrl}/asignar-inspector`, payload, { responseType: 'text' })
      .pipe(map((res) => this.parseResponse(res)));
  }

  autorizarReparacion(payload: AutorizarReparacionReq): Observable<any> {
    return this.http
      .post(`${this.apiGestionLegacyUrl}/autorizar-reparacion`, payload, { responseType: 'text' })
      .pipe(map((res) => this.parseResponse(res)));
  }

  cierreAdministrativo(payload: CierreAdministrativoReq): Observable<any> {
    return this.http
      .post(`${this.apiGestionLegacyUrl}/cierre-administrativo`, payload, { responseType: 'text' })
      .pipe(map((res) => this.parseResponse(res)));
  }

  rechazarDevolver(payload: RechazarDevolverReq): Observable<any> {
    return this.http
      .post(`${this.apiGestionLegacyUrl}/rechazar-devolver`, payload, { responseType: 'text' })
      .pipe(map((res) => this.parseResponse(res)));
  }

  obtenerInspectores(): Observable<UsuarioCatDTO[]> {
    return this.http.get<UsuarioCatDTO[]>(`${this.apiUsuariosUrl}/inspectores`);
  }

  obtenerEspecialistas(): Observable<UsuarioCatDTO[]> {
    return this.http.get<UsuarioCatDTO[]>(`${this.apiUsuariosUrl}/especialistas`);
  }
}
