import { Component, Input } from '@angular/core';
import { ReporteSerie } from '../../../../core/models/reporte-queja.model';

@Component({
  selector: 'app-grafico-barras',
  standalone: true,
  templateUrl: './grafico-barras.html',
  styleUrl: './grafico-barras.css',
})
export class GraficoBarrasComponent {
  @Input() series: ReporteSerie[] = [];

  get maximo(): number {
    return Math.max(...this.series.map((item) => item.valor), 1);
  }

  ancho(valor: number): number {
    return (valor / this.maximo) * 100;
  }
}
