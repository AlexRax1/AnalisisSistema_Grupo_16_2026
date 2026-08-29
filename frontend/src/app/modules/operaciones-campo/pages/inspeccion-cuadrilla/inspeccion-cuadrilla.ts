import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { QuejaMockService } from '../../../../core/services/queja-mock.service';
import { Queja, EstadoQueja } from '../../../../core/models/queja.model';

@Component({
  selector: 'app-inspeccion-cuadrilla',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './inspeccion-cuadrilla.html',
  styleUrl: './inspeccion-cuadrilla.css'
})
export class InspeccionCuadrillaComponent implements OnInit {
  quejasEnInspeccion: Queja[] = [];
  quejasReparadas: Queja[] = [];
  quejaSeleccionada?: Queja;

  rolCampo: 'INSPECTOR' | 'CUADRILLA' = 'INSPECTOR';

  // Datos de Dictamen del Inspector (CU09 / CU10)
  dictamenInspector: string = '';
  cuadrillaAsignada: string = 'Cuadrilla 01 - Asfalto y Bacheo';
  esProcedente: boolean = true;

  // Datos de Reparación de la Cuadrilla (CU12 / CU13)
  informeTecnico: string = '';
  fotoDespuesSimulada: string = '';

  cuadrillas: string[] = [
    'Cuadrilla 01 - Asfalto y Bacheo',
    'Cuadrilla 04 - Alcantarillado y Fugas',
    'Cuadrilla 08 - Alumbrado y Redes'
  ];

  constructor(private quejaService: QuejaMockService) {}

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos() {
    this.quejaService.obtenerTodas().subscribe((data: Queja[]) => {
      this.quejasEnInspeccion = data.filter(q => q.estado === 'EN_INSPECCION');
      this.quejasReparadas = data.filter(q => q.estado === 'REPARADA' || q.estado === 'ASIGNADA_CUADRILLA' || q.estado === 'EN_PROCESO');

      if (this.rolCampo === 'INSPECTOR' && this.quejasEnInspeccion.length > 0) {
        this.quejaSeleccionada = this.quejasEnInspeccion[0];
      } else if (this.rolCampo === 'CUADRILLA' && this.quejasReparadas.length > 0) {
        this.quejaSeleccionada = this.quejasReparadas[0];
      } else {
        this.quejaSeleccionada = undefined;
      }
    });
  }

  cambiarRol(nuevoRol: 'INSPECTOR' | 'CUADRILLA') {
    this.rolCampo = nuevoRol;
    this.cargarDatos();
  }

  seleccionar(queja: Queja) {
    this.quejaSeleccionada = queja;
  }

  // CU09 & CU10: Registrar dictamen de campo por el Inspector
  guardarDictamenInspector() {
    if (!this.quejaSeleccionada) return;

    if (!this.esProcedente) {
      this.quejaService.actualizarEstado(
        this.quejaSeleccionada.id,
        'RECHAZADA',
        `Dictamen Inspector (Improcedente): ${this.dictamenInspector}`,
        'Inspector Carlos Gómez'
      ).subscribe(() => {
        alert('Queja marcada como improcedente y rechazada.');
        this.limpiarFormulario();
        this.cargarDatos();
      });
      return;
    }

    const detalle = `Dictamen Procedente. Asignado a ${this.cuadrillaAsignada}. Obs: ${this.dictamenInspector}`;
    this.quejaService.actualizarEstado(
      this.quejaSeleccionada.id,
      'ASIGNADA_CUADRILLA',
      detalle,
      'Inspector Carlos Gómez'
    ).subscribe(() => {
      alert(`Inspección aprobada. Orden enviada a ${this.cuadrillaAsignada}`);
      this.limpiarFormulario();
      this.cargarDatos();
    });
  }

  simularFotoDespues(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.fotoDespuesSimulada = `https://placehold.co/600x400/10b981/ffffff?text=Reparado+${file.name}`;
    }
  }

  // CU12 & CU13: Finalizar trabajo de cuadrilla con foto del "Después"
  finalizarTrabajoCuadrilla() {
    if (!this.quejaSeleccionada) return;

    if (!this.fotoDespuesSimulada) {
      this.fotoDespuesSimulada = 'https://placehold.co/600x400/10b981/ffffff?text=Obra+Finalizada+OK';
    }

    this.quejaSeleccionada.fotosDespues = [this.fotoDespuesSimulada];

    const detalle = `Trabajo finalizado por Cuadrilla. Obs: ${this.informeTecnico || 'Reparación completada sin novedades'}`;
    this.quejaService.actualizarEstado(
      this.quejaSeleccionada.id,
      'REPARADA',
      detalle,
      'Líder de Cuadrilla'
    ).subscribe(() => {
      alert('Trabajo finalizado. Expediente listo para cierre formal.');
      this.limpiarFormulario();
      this.cargarDatos();
    });
  }

  private limpiarFormulario() {
    this.dictamenInspector = '';
    this.informeTecnico = '';
    this.fotoDespuesSimulada = '';
  }
}
