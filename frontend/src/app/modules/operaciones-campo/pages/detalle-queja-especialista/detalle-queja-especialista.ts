import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { ActivatedRoute, Router } from '@angular/router';
import { EspecialistaService } from '../../../../core/services/especialista.service';
import {
  QuejaDetalleCompletoDTO,
  InformeDTO,
  EvidenciaDetalleDTO,
  RegistrarInformeReparacionReq,
  ESTADO_COLORS,
} from '../../../../core/models/queja.model';

interface FotoAdjunta {
  file: File;
  previewUrl: string;
}

@Component({
  selector: 'app-detalle-queja-especialista',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './detalle-queja-especialista.html',
  styleUrl: './detalle-queja-especialista.css',
})
export class DetalleQuejaEspecialistaComponent implements OnInit {
  // ── Detalle de la Orden ──
  quejaSeleccionada: QuejaDetalleCompletoDTO | null = null;
  cargandoDetalle: boolean = false;
  quejaId: number | null = null;

  // ── Diagnóstico del Inspector (INSPECCION_INICIAL) ──
  diagnosticoInspector: InformeDTO | null = null;

  // ── Formulario de Informe Técnico de Reparación ──
  descripcionReparacion: string = '';
  materialesUtilizados: string = '';
  horasTrabajadas: number | null = null;
  instruccionesCuadrilla: string = '';

  // Fotografías de Evidencia Obligatorias
  fotos: FotoAdjunta[] = [];

  // Estados de Validación y Envío
  errorFormulario: string = '';
  cargandoEnvio: boolean = false;

