import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { InspectorService } from '../../../../core/services/inspector.service';
import {
  QuejaInspectorBandejaDTO,
  RegistroInspeccionDTO,
  MensajeResponse
} from '../../../../core/models/queja.model';

interface FotoAdjunta {
  file?: File;
  previewUrl: string;
  urlBackend: string;
}

@Component({
  selector: 'app-inspeccion-cuadrilla',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './inspeccion-cuadrilla.html',
  styleUrl: './inspeccion-cuadrilla.css'
})
export class InspeccionCuadrillaComponent implements OnInit {
  // --- INSPECTOR DE CAMPO ---
  asignaciones: QuejaInspectorBandejaDTO[] = [];
  quejaSeleccionada?: QuejaInspectorBandejaDTO;
  cargandoAsignaciones: boolean = false;
  enviandoInforme: boolean = false;

  // Formulario de Inspección
  problemaVerificado: boolean = true;
  gravedad: 'LEVE' | 'MODERADA' | 'GRAVE' | 'CRITICA' | '' = 'MODERADA';
  diagnostico: string = '';
  recursosSugeridos: string = '';
  instruccionesCuadrilla: string = '';
  fotosSubidas: FotoAdjunta[] = [];

  // Modales de Confirmación y Éxito
  mostrarModalConfirmar: boolean = false;
  modalExito = {
    visible: false,
    correlativo: '',
    nuevoEstado: '',
    mensaje: ''
  };

  // Control Visual y Notificaciones
  imagenAmpliadaUrl: string | null = null;
  toast = {
    visible: false,
    mensaje: '',
    tipo: 'success' as 'success' | 'error' | 'warning' | 'info'
  };
  private toastTimeout: any = null;

  constructor(private inspectorService: InspectorService) {}

  ngOnInit(): void {
    this.cargarAsignacionesInspector();
  }

  cargarAsignacionesInspector(): void {
    this.cargandoAsignaciones = true;
    this.inspectorService.obtenerAsignaciones().pipe(
      finalize(() => {
        this.cargandoAsignaciones = false;
      })
    ).subscribe({
      next: (data) => {
        this.asignaciones = data || [];
      },
      error: (err) => {
        console.error('Error al cargar asignaciones del inspector:', err);
        const errorMsg = typeof err?.error?.error === 'string'
          ? err.error.error
          : (err.message || 'No se pudieron cargar las asignaciones de inspección del servidor.');
        this.mostrarToast(errorMsg, 'error');
      }
    });
  }

  abrirQueja(q: QuejaInspectorBandejaDTO): void {
    this.quejaSeleccionada = q;
    this.mostrarModalConfirmar = false;
    this.resetFormulario();
  }

  salirDeQueja(): void {
    this.quejaSeleccionada = undefined;
    this.mostrarModalConfirmar = false;
    this.resetFormulario();
  }

  onSwitchProblemaVerificado(valor: boolean): void {
    this.problemaVerificado = valor;
    if (!valor) {
      this.gravedad = '';
      this.recursosSugeridos = '';
      this.instruccionesCuadrilla = '';
    } else if (!this.gravedad) {
      this.gravedad = 'MODERADA';
    }
  }

  onArchivosSeleccionados(event: any): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const files: File[] = Array.from(input.files);

    if (this.fotosSubidas.length + files.length > 3) {
      this.mostrarToast('Solo se permite adjuntar un máximo de 3 fotografías por informe.', 'warning');
      input.value = '';
      return;
    }

    files.forEach((file) => {
      if (!file.type.startsWith('image/')) {
        this.mostrarToast(`El archivo ${file.name} no es una imagen válida.`, 'warning');
        return;
      }

      const reader = new FileReader();
      reader.onload = (e: any) => {
        const previewUrl = e.target.result as string;
        const timestamp = Date.now();
        const safeName = file.name.replace(/[^a-zA-Z0-9._-]/g, '_');
        const urlBackend = `/subidas/evidencias/inspeccion_${timestamp}_${safeName}`;

        this.fotosSubidas.push({
          file,
          previewUrl,
          urlBackend
        });
      };
      reader.readAsDataURL(file);
    });

