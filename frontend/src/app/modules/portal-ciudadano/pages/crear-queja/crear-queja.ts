import { Component, OnInit, AfterViewInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { QuejaService } from '../../../../core/services/queja.service';
import {
  CATEGORIAS_QUEJA,
  CategoriaQueja,
  SubcategoriaQueja,
  RegistroQuejaPayload,
  RespuestaRegistroQueja
} from '../../../../core/models/categoria-queja.model';

declare const L: any; // Leaflet Global JS

interface FotoAdjunta {
  file: File;
  previewUrl: string;
}

@Component({
  selector: 'app-crear-queja',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './crear-queja.html',
  styleUrl: './crear-queja.css'
})
export class CrearQuejaComponent implements OnInit, AfterViewInit, OnDestroy {
  categorias: CategoriaQueja[] = CATEGORIAS_QUEJA;
  subcategoriasDisponibles: SubcategoriaQueja[] = [];

  // Formulario
  categoriaId: number | null = null;
  subcategoriaId: number | null = null;
  direccionExacta: string = '';
  puntoReferencia: string = '';
  descripcion: string = '';

  // Geolocalización / Coordenadas enfocadas en San Juan Sacatepéquez
  // Coordenadas fijas por defecto de San Juan Sacatepéquez: 14.7175, -90.6442
  readonly SAN_JUAN_LAT: number = 14.7175;
  readonly SAN_JUAN_LNG: number = -90.6442;

  latitud: number = 14.7175;
  longitud: number = -90.6442;
  obteniendoUbicacion: boolean = false;

  // Leaflet Map & Marker
  private map: any;
  private marker: any;

  // Evidencias (fotos)
  fotos: FotoAdjunta[] = [];

  // Validaciones y estados de UI
  errorMensaje: string = '';
  cargando: boolean = false;
  mostrarModalConfirmacion: boolean = false;
  mostrarModalCancelar: boolean = false;
  resultadoExitoso: RespuestaRegistroQueja | null = null;

  constructor(
    private quejaService: QuejaService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    if (this.categorias.length > 0) {
      this.categoriaId = this.categorias[0].id;
      this.onCategoriaChange();
    }
  }

  ngAfterViewInit() {
    this.initMap();
  }

  ngOnDestroy() {
    if (this.map) {
      this.map.remove();
    }
  }

  // Rango geográfico delimitado del municipio de San Juan Sacatepéquez
  // Latitud: ~ 14.65 a 14.80, Longitud: ~ -90.72 a -90.58
  readonly MIN_LAT = 14.64;
  readonly MAX_LAT = 14.81;
  readonly MIN_LNG = -90.73;
  readonly MAX_LNG = -90.57;

  private initMap() {
    if (typeof L === 'undefined') {
      console.warn('Leaflet script no ha cargado aún.');
      return;
    }

    const boundsSanJuan = L.latLngBounds(
      [this.MIN_LAT, this.MIN_LNG],
      [this.MAX_LAT, this.MAX_LNG]
    );

    // Inicializar mapa centrado en San Juan Sacatepéquez y restringido con maxBounds
    this.map = L.map('map-container', {
      center: [this.latitud, this.longitud],
      zoom: 15,
      minZoom: 13,
      maxZoom: 19,
      maxBounds: boundsSanJuan,
      maxBoundsViscosity: 1.0 // Impide arrastrar el mapa fuera del municipio
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap - Muni San Juan Sacatepéquez'
    }).addTo(this.map);

    // Crear marcador arrastrable
    this.marker = L.marker([this.latitud, this.longitud], { draggable: true }).addTo(this.map);
    this.marker.bindPopup('<b>Ubicación del Incidente</b><br>Arrastra el marcador o haz clic en el mapa.').openPopup();

    // Evento al arrastrar el marcador manualmente
    this.marker.on('dragend', () => {
      const position = this.marker.getLatLng();
      this.validarYAsignarPunto(position.lat, position.lng);
    });

    // Evento al hacer clic en cualquier parte del mapa
    this.map.on('click', (e: any) => {
      const { lat, lng } = e.latlng;
      this.validarYAsignarPunto(lat, lng);
    });
  }

  // Mensaje de notificación sutil para acciones del mapa (desaparece automáticamente)
  mapaNotificacionSutil: string = '';
  mapaNotificacionTipo: 'info' | 'error' | 'exito' = 'info';
  private notificacionTimeout: any;

  mostrarNotificacionSutil(mensaje: string, tipo: 'info' | 'error' | 'exito' = 'info') {
    this.mapaNotificacionSutil = mensaje;
    this.mapaNotificacionTipo = tipo;
    this.cdr.detectChanges();

    if (this.notificacionTimeout) {
      clearTimeout(this.notificacionTimeout);
    }

    this.notificacionTimeout = setTimeout(() => {
      this.mapaNotificacionSutil = '';
      this.cdr.detectChanges();
    }, 4000);
  }

  private validarYAsignarPunto(lat: number, lng: number): boolean {
    // Verificar si el punto está dentro del municipio de San Juan Sacatepéquez
    const enSanJuan = lat >= this.MIN_LAT && lat <= this.MAX_LAT && lng >= this.MIN_LNG && lng <= this.MAX_LNG;

    if (enSanJuan) {
      this.marker.setLatLng([lat, lng]);
      this.actualizarCoordenadas(lat, lng);
      this.errorMensaje = '';
      return true;
    } else {
      this.resetearASanJuan();
      this.mostrarNotificacionSutil('Ubicación fuera de la cobertura de San Juan Sacatepéquez. Se reajustó la vista.', 'error');
      return false;
    }
  }

  private actualizarCoordenadas(lat: number, lng: number) {
    this.latitud = Number(lat.toFixed(7));
    this.longitud = Number(lng.toFixed(7));
    this.cdr.detectChanges();
  }

  onCategoriaChange() {
    const cat = this.categorias.find((c) => c.id === Number(this.categoriaId));
    if (cat) {
      this.subcategoriasDisponibles = cat.subcategorias;
      if (this.subcategoriasDisponibles.length > 0) {
        this.subcategoriaId = this.subcategoriasDisponibles[0].id;
      } else {
        this.subcategoriaId = null;
      }
    } else {
      this.subcategoriasDisponibles = [];
      this.subcategoriaId = null;
    }
  }

  obtenerUbicacionActual() {
    if (!navigator.geolocation) {
      this.resetearASanJuan();
      this.mostrarNotificacionSutil('Geolocalización no soportada en este dispositivo.', 'error');
      return;
    }

    this.obteniendoUbicacion = true;
    this.cdr.detectChanges();

    navigator.geolocation.getCurrentPosition(
      (position) => {
        const lat = position.coords.latitude;
        const lng = position.coords.longitude;
        
        const esValido = this.validarYAsignarPunto(lat, lng);
        this.obteniendoUbicacion = false;

        if (esValido) {
          this.mostrarNotificacionSutil('Ubicación detectada correctamente.', 'exito');
        } else {
          this.mostrarNotificacionSutil('Tu GPS / Proveedor de internet reporta una ubicación fuera de San Juan Sacatepéquez.', 'error');
        }

        this.cdr.detectChanges();
      },
      (error) => {
        console.warn('GPS no disponible:', error);
        this.resetearASanJuan();
        this.obteniendoUbicacion = false;
        this.mostrarNotificacionSutil('No se pudo obtener la señal GPS de tu equipo.', 'error');
      },
      { timeout: 8000, enableHighAccuracy: true }
    );
  }

  resetearASanJuan() {
    this.actualizarCoordenadas(this.SAN_JUAN_LAT, this.SAN_JUAN_LNG);

    if (this.map && this.marker) {
      this.map.setView([this.SAN_JUAN_LAT, this.SAN_JUAN_LNG], 15);
      this.marker.setLatLng([this.SAN_JUAN_LAT, this.SAN_JUAN_LNG]);
    }

    this.obteniendoUbicacion = false;
    this.cdr.detectChanges();
  }

  onFotosSeleccionadas(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    this.errorMensaje = '';
    const archivos = Array.from(input.files);

    if (this.fotos.length + archivos.length > 3) {
      this.errorMensaje = 'Máximo 3 fotografías de evidencia permitidas.';
      input.value = '';
      return;
    }

    for (const file of archivos) {
      // FA02: Validación formato JPG, JPEG, PNG
      const extensionesValidas = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
      if (!extensionesValidas.includes(file.type.toLowerCase())) {
        this.errorMensaje = 'FA02: Solo se admiten archivos de imagen en formato JPG, JPEG o PNG.';
        input.value = '';
        return;
      }

      // FA03: Validación tamaño máximo 5MB
      const maxTamano = 5 * 1024 * 1024; // 5 MB
      if (file.size > maxTamano) {
        this.errorMensaje = 'FA03: El archivo adjunto excede el tamaño máximo permitido de 5 MB.';
        input.value = '';
        return;
      }

      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.fotos.push({
          file: file,
          previewUrl: e.target.result
        });
        this.cdr.detectChanges();
      };
      reader.readAsDataURL(file);
    }

    input.value = '';
  }

  eliminarFoto(index: number) {
    this.fotos.splice(index, 1);
  }

  validarFormulario(): boolean {
    this.errorMensaje = '';

    // FA01: Campos obligatorios
    if (!this.categoriaId || !this.subcategoriaId || !this.direccionExacta.trim()) {
      this.errorMensaje = 'FA01: Debe completar todos los campos obligatorios para registrar la queja (Categoría, Subcategoría y Dirección / Aldea).';
      return false;
    }

    // RN10: Al menos 1 foto
    if (this.fotos.length < 1) {
      this.errorMensaje = 'RN10: Debe adjuntar al menos 1 fotografía como evidencia del problema.';
      return false;
    }

    // FA04: Descripción mínima 20 caracteres
    if (!this.descripcion || this.descripcion.trim().length < 20) {
      this.errorMensaje = 'FA04: La descripción debe contener al menos 20 caracteres para detallar el problema.';
      return false;
    }

    if (this.descripcion.trim().length > 500) {
      this.errorMensaje = 'La descripción no puede exceder los 500 caracteres.';
      return false;
    }

    return true;
  }

  onSubmit() {
    if (!this.validarFormulario()) {
      window.scrollTo({ top: 0, behavior: 'smooth' });
      return;
    }

    const payload: RegistroQuejaPayload = {
      categoriaId: Number(this.categoriaId),
      subcategoriaId: Number(this.subcategoriaId),
      zona: 1, // Valor por omisión para compatibilidad con el backend
      direccionExacta: this.direccionExacta.trim(),
      puntoReferencia: this.puntoReferencia.trim(),
      latitud: this.latitud,
      longitud: this.longitud,
      descripcion: this.descripcion.trim()
    };

    const archivosFotos = this.fotos.map((f) => f.file);

    this.cargando = true;
    this.quejaService.registrarQueja(payload, archivosFotos).subscribe({
      next: (res) => {
        this.cargando = false;
        this.resultadoExitoso = res;
        this.mostrarModalConfirmacion = true;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.cargando = false;
        console.error('Error al registrar queja:', err);
        this.errorMensaje = err?.error?.message || 'Ocurrió un error al enviar la queja al servidor. Por favor verifica tu conexión o autenticación.';
        window.scrollTo({ top: 0, behavior: 'smooth' });
        this.cdr.detectChanges();
      }
    });
  }

  solicitarCancelar() {
    this.mostrarModalCancelar = true;
  }

  confirmarCancelar() {
    this.mostrarModalCancelar = false;
    this.router.navigate(['/ciudadano/mis-quejas']);
  }

  cerrarExitoYNavegar() {
    this.mostrarModalConfirmacion = false;
    this.router.navigate(['/ciudadano/mis-quejas']);
  }
}