  // ── Modales y Visores ──
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
    private especialistaService: EspecialistaService,
    private cdr: ChangeDetectorRef,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const id = params.get('id');
      if (id) {
        this.quejaId = Number(id);
        this.cargarDetalleQueja(this.quejaId);
      }
    });
  }

  regresar(): void {
    this.router.navigate(['/especialista/ordenes']);
  }

  // ── Carga de Detalle Completo de la Orden ──
  cargarDetalleQueja(quejaId: number): void {
    this.cargandoDetalle = true;
    this.resetearFormulario();
    this.cdr.detectChanges();

    this.especialistaService
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
          this.extraerDiagnosticoInspector(detalle);
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.warn(`Error al cargar detalle de la orden ${quejaId}. Generando mock...`, err);
          const mock = this.getMockEspecialistaDetalle(quejaId);
          this.quejaSeleccionada = mock;
          this.extraerDiagnosticoInspector(mock);
          this.cdr.detectChanges();
        },
      });
  }

  // Extrae del array informes el informe con tipoInforme == "INSPECCION_INICIAL"
  private extraerDiagnosticoInspector(detalle: QuejaDetalleCompletoDTO): void {
    if (detalle && detalle.informes && detalle.informes.length > 0) {
      this.diagnosticoInspector =
        detalle.informes.find((i) => i.tipoInforme === 'INSPECCION_INICIAL') || null;
    } else {
      this.diagnosticoInspector = null;
    }
  }

  // ── Fotografías del Peritaje del Inspector ──
  get fotosInspector(): (EvidenciaDetalleDTO | string)[] {
    const diag = this.diagnosticoInspector as any;
    if (diag?.evidencias && diag.evidencias.length > 0) {
      return diag.evidencias;
    }
    if (diag?.fotos && diag.fotos.length > 0) {
      return diag.fotos;
    }
    return this.quejaSeleccionada?.evidencias || [];
  }

  // ── Gestión de Fotografías de Pruebas Obligatorias ──
  onFotosSeleccionadas(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    this.errorFormulario = '';
    const archivos = Array.from(input.files);

    if (this.fotos.length + archivos.length > 3) {
      this.errorFormulario = 'Máximo 3 fotografías de evidencia de reparación permitidas.';
      input.value = '';
      return;
    }

    for (const file of archivos) {
      const formatosValidos = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
      if (!formatosValidos.includes(file.type.toLowerCase())) {
        this.errorFormulario = 'Solo se admiten fotografías en formato JPG, JPEG, PNG o WEBP.';
        input.value = '';
        return;
      }

      const maxTamano = 5 * 1024 * 1024;
      if (file.size > maxTamano) {
        this.errorFormulario = 'El archivo supera el tamaño máximo de 5 MB.';
        input.value = '';
        return;
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

  eliminarFoto(index: number): void {
    this.fotos.splice(index, 1);
  }

  // ── Validación y Envío del Informe de Reparación ──
  validarFormulario(): boolean {
    this.errorFormulario = '';

    if (!this.descripcionReparacion || this.descripcionReparacion.trim().length < 20) {
      this.errorFormulario = 'La descripción de los trabajos ejecutados debe contener al menos 20 caracteres.';
      return false;
    }

    if (!this.materialesUtilizados || !this.materialesUtilizados.trim()) {
      this.errorFormulario = 'Debe detallar los materiales y repuestos utilizados en la obra.';
      return false;
    }

    if (this.horasTrabajadas === null || this.horasTrabajadas <= 0) {
      this.errorFormulario = 'Debe indicar un número válido de horas trabajadas (ej. 2.5).';
      return false;
    }

    // Obligatoriedad estricta de fotos de prueba solicitada por el usuario
    if (this.fotos.length === 0) {
      this.errorFormulario = 'Es OBLIGATORIO adjuntar al menos una (1) fotografía como prueba del trabajo completado.';
      return false;
    }

    return true;
  }

  enviarInformeReparacion(): void {
    if (!this.validarFormulario() || !this.quejaSeleccionada) return;

    const quejaId = this.quejaSeleccionada.quejaId;
    const correlativo = this.quejaSeleccionada.correlativo;

    const datos: RegistrarInformeReparacionReq = {
      descripcion: this.descripcionReparacion.trim(),
      materialesUtilizados: this.materialesUtilizados.trim(),
      horasTrabajadas: Number(this.horasTrabajadas),
      instruccionesCuadrilla: this.instruccionesCuadrilla?.trim() || 'N/A',
    };

    const archivosFotos = this.fotos.map((f) => f.file);

    this.cargandoEnvio = true;
    this.cdr.detectChanges();

    this.especialistaService
      .registrarInformeReparacion(quejaId, datos, archivosFotos)
      .pipe(
        finalize(() => {
          this.cargandoEnvio = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: (res) => {
          const msg = res?.mensaje || `Informe de reparación técnica registrado exitosamente para la queja ${correlativo}.`;
          this.mensajeExito = msg;
          this.mostrarModalExito = true;
        },
        error: (err) => {
          const errorMsg =
            err?.error?.error || err?.error?.mensaje || 'Error al enviar informe de reparación técnica.';
          this.mostrarToast(errorMsg, 'error');
        },
      });
  }

  private resetearFormulario(): void {
    this.descripcionReparacion = '';
    this.materialesUtilizados = '';
    this.horasTrabajadas = null;
    this.instruccionesCuadrilla = '';
    this.fotos = [];
    this.errorFormulario = '';
  }

  cerrarModalExito(): void {
    this.mostrarModalExito = false;
    this.cdr.detectChanges();
    this.regresar();
  }

  // ── Visor de Imagen ──
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

  getGoogleMapsUrl(lat?: number, lng?: number): string {
    if (!lat || !lng) return '';
    return `https://www.google.com/maps?q=${lat},${lng}`;
  }

  // ── Mocks Demostrativos ──
  private getMockEspecialistaDetalle(id: number): QuejaDetalleCompletoDTO {
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
      estadoActual: 'EN REPARACIÓN TÉCNICA',
      prioridadSugerida: 'URGENTE',
      prioridadConfirmada: 'URGENTE',
      funcionarioId: 2,
      funcionarioNombre: 'Licda. María Gómez',
      inspectorId: 7,
      inspectorNombre: 'Carlos Inspecciones',
      especialistaId: 12,
      especialistaNombre: 'Pedro Técnico',
      dependenciaId: 2,
      dependenciaNombre: 'Unidad de Alumbrado Público',
      fechaRegistro: '2026-10-07T14:22:10',
      fechaModificacion: '2026-10-07T15:20:00',
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
      informes: [
        {
          informeId: 3,
          tipoInforme: 'INSPECCION_INICIAL',
          autorNombre: 'Carlos Inspecciones',
          autorRol: 'INSPECTOR_CAMPO',
          problemaVerificado: true,
          gravedad: 'ALTA',
          recursosSugeridos: 'Luminaria LED 100W y camión grúa',
          descripcion:
            'Se comprobó en sitio que el foco reventó por una sobrecarga y dañó el sóquet principal. Se requiere cambio completo de luminaria y revisión de neutro.',
          fechaRegistro: '2026-10-07T15:05:00',
          evidencias: [
            {
              evidenciaId: 55,
              urlArchivo: 'https://images.unsplash.com/photo-1541888946425-d0fbb186c5f8?w=500',
              nombreArchivo: 'inspeccion_poste_danado.jpg',
              formato: 'jpg',
              fechaSubida: '2026-10-07T15:05:00',
            },
          ],
        },
      ],
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
          comentario: 'Asignación de inspector de campo.',
          fechaCambio: '2026-10-07T14:30:00',
        },
        {
          historialId: 47,
          estadoAnterior: 'EN INSPECCIÓN',
          estadoNuevo: 'EN VALIDACIÓN DE REPARACIÓN',
          cambiadoPorNombre: 'Carlos Inspecciones',
          comentario: 'Inspección completada. Problema verificado: true. Gravedad: ALTA.',
          fechaCambio: '2026-10-07T15:05:00',
        },
        {
          historialId: 48,
          estadoAnterior: 'EN VALIDACIÓN DE REPARACIÓN',
          estadoNuevo: 'EN REPARACIÓN TÉCNICA',
          cambiadoPorNombre: 'Licda. María Gómez',
          comentario: 'Reparación autorizada. Asignada a Unidad de Alumbrado Público.',
          fechaCambio: '2026-10-07T15:20:00',
        },
      ],
      accionesDisponibles: ['REGISTRAR_INFORME_REPARACION'],
    };
  }
}
