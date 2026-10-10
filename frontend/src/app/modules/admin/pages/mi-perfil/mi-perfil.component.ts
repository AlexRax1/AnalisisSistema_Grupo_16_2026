import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { PerfilUsuarioDTO } from '../../../../core/models/usuario.model';
import { UsuarioService } from '../../../../core/services/usuario.service';

@Component({
  selector: 'app-mi-perfil',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './mi-perfil.component.html',
  styleUrls: ['./mi-perfil.component.css'],
})
export class MiPerfilComponent implements OnInit {
  perfilForm!: FormGroup;
  cargando: boolean = false;
  guardando: boolean = false;
  mensajeExito: string = '';
  mensajeError: string = '';

  // Control visual de contraseña
  mostrarSeccionPassword: boolean = false;
  verPasswordActual: boolean = false;
  verNuevaPassword: boolean = false;
  verConfirmarPassword: boolean = false;

  constructor(
    private fb: FormBuilder,
    private usuarioService: UsuarioService,
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.cargarPerfil();
  }

  private initForm(): void {
    this.perfilForm = this.fb.group({
      // Campos no editables (desde base de datos)
      dpi: [{ value: '', disabled: true }],
      nombres: [{ value: '', disabled: true }],
      apellidos: [{ value: '', disabled: true }],
      correo: [{ value: '', disabled: true }],

      // Campos editables
      telefono: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
      direccion: ['', [Validators.required]],

      // Cambio opcional de contraseña
      passwordActual: [''],
      nuevaPassword: [''],
      confirmarNuevaPassword: [''],
    });
  }

  cargarPerfil(): void {
    this.cargando = true;
    this.cdr.detectChanges();
    this.usuarioService.obtenerPerfil().subscribe({
      next: (data: PerfilUsuarioDTO) => {
        if (data) {
          this.perfilForm.patchValue({
            dpi: data.dpi,
            nombres: data.nombres,
            apellidos: data.apellidos,
            correo: data.correo,
            telefono: data.telefono,
            direccion: data.direccion,
          });
        }
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.mensajeError = 'Error al cargar los datos del perfil.';
        this.cargando = false;
        this.cdr.detectChanges();
      },
    });
  }

  guardarCambios(): void {
    this.mensajeExito = '';
    this.mensajeError = '';

    if (this.perfilForm.invalid) {
      this.mensajeError = 'Debe ingresar los campos obligatorios correctamente.';
      this.cdr.detectChanges();
      return;
    }

    const formValues = this.perfilForm.getRawValue();

    // Validar teléfono de 8 dígitos (FA02)
    if (!/^\d{8}$/.test(formValues.telefono)) {
      this.mensajeError = 'El número de teléfono debe constar de 8 dígitos.';
      this.cdr.detectChanges();
      return;
    }

    // Validar cambio opcional de contraseña (FA03, FA04)
    if (formValues.nuevaPassword && formValues.nuevaPassword.trim() !== '') {
      if (!formValues.passwordActual || formValues.passwordActual.trim() === '') {
        this.mensajeError = 'Debe ingresar su contraseña actual para realizar el cambio.';
        this.cdr.detectChanges();
        return;
      }

      if (formValues.nuevaPassword !== formValues.confirmarNuevaPassword) {
        this.mensajeError = 'La nueva contraseña y su confirmación no coinciden.';
        this.cdr.detectChanges();
        return;
      }

      const regexPass = /^(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&.#_-])[A-Za-z\d@$!%*?&.#_-]{6,}$/;
      if (!regexPass.test(formValues.nuevaPassword)) {
        this.mensajeError = 'La nueva contraseña no cumple con los requisitos de seguridad.';
        this.cdr.detectChanges();
        return;
      }
    }

    this.guardando = true;
    this.cdr.detectChanges();
    const dto = {
      telefono: formValues.telefono,
      direccion: formValues.direccion,
      correo: formValues.correo,
      passwordActual: formValues.passwordActual || undefined,
      nuevaPassword: formValues.nuevaPassword || undefined,
      confirmarNuevaPassword: formValues.confirmarNuevaPassword || undefined,
    };

    this.usuarioService.actualizarPerfil(dto).subscribe({
      next: (res) => {
        this.guardando = false;
        this.mensajeExito = res.mensaje || 'Sus datos han sido actualizados con éxito.';
        this.perfilForm.patchValue({
          passwordActual: '',
          nuevaPassword: '',
          confirmarNuevaPassword: '',
        });
        this.mostrarSeccionPassword = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.guardando = false;
        this.mensajeError = err.error?.error || err.error?.mensaje || 'Ocurrió un error al actualizar el perfil.';
        this.cdr.detectChanges();
      },
    });
  }

  cancelar(): void {
    this.router.navigate(['/ciudadano/mis-quejas']);
  }

  toggleSeccionPassword(): void {
    this.mostrarSeccionPassword = !this.mostrarSeccionPassword;
    if (!this.mostrarSeccionPassword) {
      this.perfilForm.patchValue({
        passwordActual: '',
        nuevaPassword: '',
        confirmarNuevaPassword: '',
      });
    }
    this.cdr.detectChanges();
  }

  toggleVerPassword(campo: 'actual' | 'nueva' | 'confirmar'): void {
    if (campo === 'actual') this.verPasswordActual = !this.verPasswordActual;
    if (campo === 'nueva') this.verNuevaPassword = !this.verNuevaPassword;
    if (campo === 'confirmar') this.verConfirmarPassword = !this.verConfirmarPassword;
    this.cdr.detectChanges();
  }

  get nombreCompleto(): string {
    const raw = this.perfilForm?.getRawValue();
    const nombres = raw?.nombres || '';
    const apellidos = raw?.apellidos || '';
    const full = `${nombres} ${apellidos}`.trim();
    return full || 'Ciudadano Municipal';
  }

  get iniciales(): string {
    const raw = this.perfilForm?.getRawValue();
    const n = (raw?.nombres || '').trim();
    const a = (raw?.apellidos || '').trim();
    const iniN = n ? n[0].toUpperCase() : 'C';
    const iniA = a ? a[0].toUpperCase() : 'U';
    return `${iniN}${iniA}`;
  }

  get dpiFormateado(): string {
    const raw = this.perfilForm?.getRawValue();
    const dpi = raw?.dpi || '';
    if (dpi.length === 13) {
      return `${dpi.substring(0, 4)} ${dpi.substring(4, 9)} ${dpi.substring(9, 13)}`;
    }
    return dpi || 'No registrado';
  }
}
