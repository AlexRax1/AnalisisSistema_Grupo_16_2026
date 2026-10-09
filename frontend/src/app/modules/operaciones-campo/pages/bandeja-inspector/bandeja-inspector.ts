import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { InspectorService } from '../../../../core/services/inspector.service';
import {
  QuejaResumenDTO,
  QuejaDetalleCompletoDTO,
  EvidenciaDetalleDTO,
  RegistrarInformeInspeccionReq,
  ESTADO_COLORS,
} from '../../../../core/models/queja.model';

interface FotoAdjunta {
  file: File;
  previewUrl: string;
}

@Component({
  selector: 'app-bandeja-inspector',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './bandeja-inspector.html',
  styleUrl: './bandeja-inspector.css',
})
export class BandejaInspectorComponent implements OnInit {
  // ── Bandeja de Quejas Asignadas ──
  quejasAsignadas: QuejaResumenDTO[] = [];
  quejasFiltradas: QuejaResumenDTO[] = [];
  cargandoBandeja: boolean = false;
  filtroTexto: string = '';
  filtroPrioridad: string = 'TODAS';

  // ── Toast Notifications ──
  toast: {
    visible: boolean;
    mensaje: string;
    tipo: 'success' | 'warning' | 'error' | 'info';
  } = {
    visible: false,
    mensaje: '',
    tipo: 'info',
  };
  private toastTimeout: any = null;

  constructor(
    private inspectorService: InspectorService,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cargarBandejaTareas();
  }

  // ── Carga Automática de Quejas Asignadas al Inspector ──
  cargarBandejaTareas(mantenerIdSeleccionado?: number): void {
    this.cargandoBandeja = true;
    this.cdr.detectChanges();

    this.inspectorService
      .obtenerMisQuejas()
      .pipe(
        finalize(() => {
          this.cargandoBandeja = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: (tareas) => {
          this.quejasAsignadas = tareas || [];
          this.aplicarFiltros();


          this.cdr.detectChanges();
        },
        error: (err) => {
          console.warn('Error al cargar quejas del inspector. Usando datos demostrativos...', err);
          this.quejasAsignadas = this.getMockInspectorQuejas();
          this.aplicarFiltros();

          this.mostrarToast('Modo demostrativo: Mostrando quejas asignadas locales.', 'info');
          this.cdr.detectChanges();
        },
      });
  }

  // ── Navegación a Detalle ──
  verDetalle(quejaId: number): void {
    this.router.navigate(['/inspector/detalle-inspector', quejaId]);
  }

  // ── Filtros de Búsqueda ──
  aplicarFiltros(): void {
    const texto = (this.filtroTexto || '').toLowerCase().trim();
    const prioridad = this.filtroPrioridad.toUpperCase();

    this.quejasFiltradas = this.quejasAsignadas.filter((q) => {
      const coincideTexto =
        !texto ||
        q.correlativo.toLowerCase().includes(texto) ||
        (q.ciudadanoNombre && q.ciudadanoNombre.toLowerCase().includes(texto)) ||
        (q.categoria && q.categoria.toLowerCase().includes(texto)) ||
        (q.direccionExacta && q.direccionExacta.toLowerCase().includes(texto));

      const coincidePrioridad =
        prioridad === 'TODAS' || (q.prioridadConfirmada && q.prioridadConfirmada.toUpperCase() === prioridad);

      return coincideTexto && coincidePrioridad;
    });
  }

  onFiltroTextoChange(): void {
    this.aplicarFiltros();
  }

  cambiarFiltroPrioridad(prioridad: string): void {
    this.filtroPrioridad = prioridad;
    this.aplicarFiltros();
  }

  // ── Toast ──
  mostrarToast(mensaje: string, tipo: 'success' | 'warning' | 'error' | 'info' = 'info'): void {
    if (this.toastTimeout) clearTimeout(this.toastTimeout);
    this.toast = { visible: true, mensaje, tipo };
    this.cdr.detectChanges();
    this.toastTimeout = setTimeout(() => {
      this.toast.visible = false;
      this.cdr.detectChanges();
    }, 4500);
  }

  cerrarToast(): void {
    this.toast.visible = false;
    this.cdr.detectChanges();
  }

  // ── Formateo ──
  formatearFecha(fecha?: string | Date | null): string {
    if (!fecha) return 'Sin fecha';
    try {
      const d = new Date(fecha);
      if (isNaN(d.getTime())) return String(fecha);
      return d.toLocaleDateString('es-GT', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return String(fecha);
    }
  }

  getBadgePrioridad(prioridad?: string): string {
    const p = (prioridad || '').toUpperCase();
    switch (p) {
      case 'URGENTE':
      case 'CRITICA':
        return 'badge-danger';
      case 'ALTA':
      case 'GRAVE':
        return 'badge-warning';
      case 'MEDIA':
      case 'MODERADA':
        return 'badge-info';
      default:
        return 'badge-neutral';
    }
  }

  getBadgeEstado(estado?: string): string {
    if (!estado) return 'badge-neutral';
    return ESTADO_COLORS[estado] || 'badge-neutral';
  }


  // ── Mocks Demostrativos ──
  private getMockInspectorQuejas(): QuejaResumenDTO[] {
    return [
      {
        quejaId: 14,
        correlativo: 'QUE-2026-000014',
        tipoRegistro: 'PRINCIPAL',
        categoria: 'Alumbrado Público',
        subcategoria: 'Luminaria apagada / quemada',
        zona: 3,
        direccionExacta: '4ta Calle 8-12 Zona 3',
        estadoActual: 'EN INSPECCIÓN',
        prioridadConfirmada: 'URGENTE',
        fechaRegistro: '2026-10-07T14:22:10',
        ciudadanoNombre: 'Juan Pérez',
      },
      {
        quejaId: 17,
        correlativo: 'QUE-2026-000017',
        tipoRegistro: 'PRINCIPAL',
        categoria: 'Drenajes y Alcantarillado',
        subcategoria: 'Tragante colapsado',
        zona: 2,
        direccionExacta: 'Av. El Rosario frente a escuela',
        estadoActual: 'EN INSPECCIÓN',
        prioridadConfirmada: 'ALTA',
        fechaRegistro: '2026-10-07T11:00:00',
        ciudadanoNombre: 'Lucía Morales',
      },
    ];
  }


}
