import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { GestionMunicipalService } from '../../../../core/services/gestion-municipal.service';
import {
  QuejaDetalleCompletoDTO,
  DependenciaMunicipalDTO,
  EvidenciaDetalleDTO,
  InformeDTO,
  HistorialCambioDTO,
  ESTADO_COLORS,
} from '../../../../core/models/queja.model';

@Component({
  selector: 'app-detalle-queja-gestion',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './detalle-queja-gestion.html',
  styleUrl: './detalle-queja-gestion.css',
})
export class DetalleQuejaGestionComponent implements OnInit {
  quejaSeleccionada: QuejaDetalleCompletoDTO | null = null;
  cargandoDetalle: boolean = false;
  quejaIdSeleccionada: number | null = null;

  dependencias: DependenciaMunicipalDTO[] = [];

  // ── Controles para Comprimir / Minimizar Historial e Informes ──
  historialExpandido: boolean = false;
  informesExpandidos: boolean = false;

  toggleHistorial(): void {
    this.historialExpandido = !this.historialExpandido;
  }

  toggleInformes(): void {
    this.informesExpandidos = !this.informesExpandidos;
  }

  get historialVisible(): HistorialCambioDTO[] {
    if (!this.quejaSeleccionada?.historial) return [];
    if (this.historialExpandido || this.quejaSeleccionada.historial.length <= 1) {
      return this.quejaSeleccionada.historial;
    }
    // Retorna únicamente el evento más reciente (último en la lista)
    return [this.quejaSeleccionada.historial[this.quejaSeleccionada.historial.length - 1]];
  }

  get informesVisibles(): InformeDTO[] {
    if (!this.quejaSeleccionada?.informes) return [];
    if (this.informesExpandidos || this.quejaSeleccionada.informes.length <= 1) {
      return this.quejaSeleccionada.informes;
    }
    // Retorna únicamente el informe técnico más reciente (último en la lista)
    return [this.quejaSeleccionada.informes[this.quejaSeleccionada.informes.length - 1]];
  }

  instruccionesInspector: string = '';
  dependenciaSeleccionadaId: number | null = null;
  instruccionesReparacion: string = '';
  descripcionCierre: string = '';

  mostrarModalRechazo: boolean = false;
  motivoRechazo: string = '';
  descripcionDetalladaRechazo: string = '';
  esRechazoDefinitivo: boolean = true;
  motivosOpciones: string[] = [
    'Improcedente',
    'Duplicada',
    'Falta de Información Técnica',
    'Fuera de Jurisdicción Municipal',
    'Información Falsa o Incompleta',
    'Requiere Corrección de Peritaje',
    'Otro',
  ];

  mostrarModalExito: boolean = false;
  mensajeExito: string = '';
  tituloExito: string = '¡Operación Exitosa!';

  imagenPrevisualizar: string | null = null;

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
  cargandoAccion: boolean = false;

  constructor(
    private gestionService: GestionMunicipalService,
    private cdr: ChangeDetectorRef,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.cargarDependencias();
    this.route.paramMap.subscribe((params) => {
      const idParam = params.get('id');
      if (idParam) {
        this.cargarDetalleQueja(Number(idParam));
      } else {
        this.regresar();
      }
    });
  }

  regresar(): void {
    this.router.navigate(['/funcionario/bandeja']);
  }

