import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { DashboardService } from '../../services/dashboard.service';
import { DashboardResponse, DashboardSerie } from '../../models/dashboard.model';
import { colorEstado, colorGrupo } from '../../../../core/models/estado-queja.util';
import { GraficoDistribucionComponent } from '../../components/grafico-distribucion/grafico-distribucion';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [DatePipe, GraficoDistribucionComponent],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css',
})
export class AdminDashboardComponent implements OnInit, OnDestroy {
  private readonly dashboardService = inject(DashboardService);
  private intervalo?: ReturnType<typeof setInterval>;

  readonly SEGUNDOS_ACTUALIZACION = 15;
  readonly dashboard = signal<DashboardResponse | null>(null);
  readonly cargando = signal(false);
  readonly error = signal('');
  readonly autoActualizar = signal(true);
  readonly exportando = signal<'pdf' | 'excel' | null>(null);

  readonly series = computed<DashboardSerie[]>(() =>
    (this.dashboard()?.estados ?? []).map((estado) => ({
      etiqueta: estado.estado,
      valor: estado.total,
      porcentaje: estado.porcentaje,
    })),
  );

  readonly colores = computed<string[]>(() =>
    (this.dashboard()?.estados ?? []).map((estado) => colorEstado(estado.estado)),
  );

  ngOnInit(): void {
    this.cargar();
    this.reiniciarIntervalo();
  }

  ngOnDestroy(): void {
    this.detenerIntervalo();
  }

  cargar(): void {
    if (this.cargando()) {
      return;
    }
    this.cargando.set(true);
    this.error.set('');

    this.dashboardService.obtenerDashboard().subscribe({
      next: (dashboard) => {
        this.dashboard.set(dashboard);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(err?.error?.error || 'No se pudo cargar el dashboard de quejas.');
        this.cargando.set(false);
      },
    });
  }

  cambiarAutoActualizacion(activo: boolean): void {
    this.autoActualizar.set(activo);
    if (activo) {
      this.reiniciarIntervalo();
      this.cargar();
    } else {
      this.detenerIntervalo();
    }
  }

  exportar(formato: 'pdf' | 'excel'): void {
    if (this.exportando()) {
      return;
    }
    this.exportando.set(formato);
    this.error.set('');

    const peticion =
      formato === 'pdf'
        ? this.dashboardService.exportarPdf()
        : this.dashboardService.exportarExcel();

    peticion.subscribe({
      next: (blob) => {
        const fecha = new Date().toISOString().slice(0, 10);
        this.dashboardService.descargar(
          blob,
          `Dashboard_Quejas_${fecha}.${formato === 'pdf' ? 'pdf' : 'xlsx'}`,
        );
        this.exportando.set(null);
      },
      error: () => {
        this.error.set('No se pudo generar la exportación del dashboard.');
        this.exportando.set(null);
      },
    });
  }

  colorEstado = colorEstado;
  colorGrupo = colorGrupo;

  private reiniciarIntervalo(): void {
    this.detenerIntervalo();
    if (!this.autoActualizar()) {
      return;
    }
    this.intervalo = setInterval(() => this.cargar(), this.SEGUNDOS_ACTUALIZACION * 1000);
  }

  private detenerIntervalo(): void {
    if (this.intervalo) {
      clearInterval(this.intervalo);
      this.intervalo = undefined;
    }
  }
}
