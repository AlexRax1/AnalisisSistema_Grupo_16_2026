import { Component, OnInit, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PersonalAdminService } from '../../../core/services/personal-admin.service';
import { EmpleadoDTO, GuardarEmpleadoDTO } from '../../../core/models/personal-admin.model';

@Component({
  selector: 'app-gestion-personal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './gestion-personal.component.html',
  styleUrls: ['./gestion-personal.component.css'],
})
export class GestionPersonalComponent implements OnInit {
  empleados: EmpleadoDTO[] = [];
  cargando: boolean = false;

  // Modales
  mostrarModal: boolean = false;
  modoEdicion: boolean = false;
  empleadoIdEdicion?: number;
  guardando: boolean = false;
  errorModal: string = '';

  // Formulario Empleado
  formEmpleado: GuardarEmpleadoDTO = this.limpiarFormulario();

  rolesDisponibles = [
    'Ciudadano',
    'Funcionario Municipal',
    'Inspector de Campo',
    'Especialista Técnico',
    'Administrador',
  ];

  dependenciasDisponibles = [
    'Dirección de Servicios Públicos',
    'Unidad de Alumbrado',
    'Fontanería y Drenajes',
    'Bacheo y Pavimentación',
    'Limpieza y Recolección',
    'Parques y Ornato',
  ];

  constructor(
    private personalService: PersonalAdminService,
    private ngZone: NgZone,
  ) {}

  ngOnInit(): void {
    this.cargarEmpleados();
  }

  cargarEmpleados(): void {
    this.cargando = true;
    this.personalService.obtenerEmpleados().subscribe({
      next: (data) => {
        this.ngZone.run(() => {
          this.empleados = data || [];
          this.cargando = false;
        });
      },
      error: (err) => {
        console.error('Error al cargar personal:', err);
        this.ngZone.run(() => {
          this.cargando = false;
        });
      },
    });
  }

  abrirModalNuevo(): void {
    this.modoEdicion = false;
    this.errorModal = '';
    this.formEmpleado = this.limpiarFormulario();
    this.mostrarModal = true;
  }

  abrirModalEditar(emp: EmpleadoDTO): void {
    this.modoEdicion = true;
    this.empleadoIdEdicion = emp.id;
    this.errorModal = '';
    this.formEmpleado = {
      dpi: emp.dpi,
      nombres: emp.nombres,
      apellidos: emp.apellidos,
      correoInstitucional: emp.correoInstitucional,
      telefono: emp.telefono,
      rol: emp.rol,
      dependencia: emp.dependencia,
    };
    this.mostrarModal = true;
  }

  cerrarModal(): void {
    this.mostrarModal = false;
  }

  guardarEmpleado(): void {
    if (!this.validarCampos()) return;

    this.guardando = true;
    this.errorModal = '';

    if (this.modoEdicion && this.empleadoIdEdicion) {
      this.personalService.actualizarEmpleado(this.empleadoIdEdicion, this.formEmpleado).subscribe({
        next: (res) => {
          this.ngZone.run(() => {
            alert(res.mensaje || 'Empleado actualizado exitosamente.');
            this.guardando = false;
            this.cerrarModal();
            this.cargarEmpleados();
          });
        },
        error: (err) => {
          this.ngZone.run(() => {
            const mensajeServidor = err.error?.mensaje || err.error?.message || '';

            // Detecta si es un error de duplicidad en la base de datos
            if (
              mensajeServidor.includes('llave duplicada') ||
              mensajeServidor.includes('duplicate key') ||
              mensajeServidor.includes('credenciales_username_key') ||
              err.status === 400 ||
              err.status === 409
            ) {
              this.errorModal =
                'El DPI o correo electrónico ingresado ya se encuentra registrado para otro empleado.';
            } else {
              this.errorModal = 'Error al actualizar el empleado.';
            }

            this.guardando = false;
          });
        },
      });
    } else {
      this.personalService.registrarEmpleado(this.formEmpleado).subscribe({
        next: (res) => {
          this.ngZone.run(() => {
            alert(res.mensaje || 'Empleado registrado exitosamente.');
            this.guardando = false;
            this.cerrarModal();
            this.cargarEmpleados();
          });
        },
        error: (err) => {
          this.ngZone.run(() => {
            const mensajeServidor = err.error?.mensaje || err.error?.message || '';

            // Intercepta la restricción de unicidad de PostgreSQL (FA02)
            if (
              mensajeServidor.includes('llave duplicada') ||
              mensajeServidor.includes('duplicate key') ||
              mensajeServidor.includes('credenciales_username_key') ||
              err.status === 400 ||
              err.status === 409 ||
              err.status === 500
            ) {
              this.errorModal =
                'El DPI o correo electrónico ingresado ya se encuentra registrado para otro empleado.';
            } else {
              this.errorModal = 'Ocurrió un error al registrar el empleado.';
            }

            this.guardando = false;
          });
        },
      });
    }
  }

  alternarEstado(emp: EmpleadoDTO): void {
    const accion = emp.activo ? 'desactivar' : 'activar';
    if (
      confirm(`¿Está seguro de que desea ${accion} la cuenta de ${emp.nombres} ${emp.apellidos}?`)
    ) {
      this.personalService.cambiarEstado(emp.id!, !emp.activo).subscribe({
        next: (res) => {
          this.ngZone.run(() => {
            alert(res.mensaje);
            this.cargarEmpleados();
          });
        },
        error: (err) => alert('Error al cambiar el estado del empleado.'),
      });
    }
  }

  private validarCampos(): boolean {
    const { dpi, nombres, apellidos, correoInstitucional, telefono, rol, dependencia } =
      this.formEmpleado;
    if (
      !dpi ||
      !nombres ||
      !apellidos ||
      !correoInstitucional ||
      !telefono ||
      !rol ||
      !dependencia
    ) {
      this.errorModal = 'Debe completar todos los datos obligatorios (FA01).';
      return false;
    }
    if (!correoInstitucional.endsWith('@muni.gob.gt')) {
      this.errorModal = 'El correo debe tener formato institucional (@muni.gob.gt).';
      return false;
    }
    return true;
  }

  private limpiarFormulario(): GuardarEmpleadoDTO {
    return {
      dpi: '',
      nombres: '',
      apellidos: '',
      correoInstitucional: '',
      telefono: '',
      rol: 'Funcionario Municipal',
      dependencia: 'Dirección de Servicios Públicos',
    };
  }
}
