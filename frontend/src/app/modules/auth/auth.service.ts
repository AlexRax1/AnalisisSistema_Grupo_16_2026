import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';

export type RolUsuario =
  'CIUDADANO' | 'FUNCIONARIO' | 'INSPECTOR' | 'ESPECIALISTA' | 'ADMINISTRADOR';

// Exportación de interfaces para evitar errores TS2305
export interface Usuario {
  token?: string;
  rol?: RolUsuario;
  correo?: string;
}

interface LoginResponse {
  token?: string;
  rol?: string;
  correo?: string;
}

// El backend devuelve roles con nombres largos (p. ej. INSPECTOR_CAMPO),
// mientras que la navegación del frontend usa nombres cortos.
export function normalizarRol(rol?: string): RolUsuario | undefined {
  if (!rol) return undefined;

  const rolLimpio = rol
    .trim()
    .toUpperCase()
    .replace(/^ROLE_/, '');

  switch (rolLimpio) {
    case 'CIUDADANO':
      return 'CIUDADANO';
    case 'FUNCIONARIO':
    case 'FUNCIONARIO_MUNICIPAL':
      return 'FUNCIONARIO';
    case 'INSPECTOR':
    case 'INSPECTOR_CAMPO':
      return 'INSPECTOR';
    case 'ESPECIALISTA':
    case 'ESPECIALISTA_TECNICO':
      return 'ESPECIALISTA';
    case 'ADMINISTRADOR':
      return 'ADMINISTRADOR';
    default:
      return undefined;
  }
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
    return this.http
      .post<LoginResponse>(`${this.BASE_URL}/auth/login`, { username: correo, password })
      .pipe(
        map((res) => ({
          token: res.token,
          rol: normalizarRol(res.rol),
          correo: res.correo,
        })),
      );
  }

  // Cierre de sesión auditado en el backend antes de descartar el token.
  logout(): Observable<any> {
    return this.http.post(`${this.BASE_URL}/auth/logout`, {});
  }

  // CU01: Registro de usuario ciudadano
  registrarCiudadano(datos: RegistroCiudadanoReq): Observable<string> {
    return this.http.post(`${this.BASE_URL}/usuarios/registro-ciudadano`, datos, {
      responseType: 'text',
    });
  }

  // CU02 - FA02: Métodos de recuperación de contraseña
  solicitarCodigoRecuperacion(correo: string): Observable<any> {
    return this.http.post(`${this.BASE_URL}/auth/recuperar/solicitar-codigo`, { correo });
  }

  validarCodigo(correo: string, codigo: string): Observable<any> {
    return this.http.post(`${this.BASE_URL}/auth/recuperar/validar-codigo`, { correo, codigo });
  }

  // PUT: http://localhost:8080/auth/reset-password
  restablecerPassword(correo: string, newPassword: string): Observable<string> {
    const body = {
      correo: correo,
      newPassword: newPassword,
    };

    return this.http.put(`${this.BASE_URL}/auth/recuperar/reset-password`, body, {
      responseType: 'text',
    });
  }
}
