import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css'
})
export class NavbarComponent {
  rolActual: string = 'CIUDADANO';

  constructor(private router: Router) {}

  cambiarRol(nuevoRol: string) {
    this.rolActual = nuevoRol;

    switch (nuevoRol) {
      case 'CIUDADANO':
        this.router.navigate(['/portal-ciudadano']);
        break;
      case 'FUNCIONARIO':
        this.router.navigate(['/gestion-municipal']);
        break;
      case 'INSPECTOR':
      case 'ESPECIALISTA':
        this.router.navigate(['/operaciones-campo']);
        break;
    }
  }
}
