import { Component, Input } from '@angular/core';
import { ReporteFila } from '../../../../core/models/reporte-queja.model';

/**
 * Tercer gráfico de la plantilla: barra apilada por dimensión.
 * Reutiliza las mismas filas de la tabla del reporte, así que sirve para
 * cualquier reporte que devuelva total/cerradas/enProceso/rechazadas.
 */
@Component({
  selector: 'app-grafico-apilado',
  standalone: true,
  templateUrl: './grafico-apilado.html',
  styleUrl: './grafico-apilado.css',
})
export class GraficoApiladoComponent {
  @Input() filas: ReporteFila[] = [];

  ancho(valor: number, fila: ReporteFila): number {
    if (!fila.total || fila.total <= 0) {
      return 0;
    }
    return (valor / fila.total) * 100;
  }
}
