import { Injectable } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';

export interface Usuario {
  dpi: string;
  nombreCompleto: string;
  correo: string;
  telefono: string;
  rol: 'CIUDADANO' | 'FUNCIONARIO' | 'INSPECTOR' | 'ESPECIALISTA';
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private usuariosMock: Usuario[] = [
    {
      dpi: '2540123450101',
      nombreCompleto: 'Juan Pérez',
      correo: 'ciudadano@muni.gt',
      telefono: '55551234',
      rol: 'CIUDADANO'
    }
  ];

  private usuarioLogueado: Usuario | null = null;

  login(correo: string, pass: string): Observable<Usuario> {
    const user = this.usuariosMock.find(u => u.correo === correo);
    if (user) {
      this.usuarioLogueado = user;
      return of(user);
    }
    return throwError(() => new Error('Credenciales inválidas'));
  }

  registro(nuevoUsuario: Usuario, pass: string): Observable<Usuario> {
    this.usuariosMock.push(nuevoUsuario);
    this.usuarioLogueado = nuevoUsuario;
    return of(nuevoUsuario);
  }

  getUsuarioActual(): Usuario | null {
    return this.usuarioLogueado;
  }
}
