import { Component, NgZone, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { QuejaService } from '../../../../core/services/queja.service';
import { QuejaDetalleDTO } from '../../../../core/models/queja.model';

const ESTADO_COLORS: { [key: string]: string } = {
  REGISTRADA: 'badge-info',
  'EN INSPECCIÓN': 'badge-warning',
  'EN REPARACIÓN TÉCNICA': 'badge-primary',
  'EN VALIDACIÓN DE REPARACIÓN': 'badge-purple',
  'SOLUCIONADA / CERRADA': 'badge-success',
  RECHAZADA: 'badge-danger',
};

@Component({
  selector: 'app-mis-quejas',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './mis-quejas.html',
  styleUrl: './mis-quejas.css',
})
export class MisQuejasComponent implements OnInit {
  backendUrl: string = 'http://localhost:8080';
  quejasOriginales: QuejaDetalleDTO[] = [];
  quejas: QuejaDetalleDTO[] = [];
  quejaSeleccionada?: QuejaDetalleDTO;

  // Ponemos cargando en false por defecto para permitir la renderización inmediata
  cargando: boolean = false;
  busquedaCorrelativo: string = '';
  estadoSeleccionado: string = 'TODAS';
  mensajeBusquedaVacia: boolean = false;

  // Estado del Modal de Queja Derivada (FA05 y FA06)
  mostrarModalDerivada: boolean = false;
  tipoDerivacion: 'AGRAVAMIENTO' | 'REINCIDENCIA' = 'AGRAVAMIENTO';
  descripcionDerivada: string = '';
  fotosDerivada: File[] = [];
  errorModal: string = '';
  enviandoDerivada: boolean = false;

  estadosDisponibles: string[] = [
    'TODAS',
    'REGISTRADA',
    'EN INSPECCIÓN',
    'EN REPARACIÓN TÉCNICA',
    'EN VALIDACIÓN DE REPARACIÓN',
    'SOLUCIONADA / CERRADA',
    'RECHAZADA',
  ];

  constructor(
    private quejaService: QuejaService,
    private ngZone: NgZone,
  ) {}

  ngOnInit(): void {
    this.cargarQuejas();
  }

  cargarQuejas(): void {
    this.quejaService.obtenerMisQuejas().subscribe({
      next: (data) => {
        // Envolvemos en NgZone para obligar a Angular a refrescar el DOM al instante
        this.ngZone.run(() => {
          this.quejasOriginales = data || [];
          this.quejas = [...this.quejasOriginales];

          if (this.quejas.length > 0) {
            this.quejaSeleccionada = this.quejas[0];
          } else {
            this.quejaSeleccionada = undefined;
          }
          this.cargando = false;
        });
      },
      error: (err) => {
        console.error('Error al cargar quejas:', err);
        this.ngZone.run(() => {
          this.cargando = false;
        });
      },
    });
  }

  aplicarFiltros(): void {
    this.mensajeBusquedaVacia = false;
    const termino = this.busquedaCorrelativo.trim().toLowerCase();

    this.quejas = this.quejasOriginales.filter((q) => {
      const coincideCorrelativo =
        !termino || (q.correlativo && q.correlativo.toLowerCase().includes(termino));

      const coincideEstado =
        this.estadoSeleccionado === 'TODAS' || q.estadoActual === this.estadoSeleccionado;

      return coincideCorrelativo && coincideEstado;
    });

    if (this.quejas.length === 0 && termino !== '') {
      this.mensajeBusquedaVacia = true;
    }

    this.quejaSeleccionada = this.quejas.length > 0 ? this.quejas[0] : undefined;
  }

  limpiarFiltros(): void {
    this.busquedaCorrelativo = '';
    this.estadoSeleccionado = 'TODAS';
    this.mensajeBusquedaVacia = false;
    this.quejas = [...this.quejasOriginales];
    this.quejaSeleccionada = this.quejas.length > 0 ? this.quejas[0] : undefined;
  }

  verDetalle(queja: QuejaDetalleDTO): void {
    this.quejaSeleccionada = queja;
  }

  getUrlEvidencia(ev: any): string {
    if (!ev) return '';
    const url = typeof ev === 'string' ? ev : ev.urlArchivo;
    if (!url) return '';
    if (url.startsWith('http://') || url.startsWith('https://')) return url;
    if (url.startsWith('/')) return `${this.backendUrl}${url}`;
    return `${this.backendUrl}/${url}`;
  }

  obtenerClaseBadge(estado: string): string {
    return ESTADO_COLORS[estado] || 'badge-neutral';
  }

  esQuejaActiva(estado?: string): boolean {
    if (!estado) return false;
    return ['EN INSPECCIÓN', 'EN REPARACIÓN TÉCNICA', 'EN VALIDACIÓN DE REPARACIÓN'].includes(
      estado,
    );
  }

  esQuejaCerrada(estado?: string): boolean {
    return estado === 'SOLUCIONADA / CERRADA';
  }

  descargarPdf(correlativo: string): void {
    this.quejaService.descargarConstanciaPdf(correlativo).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Constancia_${correlativo}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => console.error('Error al descargar el PDF:', err),
    });
  }

  // --- LÓGICA MODAL DERIVADAS (FA05 y FA06) ---
  abrirModalDerivada(tipo: 'AGRAVAMIENTO' | 'REINCIDENCIA'): void {
    this.tipoDerivacion = tipo;
    this.descripcionDerivada = '';
    this.fotosDerivada = [];
    this.errorModal = '';
    this.mostrarModalDerivada = true;
  }

  cerrarModalDerivada(): void {
    this.mostrarModalDerivada = false;
  }

  onSeleccionarFotos(event: any): void {
    const files: FileList = event.target.files;
    if (files.length > 3) {
      this.errorModal = 'Únicamente puede adjuntar un máximo de 3 fotografías.';
      return;
    }
    this.errorModal = '';
    this.fotosDerivada = Array.from(files);
  }

  enviarReporteDerivado(): void {
    if (!this.quejaSeleccionada) return;

    if (
      this.descripcionDerivada.trim().length < 20 ||
      this.descripcionDerivada.trim().length > 500
    ) {
      this.errorModal = 'La descripción debe tener entre 20 y 500 caracteres.';
      return;
    }

    if (this.fotosDerivada.length === 0) {
      this.errorModal = 'Debe adjuntar al menos una fotografía de evidencia.';
      return;
    }

    this.enviandoDerivada = true;
    this.errorModal = '';

    const payload = {
      correlativoPadre: this.quejaSeleccionada.correlativo,
      tipoDerivacion: this.tipoDerivacion,
      descripcion: this.descripcionDerivada.trim(),
    };

    this.quejaService.registrarQuejaDerivada(payload, this.fotosDerivada).subscribe({
      next: (res) => {
        this.ngZone.run(() => {
          alert(res.mensaje);
          this.enviandoDerivada = false;
          this.cerrarModalDerivada();
          this.cargarQuejas(); // Refrescar el historial
        });
      },
      error: (err) => {
        this.ngZone.run(() => {
          this.errorModal = err.error || 'Ocurrió un error al enviar el reporte.';
          this.enviandoDerivada = false;
        });
      },
    });
  }
}

