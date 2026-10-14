import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-inicio-especialista',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './inicio-especialista.html',
  styleUrl: './inicio-especialista.css',
})
export class InicioEspecialistaComponent {
  constructor(private router: Router) {}

  irAOrdenes(): void {
    this.router.navigate(['/especialista/ordenes']);
  }
}
