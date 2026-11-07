import { Component, Input } from '@angular/core';
import { ReporteSerie } from '../../../../core/models/reporte-queja.model';

@Component({
  selector: 'app-grafico-pastel',
  standalone: true,
  templateUrl: './grafico-pastel.html',
  styleUrl: './grafico-pastel.css',
})
export class GraficoPastelComponent {
  private readonly coloresBase = [
    '#2563eb',
    '#10b981',
    '#f59e0b',
    '#ef4444',
    '#8b5cf6',
    '#0891b2',
    '#64748b',
  ];

  @Input() series: ReporteSerie[] = [];

  /** Colores opcionales alineados con `series`; si no se envían, usa la paleta base. */
  @Input() colores: string[] = [];

  get datos(): ReporteSerie[] {
    if (this.series.length <= 7) {
      return this.series;
    }
    const principales = this.series.slice(0, 6);
    const otros = this.series.slice(6);
    return [
      ...principales,
      {
        etiqueta: 'Otros',
        valor: otros.reduce((total, item) => total + item.valor, 0),
        porcentaje: otros.reduce((total, item) => total + item.porcentaje, 0),
      },
    ];
  }

  get gradiente(): string {
    const total = this.datos.reduce((suma, item) => suma + item.valor, 0);
    if (total <= 0) {
      return 'conic-gradient(var(--slate-200) 0% 100%)';
    }
    let inicio = 0;
    const partes = this.datos.map((item, indice) => {
      const fin = inicio + (item.valor / total) * 100;
      const parte = `${this.color(indice)} ${inicio}% ${fin}%`;
      inicio = fin;
      return parte;
    });
    return `conic-gradient(${partes.join(', ')})`;
  }

  color(indice: number): string {
    return this.colores[indice] ?? this.coloresBase[indice % this.coloresBase.length];
  }
}