  cargarDetalleQueja(quejaId: number): void {
    this.quejaIdSeleccionada = quejaId;
    this.cargandoDetalle = true;
    this.resetearFormulariosAccion();
    this.cdr.detectChanges();

    this.gestionService
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
          console.warn(`Error al cargar detalle de la queja ${quejaId}. Generando mock local...`, err);
          this.quejaSeleccionada = this.getMockQuejaDetalle(quejaId);
          this.cdr.detectChanges();
        },
      });
  }

  private cargarDependencias(): void {
    this.gestionService.obtenerDependencias().subscribe({
      next: (deps) => {
        if (deps && deps.length > 0) {
          this.dependencias = deps;
        } else {
          this.dependencias = this.getMockDependencias();
        }
        this.cdr.detectChanges();
      },
      error: () => {
        this.dependencias = this.getMockDependencias();
        this.cdr.detectChanges();
      },
    });
  }

  getPuntoEtapa(): number {
    if (!this.quejaSeleccionada) return 1;
    const est = (this.quejaSeleccionada.estadoActual || '').toUpperCase();

    if (est.includes('CIERRE') || est.includes('SOLUCIONADA') || est.includes('CERRADA')) {
      return 3;
    }
    if (est.includes('REPARACIÓN') || est.includes('REPARACION') || est.includes('VALIDACIÓN') || est.includes('VALIDACION')) {
      return 2;
    }
    return 1;
  }

  esEtapa1Completada(): boolean {
    return this.getPuntoEtapa() > 1;
  }

  esEtapa2Completada(): boolean {
    return this.getPuntoEtapa() > 2;
  }

  esEtapa3Completada(): boolean {
    const est = (this.quejaSeleccionada?.estadoActual || '').toUpperCase();
    return est.includes('SOLUCIONADA') || est.includes('CERRADA');
  }

  tieneAccion(accion: string): boolean {
    if (!this.quejaSeleccionada?.accionesDisponibles) return false;
    return this.quejaSeleccionada.accionesDisponibles.includes(accion);
  }

  confirmarAsignarInspector(): void {
    if (!this.quejaSeleccionada) return;
    const quejaId = this.quejaSeleccionada.quejaId;
    const correlativo = this.quejaSeleccionada.correlativo;

    this.cargandoAccion = true;
    this.cdr.detectChanges();

    this.gestionService
      .asignarInspectorNuevo(quejaId, { instrucciones: this.instruccionesInspector.trim() })
      .pipe(
        finalize(() => {
          this.cargandoAccion = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: (res) => {
          const msg = res?.mensaje || `Inspector asignado automáticamente a la queja ${correlativo}.`;
          this.mostrarExito(msg, '¡Inspección en Campo Solicitada!');
          this.cargarDetalleQueja(quejaId);
        },
        error: (err) => {
          const errorMsg = err?.error?.error || err?.error?.mensaje || 'Error al solicitar asignación de inspector.';
          this.mostrarToast(errorMsg, 'error');
        },
      });
  }

  confirmarAutorizarReparacion(): void {
    if (!this.quejaSeleccionada) return;
    if (!this.dependenciaSeleccionadaId) {
      this.mostrarToast('Debe seleccionar la dependencia municipal obligatoria.', 'warning');
      return;
    }

    const quejaId = this.quejaSeleccionada.quejaId;
    const correlativo = this.quejaSeleccionada.correlativo;

    this.cargandoAccion = true;
    this.cdr.detectChanges();

    this.gestionService
      .autorizarReparacionNueva(quejaId, {
        dependenciaId: Number(this.dependenciaSeleccionadaId),
        instrucciones: this.instruccionesReparacion.trim(),
      })
      .pipe(
        finalize(() => {
          this.cargandoAccion = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: (res) => {
          const msg = res?.mensaje || `Reparación autorizada y especialista asignado a la queja ${correlativo}.`;
          this.mostrarExito(msg, '¡Reparación Técnica Autorizada!');
          this.cargarDetalleQueja(quejaId);
        },
        error: (err) => {
          const errorMsg = err?.error?.error || err?.error?.mensaje || 'Error al autorizar reparación.';
          this.mostrarToast(errorMsg, 'error');
        },
      });
  }

  confirmarCierreAdministrativo(): void {
    if (!this.quejaSeleccionada) return;
    if (!this.descripcionCierre || this.descripcionCierre.trim().length < 20) {
      this.mostrarToast('La resolución de cierre debe tener al menos 20 caracteres.', 'warning');
      return;
    }

    const quejaId = this.quejaSeleccionada.quejaId;
    const correlativo = this.quejaSeleccionada.correlativo;

    this.cargandoAccion = true;
    this.cdr.detectChanges();

    this.gestionService
      .cierreAdministrativoNuevo(quejaId, { descripcionCierre: this.descripcionCierre.trim() })
      .pipe(
        finalize(() => {
          this.cargandoAccion = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: (res) => {
          const msg = res?.mensaje || `La queja ${correlativo} ha sido cerrada exitosamente.`;
          this.mostrarExito(msg, '¡Queja Cerrada Formalmente!');
          this.cargarDetalleQueja(quejaId);
        },
        error: (err) => {
          const errorMsg = err?.error?.error || err?.error?.mensaje || 'Error al completar cierre administrativo.';
          this.mostrarToast(errorMsg, 'error');
        },
      });
  }

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
    if (!this.quejaSeleccionada) return;
    if (!this.motivoRechazo) {
      this.mostrarToast('Seleccione un motivo principal.', 'warning');
      return;
    }
    if (!this.descripcionDetalladaRechazo || !this.descripcionDetalladaRechazo.trim()) {
      this.mostrarToast('Debe proporcionar una justificación detallada.', 'warning');
      return;
    }

    const quejaId = this.quejaSeleccionada.quejaId;
    const correlativo = this.quejaSeleccionada.correlativo;

    this.cargandoAccion = true;
    this.cdr.detectChanges();

    this.gestionService
      .rechazarDevolverNuevo(quejaId, {
        esRechazoDefinitivo: this.esRechazoDefinitivo,
        motivoRechazo: this.motivoRechazo,
        descripcionDetallada: this.descripcionDetalladaRechazo.trim(),
      })
      .pipe(
        finalize(() => {
          this.cargandoAccion = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: (res) => {
          this.cerrarModalRechazo();
          const titulo = this.esRechazoDefinitivo ? '¡Queja Rechazada!' : '¡Queja Devuelta a Etapa Anterior!';
          const msg =
            res?.mensaje || `La operación se ha aplicado exitosamente a la queja ${correlativo}.`;
          this.mostrarExito(msg, titulo);
          this.cargarDetalleQueja(quejaId);
        },
        error: (err) => {
          this.cerrarModalRechazo();
          const errorMsg = err?.error?.error || err?.error?.mensaje || 'Error al procesar rechazo o devolución.';
          this.mostrarToast(errorMsg, 'error');
        },
      });
  }

  private resetearFormulariosAccion(): void {
    this.instruccionesInspector = '';
    this.dependenciaSeleccionadaId = null;
    this.instruccionesReparacion = '';
    this.descripcionCierre = '';
    this.motivoRechazo = '';
    this.descripcionDetalladaRechazo = '';
    this.esRechazoDefinitivo = true;
  }

  mostrarExito(mensaje: string, titulo: string = '¡Operación Exitosa!'): void {
    this.tituloExito = titulo;
    this.mensajeExito = mensaje;
    this.mostrarModalExito = true;
    this.cdr.detectChanges();
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

  private getMockDependencias(): DependenciaMunicipalDTO[] {
    return [
      { dependenciaId: 1, nombreDependencia: 'Dirección de Servicios Públicos', codigoDependencia: 'DSP' },
      { dependenciaId: 2, nombreDependencia: 'Unidad de Alumbrado Público', codigoDependencia: 'ALUM' },
      { dependenciaId: 3, nombreDependencia: 'Fontanería y Drenajes', codigoDependencia: 'DREN' },
      { dependenciaId: 4, nombreDependencia: 'Bacheo y Pavimentación', codigoDependencia: 'VIAL' },
      { dependenciaId: 5, nombreDependencia: 'Limpieza y Recolección', codigoDependencia: 'RESID' },
      { dependenciaId: 6, nombreDependencia: 'Parques y Ornato', codigoDependencia: 'ORNAT' },
    ];
  }

  private getMockQuejaDetalle(id: number): QuejaDetalleCompletoDTO {
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
        'La lámpara del poste principal lleva varios días chispeando por las noches y ahora está totalmente apagada, provocando oscuridad e inseguridad en la cuadra.',
      estadoActual: 'EN VALIDACIÓN DE REPARACIÓN',
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
      fechaModificacion: '2026-10-07T15:05:00',
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
          recursosSugeridos: 'Luminaria LED 100W, escalera telescópica o grúa',
          descripcion:
            'Se comprobó en sitio que el foco reventó por una sobrecarga y dañó el sóquet principal del poste. Se requiere cambio total de luminaria.',
          fechaRegistro: '2026-10-07T15:05:00',
        },
      ],
      historial: [
        {
          historialId: 45,
          estadoAnterior: null,
          estadoNuevo: 'REGISTRADA',
          cambiadoPorNombre: 'Juan Pérez',
          comentario: 'Queja registrada en portal ciudadano. Asignada automáticamente a funcionario.',
          fechaCambio: '2026-10-07T14:22:10',
        },
        {
          historialId: 46,
          estadoAnterior: 'REGISTRADA',
          estadoNuevo: 'EN INSPECCIÓN',
          cambiadoPorNombre: 'Licda. María Gómez',
          comentario: 'Inspector Carlos Inspecciones asignado automáticamente por el sistema.',
          fechaCambio: '2026-10-07T14:30:00',
        },
        {
          historialId: 47,
          estadoAnterior: 'EN INSPECCIÓN',
          estadoNuevo: 'EN VALIDACIÓN DE REPARACIÓN',
          cambiadoPorNombre: 'Carlos Inspecciones',
          comentario: 'Inspección completada satisfactoriamente. Problema verificado: Sí. Gravedad: ALTA.',
          fechaCambio: '2026-10-07T15:05:00',
        },
      ],
      accionesDisponibles: ['AUTORIZAR_REPARACION', 'DEVOLVER', 'RECHAZAR'],
    };
  }
}
