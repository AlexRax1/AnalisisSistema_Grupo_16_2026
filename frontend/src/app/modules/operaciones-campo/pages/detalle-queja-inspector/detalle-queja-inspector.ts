import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { InspectorService } from '../../../../core/services/inspector.service';
import {
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
  selector: 'app-detalle-queja-inspector',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './detalle-queja-inspector.html',
  styleUrl: './detalle-queja-inspector.css',
})
export class DetalleQuejaInspectorComponent implements OnInit {
  quejaId: number = 0;
  quejaSeleccionada: QuejaDetalleCompletoDTO | null = null;
  cargandoDetalle: boolean = false;

  // ── Formulario de Informe de Inspección ──
  problemaVerificado: boolean = true;
  gravedad: 'LEVE' | 'MODERADA' | 'GRAVE' | 'CRITICA' = 'MODERADA';
  descripcionInforme: string = '';
  recursosSugeridos: string = '';

  // Fotografías de Evidencia Obligatorias
  fotos: FotoAdjunta[] = [];

  // Estados de Formulario y Validaciones
  errorFormulario: string = '';
  cargandoEnvio: boolean = false;

  // ── Modales de Feedback y Visores ──
  mostrarModalExito: boolean = false;
  mensajeExito: string = '';
  imagenPrevisualizar: string | null = null;

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
    private route: ActivatedRoute,
    private router: Router,
    private inspectorService: InspectorService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.quejaId = Number(idParam);
      this.cargarDetalle(this.quejaId);
    }
  }

  regresar(): void {
    this.router.navigate(['/inspector/inspecciones']);
  }

  cargarDetalle(quejaId: number): void {
    this.cargandoDetalle = true;
    this.resetearFormulario();
    this.cdr.detectChanges();

    this.inspectorService
      .obtenerDetalleQueja(quejaId)
      .pipe(
        finalize(() => {
          this.cargandoDetalle = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: (detalle) => {
          this.quejaSeleccionada = detalle;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.warn(`Error al obtener detalle de la queja ${quejaId}. Generando mock...`, err);
          this.quejaSeleccionada = this.getMockInspectorDetalle(quejaId);
          this.cdr.detectChanges();
        },
      });
  }

  eliminarFoto(index: number): void {
    this.fotos.splice(index, 1);
  }

  onFotosSeleccionadas(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const files = Array.from(input.files);
    const limiteFotos = 3;
    const espacioRestante = limiteFotos - this.fotos.length;

    if (espacioRestante <= 0) {
      this.mostrarToast('Ya ha alcanzado el límite máximo de 3 fotografías.', 'warning');
      return;
    }

    const archivosAProcesar = files.slice(0, espacioRestante);

    for (const file of archivosAProcesar) {
      if (file.size > 5 * 1024 * 1024) {
        this.mostrarToast(`La imagen ${file.name} supera los 5MB permitidos.`, 'warning');
        continue;
      }
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.fotos.push({
          file: file,
          previewUrl: e.target.result,
        });
        this.cdr.detectChanges();
      };
      reader.readAsDataURL(file);
    }

    input.value = '';
  }

  private validarFormulario(): boolean {
    this.errorFormulario = '';

    if (!this.descripcionInforme || this.descripcionInforme.trim().length < 20) {
      this.errorFormulario = 'La descripción del informe de inspección debe contener al menos 20 caracteres.';
      return false;
    }

    if (this.fotos.length === 0) {
      this.errorFormulario = 'Es OBLIGATORIO adjuntar al menos una (1) fotografía como prueba del peritaje en campo.';
      return false;
    }

    return true;
  }

  enviarInformeInspeccion(): void {
    if (!this.validarFormulario() || !this.quejaSeleccionada) return;

    const quejaId = this.quejaSeleccionada.quejaId;
    const correlativo = this.quejaSeleccionada.correlativo;

    const datos: RegistrarInformeInspeccionReq = {
      problemaVerificado: this.problemaVerificado,
      gravedad: this.problemaVerificado ? this.gravedad : 'LEVE',
      descripcion: this.descripcionInforme.trim(),
      recursosSugeridos: this.problemaVerificado ? (this.recursosSugeridos.trim() || undefined) : undefined,
    };

    const archivosFotos = this.fotos.map((f) => f.file);

    this.cargandoEnvio = true;
    this.cdr.detectChanges();

    this.inspectorService
      .registrarInformeInspeccion(quejaId, datos, archivosFotos)
      .pipe(
        finalize(() => {
          this.cargandoEnvio = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: (res) => {
          const msg = res?.mensaje || `Informe de inspección registrado exitosamente para la queja ${correlativo}.`;
          this.mensajeExito = msg;
          this.mostrarModalExito = true;
        },
        error: (err) => {
          const errorMsg =
            err?.error?.error || err?.error?.mensaje || 'Error al enviar informe de inspección al servidor.';
          this.mostrarToast(errorMsg, 'error');
        },
      });
  }

  private resetearFormulario(): void {
    this.problemaVerificado = true;
    this.gravedad = 'MODERADA';
    this.descripcionInforme = '';
    this.recursosSugeridos = '';
    this.fotos = [];
    this.errorFormulario = '';
  }

  cerrarModalExito(): void {
    this.mostrarModalExito = false;
    this.cdr.detectChanges();
    this.regresar();
  }

  abrirVisorImagen(url: string): void {
    this.imagenPrevisualizar = url;
    this.cdr.detectChanges();
  }

  cerrarVisorImagen(): void {
    this.imagenPrevisualizar = null;
    this.cdr.detectChanges();
  }

  getUrlEvidencia(ev: EvidenciaDetalleDTO | string): string {
    if (!ev) return '';
    const url = typeof ev === 'string' ? ev : ev.urlArchivo;
    if (!url) return '';
    if (url.startsWith('/')) {
      return `http://localhost:8080${url}`;
    }
    return url;
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

  getGoogleMapsUrl(lat?: number, lng?: number): string {
    if (!lat || !lng) return '';
    return `https://www.google.com/maps?q=${lat},${lng}`;
  }

  puedeRegistrarInforme(): boolean {
    if (!this.quejaSeleccionada?.accionesDisponibles) return true;
    return this.quejaSeleccionada.accionesDisponibles.includes('REGISTRAR_INFORME_INSPECCION');
  }

  private getMockInspectorDetalle(id: number): QuejaDetalleCompletoDTO {
    return {
      quejaId: id,
      correlativo: `QUE-2026-0000${id}`,
      tipoRegistro: 'PRINCIPAL',
      correlativoOrigen: null,
      ciudadanoId: 5,
      ciudadanoNombre: 'Juan Pérez',
      ciudadanoDpi: '2987123450101',
      ciudadanoTelefono: '55551234',
      ciudadanoCorreo: 'juan.perez@correo.com',
      categoriaId: 1,
      categoria: 'Alumbrado Público',
      subcategoriaId: 1,
      subcategoria: 'Luminaria apagada / quemada',
      zona: 3,
      direccionExacta: '4ta Calle 8-12 Zona 3',
      puntoReferencia: 'Frente a panadería San Antonio',
      latitud: 14.634915,
      longitud: -90.506882,
      descripcion:
        'La lámpara del poste principal lleva varios días chispeando por las noches y ahora está totalmente apagada.',
      estadoActual: 'EN INSPECCIÓN',
      prioridadSugerida: 'URGENTE',
      prioridadConfirmada: 'URGENTE',
      funcionarioId: 2,
      funcionarioNombre: 'Licda. María Gómez',
      inspectorId: 7,
      inspectorNombre: 'Carlos Inspecciones',
      especialistaId: null,
      especialistaNombre: null,
      dependenciaId: null,
      dependenciaNombre: null,
      fechaRegistro: '2026-10-07T14:22:10',
      fechaModificacion: '2026-10-07T14:30:00',
      fechaCierre: null,
      motivoRechazo: null,
      evidencias: [
        {
          evidenciaId: 21,
          urlArchivo: 'https://images.unsplash.com/photo-1517457373958-b7bdd4587205?w=500',
          nombreArchivo: 'poste_danado.jpg',
          formato: 'jpg',
          fechaSubida: '2026-10-07T14:22:10',
        },
      ],
      informes: [],
      historial: [
        {
          historialId: 45,
          estadoAnterior: null,
          estadoNuevo: 'REGISTRADA',
          cambiadoPorNombre: 'Juan Pérez',
          comentario: 'Queja registrada por el ciudadano.',
          fechaCambio: '2026-10-07T14:22:10',
        },
        {
          historialId: 46,
          estadoAnterior: 'REGISTRADA',
          estadoNuevo: 'EN INSPECCIÓN',
          cambiadoPorNombre: 'Licda. María Gómez',
          comentario: 'Asignación automática de inspector de campo.',
          fechaCambio: '2026-10-07T14:30:00',
        },
      ],
      accionesDisponibles: ['REGISTRAR_INFORME_INSPECCION'],
    };
  }
}
