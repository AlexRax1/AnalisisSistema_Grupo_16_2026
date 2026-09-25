import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ESTADO_COLORS, QuejaDetalleDTO } from '../../../../core/models/queja.model';
import { QuejaService } from '../../../../core/services/queja.service';

@Component({
  selector: 'app-mis-quejas',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './mis-quejas.html',
  styleUrl: './mis-quejas.css',
})
export class MisQuejasComponent implements OnInit {
  quejasOriginales: QuejaDetalleDTO[] = [];
  quejas: QuejaDetalleDTO[] = [];
  quejaSeleccionada?: QuejaDetalleDTO;

  cargando: boolean = true;
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

  constructor(private quejaService: QuejaService) {}

  ngOnInit(): void {
    this.cargarQuejas();
  }

  cargarQuejas(): void {
    this.cargando = true;
    this.quejaService.obtenerMisQuejas().subscribe({
      next: (data) => {
        this.quejasOriginales = data;
        this.quejas = data;
        if (data.length > 0) {
          this.quejaSeleccionada = data[0];
        }
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error al cargar quejas:', err);
        this.cargando = false;
      },
    });
  }

  aplicarFiltros(): void {
    this.mensajeBusquedaVacia = false;
    this.quejas = this.quejasOriginales.filter((q) => {
      const coincideCorrelativo =
        !this.busquedaCorrelativo ||
        q.correlativo.toLowerCase().includes(this.busquedaCorrelativo.trim().toLowerCase());

      const coincideEstado =
        this.estadoSeleccionado === 'TODAS' || q.estadoActual === this.estadoSeleccionado;

      return coincideCorrelativo && coincideEstado;
    });

    if (this.quejas.length === 0 && this.busquedaCorrelativo.trim() !== '') {
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
        alert(res.mensaje);
        this.enviandoDerivada = false;
        this.cerrarModalDerivada();
        this.cargarQuejas(); // Refrescar el historial
      },
      error: (err) => {
        this.errorModal = err.error || 'Ocurrió un error al enviar el reporte.';
        this.enviandoDerivada = false;
      },
    });
  }
}
