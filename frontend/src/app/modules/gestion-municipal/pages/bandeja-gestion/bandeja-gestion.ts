import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { QuejaMockService } from '../../../../core/services/queja-mock.service';
import { Queja, EstadoQueja } from '../../../../core/models/queja.model';

@Component({
  selector: 'app-bandeja-gestion',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './bandeja-gestion.html',
  styleUrl: './bandeja-gestion.css'
})
export class BandejaGestionComponent implements OnInit {
  quejasOriginales: Queja[] = [];
  quejasFiltradas: Queja[] = [];
  quejaSeleccionada?: Queja;

  // Filtros Avanzados (CU06)
  filtroZona: number = 0; // 0 = Todas
  filtroEstado: string = 'TODOS';
  filtroCategoria: string = 'TODAS';

  // Opciones de Asignación (CU07)
  inspectoresDisponibles: string[] = [
    'Inspector Carlos Gómez (Zona 1-10)',
    'Inspectora María Rodríguez (Zona 11-25)',
    'Inspector Jorge Morales (Obras Mayores)'
  ];
  inspectorSeleccionado: string = '';
  comentarioAsignacion: string = '';

  zonas: number[] = Array.from({ length: 25 }, (_, i) => i + 1);
  categorias: string[] = [
    'Vías e Infraestructura',
    'Agua Potable y Alcantarillado',
    'Alumbrado Público',
    'Limpieza y Desechos',
    'Parques y Áreas Verdes'
  ];

  constructor(private quejaService: QuejaMockService) {}

  ngOnInit(): void {
    this.cargarQuejas();
  }

  cargarQuejas() {
    this.quejaService.obtenerTodas().subscribe((data: Queja[]) => {
      this.quejasOriginales = this.calcularPrioridades(data);
      this.aplicarFiltros();
    });
  }

  // Algoritmo Simulado de Priorización por Volumen / Concentración (CU07)
  private calcularPrioridades(listado: Queja[]): Queja[] {
    const conteoPorZonaCat: { [key: string]: number } = {};

    listado.forEach(q => {
      const clave = `${q.zona}-${q.categoria}`;
      conteoPorZonaCat[clave] = (conteoPorZonaCat[clave] || 0) + 1;
    });

    return listado.map(q => {
      const clave = `${q.zona}-${q.categoria}`;
      // Si existen más de 2 quejas similares en la misma zona, sube a prioridad ALTA automáticamente
      if (conteoPorZonaCat[clave] >= 2) {
        q.prioridad = 'ALTA';
      }
      return q;
    });
  }

  aplicarFiltros() {
    this.quejasFiltradas = this.quejasOriginales.filter(q => {
      const cumpleZona = this.filtroZona === 0 || q.zona === Number(this.filtroZona);
      const cumpleEstado = this.filtroEstado === 'TODOS' || q.estado === this.filtroEstado;
      const cumpleCat = this.filtroCategoria === 'TODAS' || q.categoria === this.filtroCategoria;
      return cumpleZona && cumpleEstado && cumpleCat;
    });

    if (this.quejasFiltradas.length > 0) {
      this.quejaSeleccionada = this.quejasFiltradas[0];
    } else {
      this.quejaSeleccionada = undefined;
    }
  }

  seleccionar(queja: Queja) {
    this.quejaSeleccionada = queja;
  }

  asignarAInspector() {
    if (!this.quejaSeleccionada || !this.inspectorSeleccionado) {
      alert('Por favor selecciona un inspector.');
      return;
    }

    const detalle = `Asignado a ${this.inspectorSeleccionado}. Observación: ${this.comentarioAsignacion || 'Sin observaciones'}`;

    this.quejaService.actualizarEstado(
      this.quejaSeleccionada.id,
      'EN_INSPECCION',
      detalle,
      'Funcionario Municipal'
    ).subscribe(() => {
      alert(`Queja ${this.quejaSeleccionada?.correlativo} asignada exitosamente.`);
      this.inspectorSeleccionado = '';
      this.comentarioAsignacion = '';
      this.cargarQuejas();
    });
  }

  rechazarQueja() {
    if (!this.quejaSeleccionada) return;

    const motivo = prompt('Ingrese la razón del rechazo formal de la queja:');
    if (motivo) {
      this.quejaService.actualizarEstado(
        this.quejaSeleccionada.id,
        'RECHAZADA',
        `Rechazada: ${motivo}`,
        'Funcionario Municipal'
      ).subscribe(() => {
        alert('Queja rechazada formalmente.');
        this.cargarQuejas();
      });
    }
  }
}
