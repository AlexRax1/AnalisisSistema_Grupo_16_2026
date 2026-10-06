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
            alert(res.mensaje);
            this.guardando = false;
            this.cerrarModal();
            this.cargarEmpleados();
          });
        },
        error: (err) => {
          this.ngZone.run(() => {
            this.errorModal = err.error?.mensaje || 'Error al actualizar el empleado.';
            this.guardando = false;
          });
        },
      });
    } else {
      this.personalService.registrarEmpleado(this.formEmpleado).subscribe({
        next: (res) => {
          this.ngZone.run(() => {
            alert(res.mensaje);
            this.guardando = false;
            this.cerrarModal();
            this.cargarEmpleados();
          });
        },
        error: (err) => {
          this.ngZone.run(() => {
            this.errorModal =
              err.error?.mensaje || 'El DPI o correo ya se encuentra registrado (FA02).';
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
