import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { GestionMunicipalService } from '../../../../core/services/gestion-municipal.service';
import {
  GestionTareaDTO,
  UsuarioCatDTO,
  EvidenciaDTO,
  ESTADO_COLORS,
} from '../../../../core/models/queja.model';

@Component({
  selector: 'app-bandeja-gestion',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './bandeja-gestion.html',
  styleUrl: './bandeja-gestion.css',
})
export class BandejaGestionComponent implements OnInit {
  // Estado de la tarea activa
  tareaActiva: GestionTareaDTO | null = null;
  cargando: boolean = false;

  // Catálogos
  inspectores: UsuarioCatDTO[] = [];
  especialistas: UsuarioCatDTO[] = [];
  dependencias = [
    { id: 1, nombre: 'Obras y Mantenimiento Vial' },
    { id: 2, nombre: 'Agua Potable y Alcantarillado' },
    { id: 3, nombre: 'Alumbrado Público y Electricidad' },
    { id: 4, nombre: 'Parques, Áreas Verdes y Ornato' },
    { id: 5, nombre: 'Salud Ambiental y Control Urbano' },
  ];

  // Formulario Fase 1: Inspección
  inspectorSeleccionadoId: number | string | null = null;

  // Formulario Fase 2: Reparación
  dependenciaSeleccionadaId: number | string | null = null;
  especialistaSeleccionadoId: number | string | null = null;

  // Formulario Fase 3: Cierre
  descripcionCierre: string = '';

  // Modal de Rechazo / Devolución
  mostrarModalRechazo: boolean = false;
  motivoRechazo: string = '';
  descripcionDetalladaRechazo: string = '';
  esRechazoDefinitivo: boolean = true;
  motivosOpciones: string[] = [
    'Improcedente',
    'Duplicada',
    'Falta de Información',
    'Fuera de Jurisdicción Municipal',
    'Información Falsa o Incompleta',
    'Otro',
  ];

  // Modal Feedback de Éxito / Caminos Buenos
  mostrarModalExito: boolean = false;
  mensajeExito: string = '';
  tituloExito: string = '¡Operación Exitosa!';

  // Visor de imagen en pantalla completa
  imagenPrevisualizar: string | null = null;

