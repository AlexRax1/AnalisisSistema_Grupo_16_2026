import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-admin-reportes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-reportes.html',
  styleUrl: './admin-reportes.css'
})
export class AdminReportesComponent {
  metricas = [
    { icon: 'description', label: 'Total Quejas', valor: '1,247', variacion: '+12%', positivo: true },
    { icon: 'check_circle', label: 'Resueltas', valor: '1,089', variacion: '+8%', positivo: true },
    { icon: 'pending', label: 'En Proceso', valor: '134', variacion: '-3%', positivo: true },
    { icon: 'timer', label: 'Tiempo Promedio', valor: '4.2 días', variacion: '-0.5d', positivo: true }
  ];

  resumenPorCategoria = [
    { categoria: 'Vialidad y Bacheo', total: 423, resueltas: 380, porcentaje: 89.8 },
    { categoria: 'Alumbrado Público', total: 312, resueltas: 289, porcentaje: 92.6 },
    { categoria: 'Drenajes y Alcantarillado', total: 278, resueltas: 230, porcentaje: 82.7 },
    { categoria: 'Manejo de Residuos', total: 156, resueltas: 120, porcentaje: 76.9 },
    { categoria: 'Ornato y Parques', total: 78, resueltas: 70, porcentaje: 89.7 }
  ];
}
