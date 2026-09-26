import { Component, OnInit, NgZone } from '@angular/core';
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
  mensajeExito: string = '';
  mensajeError: string = '';

  constructor(
    private fb: FormBuilder,
    private usuarioService: UsuarioService,
    private router: Router,
    private ngZone: NgZone,
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.cargarPerfil();
  }

  private initForm(): void {
    this.perfilForm = this.fb.group({
      // Campos inmutables (RN06)
      dpi: [{ value: '', disabled: true }],
      nombres: [{ value: '', disabled: true }],
      apellidos: [{ value: '', disabled: true }],

      // Campos editables obligatorios (RN01, RN03, RN04)
      telefono: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
      direccion: ['', [Validators.required]],
      correo: ['', [Validators.required, Validators.email]],

      // Sección opcional: Cambio de contraseña (RN05)
      passwordActual: [''],
      nuevaPassword: [''],
      confirmarNuevaPassword: [''],
    });
  }

  cargarPerfil(): void {
    //this.cargando = true;

    this.usuarioService.obtenerPerfil().subscribe({
      next: (data: PerfilUsuarioDTO) => {
        // 3. Forzar a que la actualización ocurra dentro del hilo UI de Angular
        this.ngZone.run(() => {
          if (data) {
            this.perfilForm.patchValue({
              dpi: data.dpi,
              nombres: data.nombres,
              apellidos: data.apellidos,
              telefono: data.telefono,
              direccion: data.direccion,
              correo: data.correo,
            });
          }
          this.cargando = false; // Se quita el "Cargando datos..." al instante
        });
      },
      error: (err) => {
        this.ngZone.run(() => {
          this.mensajeError = 'Error al cargar los datos del perfil.';
          this.cargando = false;
        });
      },
    });
  }

  guardarCambios(): void {
    this.mensajeExito = '';
    this.mensajeError = '';

    if (this.perfilForm.invalid) {
      this.mensajeError = 'Debe ingresar los campos obligatorios correctamente.';
      return;
    }

    const formValues = this.perfilForm.getRawValue();

    // Validar teléfono de 8 dígitos (FA02)[cite: 5]
    if (!/^\d{8}$/.test(formValues.telefono)) {
      this.mensajeError = 'El número de teléfono debe constar de 8 dígitos.';
      return;
    }

    // Validar cambio opcional de contraseña (FA03, FA04)[cite: 5]
    if (formValues.nuevaPassword && formValues.nuevaPassword.trim() !== '') {
      if (!formValues.passwordActual || formValues.passwordActual.trim() === '') {
        this.mensajeError = 'Debe ingresar su contraseña actual para realizar el cambio.';
        return;
      }

      if (formValues.nuevaPassword !== formValues.confirmarNuevaPassword) {
        this.mensajeError = 'La nueva contraseña y su confirmación no coinciden.';
        return;
      }

      const regexPass = /^(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&.#_-])[A-Za-z\d@$!%*?&.#_-]{6,}$/;
      if (!regexPass.test(formValues.nuevaPassword)) {
        this.mensajeError = 'La nueva contraseña no cumple con los requisitos de seguridad.';
        return;
      }
    }

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
        this.mensajeExito = res.mensaje || 'Sus datos han sido actualizados con éxito.';
        this.perfilForm.patchValue({
          passwordActual: '',
          nuevaPassword: '',
          confirmarNuevaPassword: '',
        });
      },
      error: (err) => {
        this.mensajeError = err.error?.error || 'Ocurrió un error al actualizar el perfil.';
      },
    });
  }

  cancelar(): void {
    this.router.navigate(['/ciudadano/dashboard']);
  }
}
