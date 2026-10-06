import { Component, OnInit, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  ItemQuejaAdminDTO,
  FiltroCatalogoAdminDTO,
} from '../../../core/models/catalogo-admin.model';
import { AdminQuejasService } from '../../../core/services/admin-quejas.services';

@Component({
  selector: 'app-catalogo-quejas-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './catalogo-quejas.component.html',
  styleUrls: ['./catalogo-quejas.component.css'],
})
export class CatalogoQuejasAdminComponent implements OnInit {
  // Listas y Paginación
  registros: ItemQuejaAdminDTO[] = [];
  totalElementos: number = 0;
  totalPaginas: number = 0;
  paginaActual: number = 0;
  tamanoPagina: number = 20;

  cargando: boolean = false;
  mensajeSinResultados: boolean = false;

  // Filtros (RN07, RN14, RN28)
  filtros: FiltroCatalogoAdminDTO = {
    fechaDesde: '',
    fechaHasta: '',
    categoria: 'TODAS',
    estado: 'TODAS',
    prioridad: 'TODAS',
    busqueda: '',
    pagina: 0,
    tamanoPagina: 20,
  };

  categoriasDisponibles = ['TODAS', 'ALUMBRADO', 'DRENAJES', 'VIALIDAD', 'RESIDUOS', 'ORNATO'];
  estadosDisponibles = [
    'TODAS',
    'REGISTRADA',
    'EN INSPECCIÓN',
    'EN REPARACIÓN TÉCNICA',
    'EN VALIDACIÓN DE REPARACIÓN',
    'SOLUCIONADA / CERRADA',
    'RECHAZADA',
  ];
  prioridadesDisponibles = ['TODAS', 'BAJA', 'MEDIA', 'ALTA', 'URGENTE'];

  constructor(
    private adminQuejasService: AdminQuejasService,
    private ngZone: NgZone,
  ) {}

  ngOnInit(): void {
    this.buscar();
  }

  buscar(): void {
    this.cargando = true;
    this.mensajeSinResultados = false;
    this.filtros.pagina = this.paginaActual;

    this.adminQuejasService.consultarCatalogo(this.filtros).subscribe({
      next: (res) => {
        this.ngZone.run(() => {
          this.registros = res.contenido || [];
          this.totalElementos = res.totalElementos;
          this.totalPaginas = res.totalPaginas;
          this.mensajeSinResultados = this.registros.length === 0;
          this.cargando = false;
        });
      },
      error: (err) => {
        console.error('Error al consultar catálogo:', err);
        this.ngZone.run(() => {
          this.cargando = false;
        });
      },
    });
  }

  limpiarFiltros(): void {
    this.filtros = {
      fechaDesde: '',
      fechaHasta: '',
      categoria: 'TODAS',
      estado: 'TODAS',
      prioridad: 'TODAS',
      busqueda: '',
      pagina: 0,
      tamanoPagina: 20,
    };
    this.paginaActual = 0;
    this.buscar();
  }

  cambiarPagina(nuevaPagina: number): void {
    if (nuevaPagina >= 0 && nuevaPagina < this.totalPaginas) {
      this.paginaActual = nuevaPagina;
      this.buscar();
    }
  }

  exportarExcel(): void {
    this.adminQuejasService.exportarExcel(this.filtros).subscribe({
      next: (blob) => this.descargarArchivo(blob, 'Catalogo_Quejas_Municipal.xlsx'),
      error: (err) => alert('Error al generar la exportación a Excel.'),
    });
  }

  exportarPdf(): void {
    this.adminQuejasService.exportarPdf(this.filtros).subscribe({
      next: (blob) => this.descargarArchivo(blob, 'Catalogo_Quejas_Municipal.pdf'),
      error: (err) => alert('Error al generar la exportación a PDF.'),
    });
  }

  private descargarArchivo(blob: Blob, nombreArchivo: string): void {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = nombreArchivo;
    a.click();
    window.URL.revokeObjectURL(url);
  }

  obtenerClasePrioridad(prioridad: string): string {
    switch (prioridad) {
      case 'URGENTE':
        return 'badge bg-danger';
      case 'ALTA':
        return 'badge bg-warning text-dark';
      case 'MEDIA':
        return 'badge bg-info text-dark';
      default:
        return 'badge bg-secondary';
    }
  }
}