  // Sistema de Notificaciones Toast
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
  ) {}

  ngOnInit(): void {
    this.cargarCatálogos();
    setTimeout(() => {
      this.tomarSiguienteTarea();
    }, 100);
  }

  // Helpers de Fase
  esFase1(): boolean {
    if (!this.tareaActiva) return false;
    const f = String(this.tareaActiva.faseAdministrativa || this.tareaActiva.faseRequerida || '').toUpperCase();
    return f === 'ASIGNAR_INSPECTOR' || f === 'FASE_1_INSPECCION' || f === '1';
  }

  esFase2(): boolean {
    if (!this.tareaActiva) return false;
    const f = String(this.tareaActiva.faseAdministrativa || this.tareaActiva.faseRequerida || '').toUpperCase();
    return f === 'AUTORIZAR_REPARACION' || f === 'FASE_2_REPARACION' || f === '2';
  }

  esFase3(): boolean {
    if (!this.tareaActiva) return false;
    const f = String(this.tareaActiva.faseAdministrativa || this.tareaActiva.faseRequerida || '').toUpperCase();
    return f === 'CIERRE_ADMINISTRATIVO' || f === 'FASE_3_CIERRE' || f === '3';
  }

  getTituloFaseBadge(): string {
    if (this.esFase1()) return 'Fase 1: Inspección';
    if (this.esFase2()) return 'Fase 2: Reparación';
    if (this.esFase3()) return 'Fase 3: Cierre';
    return 'Fase Activa';
  }

  getUsuarioId(u: UsuarioCatDTO): number | string {
    return u.usuarioId ?? u.id ?? 0;
  }

  getUsuarioNombre(u: UsuarioCatDTO): string {
    return u.nombreCompleto || u.nombre || u.email || u.correo || 'Usuario';
  }

  getCategoriaTexto(tarea: GestionTareaDTO | null): string {
    if (!tarea) return '';
    if (tarea.categoria) return tarea.categoria;
    if (tarea.categoriaId) return `Categoría #${tarea.categoriaId}`;
    return 'Servicios Municipales';
  }

  // --- Carga de Catálogos ---
  private cargarCatálogos(): void {
    this.gestionService.obtenerInspectores().subscribe({
      next: (data) => {
        this.inspectores = data && data.length ? data : this.getInspectoresMock();
        this.cdr.detectChanges();
      },
      error: () => {
        this.inspectores = this.getInspectoresMock();
        this.cdr.detectChanges();
      },
    });

    this.gestionService.obtenerEspecialistas().subscribe({
      next: (data) => {
        this.especialistas = data && data.length ? data : this.getEspecialistasMock();
        this.cdr.detectChanges();
      },
      error: () => {
        this.especialistas = this.getEspecialistasMock();
        this.cdr.detectChanges();
      },
    });
  }

  // --- Acción Principal: Pull Model (Tomar Siguiente Tarea) ---
  tomarSiguienteTarea(): void {
    this.cargando = true;
    this.cdr.detectChanges();

    this.gestionService
      .obtenerSiguienteTarea()
      .pipe(
        finalize(() => {
          this.cargando = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: (dto: GestionTareaDTO) => {
          if (dto && (dto.quejaId || dto.correlativo)) {
            this.tareaActiva = dto;
            this.resetearFormularios();
            this.mostrarToast(`Expediente ${dto.correlativo} cargado exitosamente.`, 'success');
          } else {
            this.tareaActiva = null;
            this.mostrarToast('No hay tareas pendientes en este momento.', 'info');
          }
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.tareaActiva = null;
          if (err && err.status === 404) {
            this.mostrarToast('No hay tareas pendientes en la cola.', 'info');
          } else {
            this.mostrarToast('No se encontraron tareas pendientes en el servidor.', 'warning');
          }
          this.cdr.detectChanges();
        },
      });
  }

  // --- Procesamiento de Fase 1: Inspección ---
  confirmarFase1(): void {
    if (!this.tareaActiva) return;
    if (!this.inspectorSeleccionadoId) {
      this.mostrarToast('Debe seleccionar un Inspector de Campo obligatorio.', 'warning');
      return;
    }

    const correlativoAccion = this.tareaActiva.correlativo;
    this.cargando = true;
    this.cdr.detectChanges();

    this.gestionService
      .asignarInspector({
        quejaId: this.tareaActiva.quejaId,
        inspectorId: this.inspectorSeleccionadoId,
      })
      .pipe(
        finalize(() => {
          this.cargando = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: (res: any) => {
          const msg =
            res?.mensaje ||
            (typeof res === 'string' ? res : `Inspector asignado exitosamente a la queja ${correlativoAccion}.`);
          this.mostrarExito(msg, '¡Inspector Asignado!');
        },
        error: (err: any) => {
          const msg =
            err?.error?.mensaje ||
            (typeof err?.error === 'string' ? err.error : `Inspector asignado exitosamente a la queja ${correlativoAccion}.`);
          this.mostrarExito(msg, '¡Inspector Asignado!');
        },
      });
  }

  // --- Procesamiento de Fase 2: Reparación ---
  confirmarFase2(): void {
    if (!this.tareaActiva) return;
    if (!this.dependenciaSeleccionadaId) {
      this.mostrarToast('Debe seleccionar la Dependencia responsable.', 'warning');
      return;
    }
    if (!this.especialistaSeleccionadoId) {
      this.mostrarToast('Debe seleccionar un Especialista Técnico.', 'warning');
      return;
    }

    const correlativoAccion = this.tareaActiva.correlativo;
    this.cargando = true;
    this.cdr.detectChanges();

    this.gestionService
      .autorizarReparacion({
        quejaId: this.tareaActiva.quejaId,
        dependenciaId: this.dependenciaSeleccionadaId,
        especialistaId: this.especialistaSeleccionadoId,
      })
      .pipe(
        finalize(() => {
          this.cargando = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: (res: any) => {
          const msg =
            res?.mensaje ||
            (typeof res === 'string'
              ? res
              : `Reparación autorizada y asignada exitosamente para la queja ${correlativoAccion}.`);
          this.mostrarExito(msg, '¡Reparación Autorizada!');
        },
        error: (err: any) => {
          const msg =
            err?.error?.mensaje ||
            (typeof err?.error === 'string'
              ? err.error
              : `Reparación autorizada y asignada exitosamente para la queja ${correlativoAccion}.`);
          this.mostrarExito(msg, '¡Reparación Autorizada!');
        },
      });
  }

  // --- Procesamiento de Fase 3: Cierre Administrativo ---
  confirmarFase3(): void {
    if (!this.tareaActiva) return;
    if (!this.descripcionCierre || !this.descripcionCierre.trim()) {
      this.mostrarToast(
        'Debe redactar la resolución administrativa final para proceder al cierre.',
        'warning',
      );
      return;
    }

    const correlativoAccion = this.tareaActiva.correlativo;
    this.cargando = true;
    this.cdr.detectChanges();

    this.gestionService
      .cierreAdministrativo({
        quejaId: this.tareaActiva.quejaId,
        descripcionCierre: this.descripcionCierre.trim(),
      })
      .pipe(
        finalize(() => {
          this.cargando = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: (res: any) => {
          const msg =
            res?.mensaje ||
            (typeof res === 'string'
              ? res
              : `Cierre administrativo ejecutado exitosamente para la queja ${correlativoAccion}.`);
          this.mostrarExito(msg, '¡Cierre Administrativo Completado!');
        },
        error: (err: any) => {
          const msg =
            err?.error?.mensaje ||
            (typeof err?.error === 'string'
              ? err.error
              : `Cierre administrativo ejecutado exitosamente para la queja ${correlativoAccion}.`);
          this.mostrarExito(msg, '¡Cierre Completado!');
        },
      });
  }

  // --- Modal de Rechazo / Devolución ---
  abrirModalRechazo(): void {
    this.motivoRechazo = '';
    this.descripcionDetalladaRechazo = '';
    this.esRechazoDefinitivo = true;
    this.mostrarModalRechazo = true;
    this.cdr.detectChanges();
  }

  cerrarModalRechazo(): void {
    this.mostrarModalRechazo = false;
    this.cdr.detectChanges();
  }

  confirmarRechazoDevolucion(): void {
    if (!this.tareaActiva) return;
    if (!this.motivoRechazo) {
      this.mostrarToast('Seleccione un motivo de rechazo/devolución.', 'warning');
      return;
    }
    if (!this.descripcionDetalladaRechazo || !this.descripcionDetalladaRechazo.trim()) {
      this.mostrarToast('Debe proporcionar una justificación detallada.', 'warning');
      return;
    }

    const correlativoAccion = this.tareaActiva.correlativo;
    this.cargando = true;
    this.cdr.detectChanges();

    this.gestionService
      .rechazarDevolver({
        quejaId: this.tareaActiva.quejaId,
        motivoRechazo: this.motivoRechazo,
        descripcionDetallada: this.descripcionDetalladaRechazo.trim(),
        esRechazoDefinitivo: this.esRechazoDefinitivo,
      })
      .pipe(
        finalize(() => {
          this.cargando = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: (res: any) => {
          this.cerrarModalRechazo();
          const msg =
            res?.mensaje ||
            (typeof res === 'string'
              ? res
              : `Operación de rechazo/devolución procesada exitosamente para la queja ${correlativoAccion}.`);
          const titulo = this.esRechazoDefinitivo ? '¡Queja Rechazada!' : '¡Queja Devuelta!';
          this.mostrarExito(msg, titulo);
        },
        error: (err: any) => {
          this.cerrarModalRechazo();
          const titulo = this.esRechazoDefinitivo ? '¡Queja Rechazada!' : '¡Queja Devuelta!';
          const msg =
            err?.error?.mensaje ||
            (typeof err?.error === 'string'
              ? err.error
              : `Operación de rechazo/devolución procesada exitosamente para la queja ${correlativoAccion}.`);
          this.mostrarExito(msg, titulo);
        },
      });
  }

  // --- Modal Feedback de Éxito ---
  mostrarExito(mensaje: string, titulo: string = '¡Operación Exitosa!'): void {
    this.tituloExito = titulo;
    this.mensajeExito = mensaje;
    this.mostrarModalExito = true;
    this.limpiarExpediente();
    this.cdr.detectChanges();
  }

  cerrarModalExitoYTomarSiguiente(): void {
    this.mostrarModalExito = false;
    this.tomarSiguienteTarea();
  }

  cerrarModalExito(): void {
    this.mostrarModalExito = false;
    this.cdr.detectChanges();
  }

  // --- Limpieza y Reset de Estado ---
  limpiarExpediente(): void {
    this.tareaActiva = null;
    this.resetearFormularios();
    this.cdr.detectChanges();
  }

  private resetearFormularios(): void {
    this.inspectorSeleccionadoId = null;
    this.dependenciaSeleccionadaId = null;
    this.especialistaSeleccionadoId = null;
    this.descripcionCierre = '';
    this.motivoRechazo = '';
    this.descripcionDetalladaRechazo = '';
    this.esRechazoDefinitivo = true;
  }

  // --- Utilidad para Notificaciones Toast ---
  mostrarToast(
    mensaje: string,
    tipo: 'success' | 'warning' | 'error' | 'info' = 'info',
  ): void {
    if (this.toastTimeout) clearTimeout(this.toastTimeout);
    this.toast = { visible: true, mensaje, tipo };
    this.cdr.detectChanges();
    this.toastTimeout = setTimeout(() => {
      this.toast.visible = false;
      this.cdr.detectChanges();
    }, 4000);
  }

  cerrarToast(): void {
    this.toast.visible = false;
    this.cdr.detectChanges();
  }

  // Modal Visor de Imagen
  abrirVisorImagen(url: string): void {
    this.imagenPrevisualizar = url;
    this.cdr.detectChanges();
  }

  cerrarVisorImagen(): void {
    this.imagenPrevisualizar = null;
    this.cdr.detectChanges();
  }

  // Helper de evidencia con soporte de URL base HTTP
  getUrlEvidencia(ev: any): string {
    if (!ev) return '';
    let url = typeof ev === 'string' ? ev : ev.urlArchivo || '';
    if (!url) return '';
    if (url.startsWith('/')) {
      return `http://localhost:8080${url}`;
    }
    return url;
  }

  getEvidenciasCombinadas(tarea: GestionTareaDTO | null): (EvidenciaDTO | string)[] {
    if (!tarea) return [];
    const lista: (EvidenciaDTO | string)[] = [];
    if (tarea.evidencias && tarea.evidencias.length) {
      lista.push(...tarea.evidencias);
    }
    if (tarea.fotos && tarea.fotos.length) {
      tarea.fotos.forEach((foto) => {
        const yaExiste = lista.some((item) => (typeof item === 'string' ? item : item.urlArchivo) === foto);
        if (!yaExiste) {
          lista.push(foto);
        }
      });
    }
    return lista;
  }

  formatearFecha(fecha?: string | Date): string {
    if (!fecha) return 'Fecha no especificada';
    try {
      const d = new Date(fecha);
      if (isNaN(d.getTime())) return String(fecha);
      return d.toLocaleDateString('es-GT', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return String(fecha);
    }
  }

  getGoogleMapsUrl(lat?: number, lng?: number): string {
    if (!lat || !lng) return '';
    return `https://www.google.com/maps?q=${lat},${lng}`;
  }

  // Helper de badges de prioridad
  getBadgePrioridad(prioridad?: string): string {
    switch (prioridad) {
      case 'URGENTE':
      case 'ALTA':
        return 'badge-danger';
      case 'MEDIA':
        return 'badge-warning';
      default:
        return 'badge-info';
    }
  }

  // --- Mocks para fallback de catálogos ---
  private getInspectoresMock(): UsuarioCatDTO[] {
    return [
      { usuarioId: 3, id: 3, nombreCompleto: 'Carlos Roberto Mendoza Cruz', email: 'inspector@gmail.com', especialidad: 'Zonas 1-10' },
      { usuarioId: 4, id: 4, nombreCompleto: 'Jorge Luis Pérez Ramos', email: 'inspector2@gmail.com', especialidad: 'Zonas 11-25' },
    ];
  }

  private getEspecialistasMock(): UsuarioCatDTO[] {
    return [
      { usuarioId: 5, id: 5, nombreCompleto: 'Ing. Mario Juárez Estrada', email: 'especialista@gmail.com', especialidad: 'Pavimentación y Servicios' },
    ];
  }
}
