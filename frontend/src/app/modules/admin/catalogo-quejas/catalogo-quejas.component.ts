import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-catalogo-quejas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './catalogo-quejas.component.html',
  styleUrls: ['./catalogo-quejas.component.css'],
})
export class CatalogoQuejasComponent implements OnInit {
  backendUrl: string = environment.apiUrl;

  filtros = {
    fechaDesde: '',
    fechaHasta: '',
    categoriaId: null as number | null,
    estado: 'TODAS',
    prioridad: 'TODAS',
    busqueda: '',
  };

  registros: any[] = [];
  cargando: boolean = false;
  mensajeSinResultados: boolean = false;
  listaCategorias: any[] = [];

  paginaActual: number = 0;
  tamanoPagina: number = 20;
  totalPaginas: number = 0;
  totalElementos: number = 0;

  estadosDisponibles: string[] = [
    'TODAS',
    'REGISTRADA',
    'EN INSPECCIÓN',
    'EN REPARACIÓN TÉCNICA',
    'EN VALIDACIÓN DE REPARACIÓN',
    'PENDIENTE DE CIERRE',
    'SOLUCIONADA / CERRADA',
    'RECHAZADA',
  ];
  prioridadesDisponibles: string[] = ['TODAS', 'BAJA', 'MEDIA', 'ALTA', 'URGENTE'];

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.cargarCategorias();
    this.buscar();
  }

  private obtenerHeaders(): HttpHeaders {
    const token = localStorage.getItem('token') || localStorage.getItem('jwt');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  cargarCategorias(): void {
    const headers = this.obtenerHeaders();
    this.http.get<any[]>(`${this.backendUrl}/admin/quejas/categorias`, { headers }).subscribe({
      next: (res) => (this.listaCategorias = res),
      error: (err) => console.error('Error al cargar categorías:', err),
    });
  }

  buscar(): void {
    this.cargando = true;
    this.mensajeSinResultados = false;
    const headers = this.obtenerHeaders();

    let params = new HttpParams()
      .set('pagina', this.paginaActual.toString())
      .set('tamanoPagina', this.tamanoPagina.toString());

    if (this.filtros.busqueda && this.filtros.busqueda.trim() !== '') {
      params = params.set('buscar', this.filtros.busqueda.trim());
    }

    if (this.filtros.estado && this.filtros.estado !== 'TODAS') {
      params = params.set('estado', this.filtros.estado.trim());
    }

    if (this.filtros.categoriaId !== null && this.filtros.categoriaId !== undefined) {
      const idCat = Number(this.filtros.categoriaId);
      if (!isNaN(idCat) && idCat > 0) {
        params = params.set('categoriaId', idCat.toString());
      }
    }

    if (this.filtros.prioridad && this.filtros.prioridad !== 'TODAS' && this.filtros.prioridad.trim() !== '') {
      params = params.set('prioridad', this.filtros.prioridad.trim());
    }

    if (this.filtros.fechaDesde) {
      params = params.set('fechaInicio', this.filtros.fechaDesde);
    }

    if (this.filtros.fechaHasta) {
      params = params.set('fechaFin', this.filtros.fechaHasta);
    }

    // Ruta oficial del catálogo
    this.http.get<any>(`${this.backendUrl}/admin/quejas/catalogo`, { params, headers }).subscribe({
      next: (response) => {
        this.registros = response.content || response || [];
        this.totalPaginas = response.totalPages || 0;
        this.totalElementos = response.totalElements || 0;
        this.mensajeSinResultados = this.registros.length === 0;
        this.cargando = false;
      },
      error: (error) => {
        console.error('Error al consultar el catálogo:', error);
        this.registros = [];
        this.mensajeSinResultados = true;
        this.cargando = false;
      },
    });
  }

  aplicarFiltros(): void {
    this.paginaActual = 0;
    this.buscar();
  }

  limpiarFiltros(): void {
    this.filtros = {
      fechaDesde: '',
      fechaHasta: '',
      categoriaId: null,
      estado: 'TODAS',
      prioridad: 'TODAS',
      busqueda: '',
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

  exportarPdf(): void {
    const params = this.construirParametrosFiltro();
    const headers = this.obtenerHeaders();

    this.http
      .get(`${this.backendUrl}/admin/quejas/exportar/pdf`, {
        params,
        headers,
        responseType: 'blob',
      })
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `Catalogo_Quejas_${new Date().getTime()}.pdf`;
          a.click();
          window.URL.revokeObjectURL(url);
        },
        error: (err) => console.error('Error al exportar PDF:', err),
      });
  }

  exportarExcel(): void {
    const params = this.construirParametrosFiltro();
    const headers = this.obtenerHeaders();

    this.http
      .get(`${this.backendUrl}/admin/quejas/exportar/excel`, {
        params,
        headers,
        responseType: 'blob',
      })
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `Catalogo_Quejas_${new Date().getTime()}.xlsx`;
          a.click();
          window.URL.revokeObjectURL(url);
        },
        error: (err) => console.error('Error al exportar a Excel:', err),
      });
  }

  private construirParametrosFiltro(): HttpParams {
    let params = new HttpParams();

    if (this.filtros.busqueda) params = params.set('buscar', this.filtros.busqueda.trim());
    if (this.filtros.estado && this.filtros.estado !== 'TODAS')
      params = params.set('estado', this.filtros.estado.trim());
    if (this.filtros.categoriaId && Number(this.filtros.categoriaId) > 0)
      params = params.set('categoriaId', Number(this.filtros.categoriaId).toString());
    if (this.filtros.prioridad && this.filtros.prioridad !== 'TODAS')
      params = params.set('prioridad', this.filtros.prioridad.trim());
    if (this.filtros.fechaDesde) params = params.set('fechaInicio', this.filtros.fechaDesde);
    if (this.filtros.fechaHasta) params = params.set('fechaFin', this.filtros.fechaHasta);

    return params;
  }

  obtenerClasePrioridad(prioridad: string): string {
    switch (prioridad?.toUpperCase()) {
      case 'URGENTE':
        return 'badge bg-danger';
      case 'ALTA':
        return 'badge bg-warning text-dark';
      case 'MEDIA':
        return 'badge bg-info text-dark';
      case 'BAJA':
        return 'badge bg-secondary';
      default:
        return 'badge bg-light text-dark';
    }
  }
}
