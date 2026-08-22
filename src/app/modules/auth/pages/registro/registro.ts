import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService, Usuario } from '../../../../modules/auth/auth.service';

@Component({
  selector: 'app-registro',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './registro.html',
  styleUrl: './registro.css'
})
export class RegistroComponent {
  nuevoUsuario: Usuario = {
    dpi: '',
    nombreCompleto: '',
    correo: '',
    telefono: '',
    rol: 'CIUDADANO'
  };
  password: string = '';

  constructor(private authService: AuthService, private router: Router) {}

  onRegistro() {
    this.authService.registro(this.nuevoUsuario, this.password).subscribe({
      next: () => {
        alert('Registro exitoso. Bienvenido.');
        this.router.navigate(['/portal-ciudadano']);
      }
    });
  }
}