    input.value = '';
  }

  eliminarFoto(index: number): void {
    this.fotosSubidas.splice(index, 1);
  }

  esFormularioValido(): boolean {
    if (!this.quejaSeleccionada) return false;
    if (this.enviandoInforme) return false;

    const diagTrim = this.diagnostico ? this.diagnostico.trim() : '';
    if (diagTrim.length < 20) return false;

    if (this.problemaVerificado && !this.gravedad) return false;

    if (this.fotosSubidas.length < 1 || this.fotosSubidas.length > 3) return false;

    return true;
  }

  // Solicitud de confirmación previa
  solicitarConfirmacionEnvio(): void {
    if (!this.esFormularioValido()) return;
    this.mostrarModalConfirmar = true;
  }

  cancelarConfirmacion(): void {
    this.mostrarModalConfirmar = false;
  }

  // Envío real con protección finalize (anti-bloqueo)
  procesarEnvioInforme(): void {
    if (!this.quejaSeleccionada || !this.esFormularioValido() || this.enviandoInforme) return;

    this.mostrarModalConfirmar = false;
    this.enviandoInforme = true;

    const dto: RegistroInspeccionDTO = {
      problemaVerificado: this.problemaVerificado,
      gravedad: this.problemaVerificado ? this.gravedad : undefined,
      diagnostico: this.diagnostico.trim(),
      recursosSugeridos: this.problemaVerificado && this.recursosSugeridos.trim() ? this.recursosSugeridos.trim() : undefined,
      instruccionesCuadrilla: this.problemaVerificado && this.instruccionesCuadrilla.trim() ? this.instruccionesCuadrilla.trim() : undefined,
      fotos: this.fotosSubidas.map(f => f.urlBackend)
    };

    const quejaId = this.quejaSeleccionada.quejaId;
    const correlativo = this.quejaSeleccionada.correlativo;

    this.inspectorService.registrarInspeccion(quejaId, dto).pipe(
      finalize(() => {
        this.enviandoInforme = false;
      })
    ).subscribe({
      next: (res: MensajeResponse) => {
        const msg = res?.mensaje || `Informe de inspección registrado exitosamente para la queja ${correlativo}.`;
        const nuevoEst = res?.nuevoEstado || 'EN VALIDACIÓN DE REPARACIÓN';

        // Desplegar Modal de Éxito
        this.modalExito = {
          visible: true,
          correlativo: correlativo,
          nuevoEstado: nuevoEst,
          mensaje: msg
        };

        // Retirar la queja procesada de la lista
        this.asignaciones = this.asignaciones.filter(item => item.quejaId !== quejaId);
      },
      error: (err) => {
        console.error('Error al registrar informe de inspección:', err);
        const errorMsg = typeof err?.error?.error === 'string'
          ? err.error.error
          : (err.message || 'Ocurrió un error al enviar el informe técnico al servidor.');
        this.mostrarToast(errorMsg, 'error');
      }
    });
  }

  cerrarModalExitoYVolver(): void {
    this.modalExito.visible = false;
    this.salirDeQueja();
  }

  resetFormulario(): void {
    this.problemaVerificado = true;
    this.gravedad = 'MODERADA';
    this.diagnostico = '';
    this.recursosSugeridos = '';
    this.instruccionesCuadrilla = '';
    this.fotosSubidas = [];
  }

  // Helpers
  getUrlImagen(url?: string): string {
    if (!url) return 'https://images.unsplash.com/photo-1584467735871-8e85353a8413?w=500';
    if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:')) {
      return url;
    }
    return `http://localhost:8080${url}`;
  }

  getGoogleMapsUrl(lat?: number, lng?: number): string {
    if (lat === undefined || lng === undefined || lat === null || lng === null) {
      return '#';
    }
    return `https://maps.google.com/?q=${lat},${lng}`;
  }

  abrirImagenModal(url: string): void {
    this.imagenAmpliadaUrl = this.getUrlImagen(url);
  }

  cerrarImagenModal(): void {
    this.imagenAmpliadaUrl = null;
  }

  getBadgePrioridadClass(prioridad?: string): string {
    switch (prioridad?.toUpperCase()) {
      case 'URGENTE': return 'badge-urgente';
      case 'ALTA': return 'badge-alta';
      case 'MEDIA': return 'badge-media';
      case 'BAJA': return 'badge-baja';
      default: return 'badge-media';
    }
  }

  mostrarToast(mensaje: string, tipo: 'success' | 'error' | 'warning' | 'info' = 'info'): void {
    if (this.toastTimeout) clearTimeout(this.toastTimeout);
    this.toast = { visible: true, mensaje, tipo };
    this.toastTimeout = setTimeout(() => {
      this.toast.visible = false;
    }, 5000);
  }

  cerrarToast(): void {
    this.toast.visible = false;
  }
}
