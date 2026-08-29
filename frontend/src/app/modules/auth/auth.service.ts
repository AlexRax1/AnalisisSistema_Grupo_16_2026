import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// Exportación de interfaces para evitar errores TS2305
export interface Usuario {
  token?: string;
  rol?: 'CIUDADANO' | 'FUNCIONARIO' | 'INSPECTOR' | 'ESPECIALISTA' | 'ADMINISTRADOR';
  correo?: string;
}

export interface RegistroCiudadanoReq {
  dpi: string;
  nombres: string;
  apellidos: string;
  correo: string;
  telefono: string;
  direccion: string;
  password: string;
  confirmPassword: string;
}

export interface ResetPasswordReq {
  userId: number;
  newPassword: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private BASE_URL = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  // CU02: Login
  login(correo: string, password: string): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.BASE_URL}/auth/login`, { username: correo, password });
  }

  // CU01: Registro de usuario ciudadano
  registrarCiudadano(datos: RegistroCiudadanoReq): Observable<any> {
    return this.http.post<any>(`${this.BASE_URL}/usuarios/registro-ciudadano`, datos);
  }

  // CU02 - FA02: Métodos de recuperación de contraseña
  solicitarCodigoRecuperacion(correo: string): Observable<any> {
    return this.http.post(`${this.BASE_URL}/auth/recuperar/solicitar-codigo`, { correo });
  }

  validarCodigo(correo: string, codigo: string): Observable<any> {
    return this.http.post(`${this.BASE_URL}/auth/recuperar/validar-codigo`, { correo, codigo });
  }

  // PUT: http://localhost:8080/auth/reset-password
  restablecerPassword(userId: number, newPassword: string): Observable<any> {
    const body: ResetPasswordReq = {
      userId: userId,
      newPassword: newPassword,
    };

    return this.http.put<any>(`${this.BASE_URL}/auth/reset-password`, body);
  }
}
