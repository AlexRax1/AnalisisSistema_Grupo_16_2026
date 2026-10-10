import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  QuejaResumenDTO,
  QuejaDetalleCompletoDTO,
  DependenciaMunicipalDTO,
  AsignarInspectorActionReq,
  AutorizarReparacionActionReq,
  CierreAdministrativoActionReq,
  RechazarDevolverActionReq,
} from '../models/queja.model';

@Injectable({
  providedIn: 'root',
})
export class GestionMunicipalService {
  private apiFuncionarioUrl = 'http://localhost:8080/api/funcionario';

  constructor(private http: HttpClient) {}

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
}
