import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { GestionMunicipalService } from '../../../../core/services/gestion-municipal.service';
import { QuejaResumenDTO, ESTADO_COLORS } from '../../../../core/models/queja.model';

@Component({
  selector: 'app-bandeja-gestion',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './bandeja-gestion.html',
  styleUrl: './bandeja-gestion.css',
})
export class BandejaGestionComponent implements OnInit {
  quejasAsignadas: QuejaResumenDTO[] = [];
  quejasFiltradas: QuejaResumenDTO[] = [];
  cargandoBandeja: boolean = false;
  filtroTexto: string = '';
  filtroPrioridad: string = 'TODAS';

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
    private gestionService: GestionMunicipalService,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cargarBandejaTareas();
  }

  cargarBandejaTareas(): void {
    this.cargandoBandeja = true;
    this.cdr.detectChanges();

    this.gestionService
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
          console.warn('Error al cargar mis quejas desde el backend. Cargando datos de muestra...', err);
          this.quejasAsignadas = this.getMockQuejasResumen();
          this.aplicarFiltros();
          this.mostrarToast('No se pudo conectar al servidor. Mostrando datos locales.', 'info');
          this.cdr.detectChanges();
        },
      });
  }

  seleccionarQueja(quejaId: number): void {
    this.router.navigate(['/funcionario/detalle-gestion', quejaId]);
  }

  aplicarFiltros(): void {
    const texto = (this.filtroTexto || '').toLowerCase().trim();
    const prioridad = this.filtroPrioridad.toUpperCase();

    this.quejasFiltradas = this.quejasAsignadas.filter((q) => {
      const coincideTexto =
        !texto ||
        q.correlativo.toLowerCase().includes(texto) ||
        (q.ciudadanoNombre && q.ciudadanoNombre.toLowerCase().includes(texto)) ||
        (q.categoria && q.categoria.toLowerCase().includes(texto)) ||
        (q.subcategoria && q.subcategoria.toLowerCase().includes(texto)) ||
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

  private getMockQuejasResumen(): QuejaResumenDTO[] {
    return [
      {
        quejaId: 14,
        correlativo: 'QUE-2026-000014',
        tipoRegistro: 'PRINCIPAL',
        categoria: 'Alumbrado Público',
        subcategoria: 'Luminaria apagada / quemada',
        zona: 3,
        direccionExacta: '4ta Calle 8-12 Zona 3',
        estadoActual: 'EN VALIDACIÓN DE REPARACIÓN',
        prioridadConfirmada: 'URGENTE',
        fechaRegistro: '2026-10-07T14:22:10',
        ciudadanoNombre: 'Juan Pérez',
      },
      {
        quejaId: 15,
        correlativo: 'QUE-2026-000015',
        tipoRegistro: 'PRINCIPAL',
        categoria: 'Bacheo y Vialidad',
        subcategoria: 'Hoyo profundo en vía principal',
        zona: 1,
        direccionExacta: '3ra Avenida 5-20 Zona 1',
        estadoActual: 'REGISTRADA',
        prioridadConfirmada: 'ALTA',
        fechaRegistro: '2026-10-07T16:00:00',
        ciudadanoNombre: 'María López',
      },
      {
        quejaId: 16,
        correlativo: 'QUE-2026-000016',
        tipoRegistro: 'PRINCIPAL',
        categoria: 'Fontanería y Drenajes',
        subcategoria: 'Fuga de agua potable',
        zona: 2,
        direccionExacta: 'Calle Real hacia aldea Montúfar',
        estadoActual: 'PENDIENTE DE CIERRE',
        prioridadConfirmada: 'MEDIA',
        fechaRegistro: '2026-10-06T10:15:00',
        ciudadanoNombre: 'Carlos Hernández',
      },
    ];
  }
}
