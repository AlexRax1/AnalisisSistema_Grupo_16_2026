import { Component, Input } from '@angular/core';
import { DashboardSerie } from '../../models/dashboard.model';

/**
 * Gráfico de distribución propio del dashboard.
 *
 * Es un dona hecho con `conic-gradient`: no necesita librerías externas y
 * responde al tamaño que le da el contenedor. Los colores se reciben desde la
 * página porque dependen del estado de la queja, no del gráfico.
 */
@Component({
  selector: 'app-grafico-distribucion',
  standalone: true,
  templateUrl: './grafico-distribucion.html',
  styleUrl: './grafico-distribucion.css',
})
export class GraficoDistribucionComponent {
  @Input() series: DashboardSerie[] = [];
  @Input() colores: string[] = [];

  get total(): number {
    return this.series.reduce((suma, item) => suma + item.valor, 0);
  }

  get gradiente(): string {
    const total = this.total;
    if (total <= 0) {
      return 'conic-gradient(var(--slate-200) 0% 100%)';
    }

    let inicio = 0;
    const partes = this.series.map((item, indice) => {
      const fin = inicio + (item.valor / total) * 100;
      const parte = `${this.color(indice)} ${inicio}% ${fin}%`;
      inicio = fin;
      return parte;
    });

    return `conic-gradient(${partes.join(', ')})`;
  }

  color(indice: number): string {
    return this.colores[indice] ?? '#64748b';
  }
}
