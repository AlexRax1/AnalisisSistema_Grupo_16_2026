import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ReporteQuejaService } from '../../../../core/services/reporte-queja.service';
import { DashboardResponse } from '../../../../core/models/dashboard-queja.model';
import { ReporteSerie } from '../../../../core/models/reporte-queja.model';
import { colorEstado, colorGrupo } from '../../../../core/models/estado-queja.util';
import { GraficoPastelComponent } from '../../components/grafico-pastel/grafico-pastel';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [DatePipe, GraficoPastelComponent],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css',
})
export class AdminDashboardComponent implements OnInit, OnDestroy {
  private readonly reporteService = inject(ReporteQuejaService);
  private intervalo?: ReturnType<typeof setInterval>;

  readonly SEGUNDOS_ACTUALIZACION = 15;
  readonly dashboard = signal<DashboardResponse | null>(null);
  readonly cargando = signal(false);
  readonly error = signal('');
  readonly autoActualizar = signal(true);

  readonly series = computed<ReporteSerie[]>(() =>
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

    this.reporteService.obtenerDashboard().subscribe({
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
