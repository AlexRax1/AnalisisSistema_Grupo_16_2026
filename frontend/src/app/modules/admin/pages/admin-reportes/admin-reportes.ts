import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ReporteQuejaService } from '../../../../core/services/reporte-queja.service';
import {
  ReporteFiltro,
  ReporteOpciones,
  ReporteResponse,
} from '../../../../core/models/reporte-queja.model';
import { GraficoBarrasComponent } from '../../components/grafico-barras/grafico-barras';
import { GraficoPastelComponent } from '../../components/grafico-pastel/grafico-pastel';

@Component({
  selector: 'app-admin-reportes',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    ReactiveFormsModule,
    GraficoBarrasComponent,
    GraficoPastelComponent,
  ],
  templateUrl: './admin-reportes.html',
  styleUrl: './admin-reportes.css',
})
export class AdminReportesComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly reporteService = inject(ReporteQuejaService);

  readonly filtrosForm = this.fb.group({
    fechaDesde: [''],
    fechaHasta: [''],
    categoriaId: [null as number | null],
    estado: ['TODAS'],
    prioridad: ['TODAS'],
    agrupacion: ['CATEGORIA'],
  });

  readonly reporte = signal<ReporteResponse | null>(null);
  readonly opciones = signal<ReporteOpciones | null>(null);
  readonly cargando = signal(false);
  readonly error = signal('');

  ngOnInit(): void {
    this.cargarOpciones();
    this.consultar();
  }

  cargarOpciones(): void {
    this.reporteService.obtenerOpciones().subscribe({
      next: (opciones) => this.opciones.set(opciones),
      error: () => this.error.set('No se pudieron cargar los catálogos de filtros.'),
    });
  }

  consultar(): void {
    if (this.cargando()) {
      return;
    }
    this.cargando.set(true);
    this.error.set('');

    this.reporteService.consultar(this.obtenerFiltros()).subscribe({
      next: (reporte) => {
        this.reporte.set(reporte);
        this.cargando.set(false);
      },
      error: (err) => {
        this.reporte.set(null);
        this.error.set(err?.error?.error || 'No se pudo consultar el reporte.');
        this.cargando.set(false);
      },
    });
  }

  limpiar(): void {
    this.filtrosForm.reset({
      fechaDesde: '',
      fechaHasta: '',
      categoriaId: null,
      estado: 'TODAS',
      prioridad: 'TODAS',
      agrupacion: 'CATEGORIA',
    });
    this.consultar();
  }

  exportar(formato: 'excel' | 'pdf'): void {
    if (!this.reporte()) {
      return;
    }
    this.error.set('');
    const peticion = formato === 'excel'
      ? this.reporteService.exportarExcel(this.obtenerFiltros())
      : this.reporteService.exportarPdf(this.obtenerFiltros());

    peticion.subscribe({
      next: (blob) => {
        this.reporteService.descargar(
          blob,
          `Reporte_Quejas_${new Date().getTime()}.${formato === 'excel' ? 'xlsx' : 'pdf'}`,
        );
      },
      error: () => this.error.set(`No se pudo exportar el reporte en ${formato.toUpperCase()}.`),
    });
  }

  etiquetaAgrupacion(valor: string): string {
    const etiquetas: Record<string, string> = {
      CATEGORIA: 'Categoría',
      ESTADO: 'Estado',
      SUBCATEGORIA: 'Subcategoría',
      PRIORIDAD: 'Prioridad',
      TIPO_REGISTRO: 'Tipo de registro',
      DEPENDENCIA: 'Dependencia',
      ESPECIALISTA: 'Especialista',
      INSPECTOR: 'Inspector',
    };
    return etiquetas[valor] ?? valor;
  }

  private obtenerFiltros(): ReporteFiltro {
    const valores = this.filtrosForm.getRawValue();
    return {
      fechaDesde: valores.fechaDesde || null,
      fechaHasta: valores.fechaHasta || null,
      categoriaId: valores.categoriaId ?? null,
      estado: valores.estado || 'TODAS',
      prioridad: valores.prioridad || 'TODAS',
      agrupacion: valores.agrupacion || 'CATEGORIA',
    };
  }
}
