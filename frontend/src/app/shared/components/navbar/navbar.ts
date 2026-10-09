import { Component } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive],
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
        this.router.navigate(['/ciudadano/mis-quejas']);
        break;
      case 'FUNCIONARIO':
        this.router.navigate(['/funcionario/bandeja']);
        break;
      case 'INSPECTOR':
        this.router.navigate(['/inspector/inspecciones']);
        break;
      case 'ESPECIALISTA':
        this.router.navigate(['/especialista/ordenes']);
        break;
    }
  }
}
