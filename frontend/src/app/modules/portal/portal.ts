import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-portal',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './portal.html',
  styleUrl: './portal.css'
})
export class PortalComponent {
  pasos = [
    {
      icon: 'edit_note',
      titulo: 'Reporta el Problema',
      descripcion: 'Registra tu queja con fotos, ubicación GPS y una descripción detallada del problema en tu comunidad.'
    },
    {
      icon: 'engineering',
      titulo: 'Se Gestiona y Repara',
      descripcion: 'Un equipo de funcionarios, inspectores y cuadrillas técnicas trabajan en la solución del problema reportado.'
    },
    {
      icon: 'verified',
      titulo: 'Se Verifica y Resuelve',
      descripcion: 'Un inspector valida la reparación en sitio y se emite una constancia oficial de resolución para tu registro.'
    }
  ];
}
