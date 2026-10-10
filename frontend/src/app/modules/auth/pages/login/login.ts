import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
  AbstractControl,
  ValidationErrors,
} from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService, Usuario } from '../../auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  recuperarForm!: FormGroup;

  errorMsg: string = '';
  successMsg: string = '';
  formEnviado: boolean = false;

  // Estado del modal de recuperación (FA02)
  mostrarModalRecuperacion: boolean = false;
  pasoRecuperacion: 'SOLICITAR_CODIGO' | 'INGRESAR_CODIGO' | 'NUEVA_CONTRASEÑA' =
    'SOLICITAR_CODIGO';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    // Limpiar tokens residuales previos al ingresar a la pantalla de login
    localStorage.removeItem('token');
    localStorage.removeItem('rol');

    if (this.route.snapshot.queryParams['registrado'] === 'true') {
      this.successMsg = '¡Su cuenta ha sido creada exitosamente! Ya puede iniciar sesión.';
    }

    // Formulario de Inicio de Sesión
    this.loginForm = this.fb.group({
      correo: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
    });

    // Formulario de Recuperación de Contraseña
    this.recuperarForm = this.fb.group(
      {
        correoRecuperacion: ['', [Validators.required, Validators.email]],
        codigo: [''],
        nuevaPassword: [
          '',
          [
            Validators.pattern(
              '^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.#_-])[A-Za-z\\d@$!%*?&.#_-]{6,}$',
            ),
          ],
        ],
        confirmarPassword: [''],
      },
      {
        validators: this.validarQueCoincidanContrasenas,
      },
    );
  }

  // Validador personalizado para FA07: Contraseñas no coinciden
  validarQueCoincidanContrasenas(control: AbstractControl): ValidationErrors | null {
    const pass = control.get('nuevaPassword')?.value;
    const confirmPass = control.get('confirmarPassword')?.value;

    if (pass && confirmPass && pass !== confirmPass) {
      control.get('confirmarPassword')?.setErrors({ noCoincide: true });
      return { noCoincide: true };
    }
    return null;
  }

  // --- FLUJO NORMAL: INICIAR SESIÓN ---
  onLogin(): void {
    this.formEnviado = true;
    this.errorMsg = '';

    if (this.loginForm.invalid) {
      this.errorMsg = 'Debe ingresar los campos obligatorios.';
      return;
    }

    const { correo, password } = this.loginForm.value;

    this.authService.login(correo, password).subscribe({
      next: (res: Usuario) => {
        if (res.token) localStorage.setItem('token', res.token);
        if (res.rol) localStorage.setItem('rol', res.rol);
        this.redirigirSegunRol(res.rol);
      },
      error: () => {
        localStorage.removeItem('token');
        localStorage.removeItem('rol');
        this.errorMsg = 'Correo electrónico o contraseña incorrectos.';
        this.cdr.detectChanges();
      },
    });
  }

  private redirigirSegunRol(rol?: string): void {
    switch (rol) {
      case 'CIUDADANO':
        this.router.navigate(['/ciudadano/mis-quejas']);
        break;
      case 'FUNCIONARIO':
        this.router.navigate(['/funcionario/bandeja']);
        break;
      case 'INSPECTOR':
        this.router.navigate(['/inspector/inspecciones']);
        break;
      case 'ESPECIALISTA':
        this.router.navigate(['/especialista/ordenes']);
        break;
      case 'ADMINISTRADOR':
        this.router.navigate(['/admin/usuarios']);
        break;
      default:
        this.router.navigate(['/ciudadano/mis-quejas']);
    }
  }

  // --- FA02: RECUPERACIÓN DE CONTRASEÑA ---

  abrirRecuperacion(): void {
    this.mostrarModalRecuperacion = true;
    this.pasoRecuperacion = 'SOLICITAR_CODIGO';
    this.recuperarForm.reset();
    this.errorMsg = '';
    this.successMsg = '';
    this.cdr.detectChanges();
  }

  cerrarRecuperacion(): void {
    this.mostrarModalRecuperacion = false;
    this.errorMsg = '';
    this.cdr.detectChanges();
  }

  // Paso 1: Solicitar el código y enviarlo por correo
  onEnviarCodigo(): void {
    const correoControl = this.recuperarForm.get('correoRecuperacion');

    if (correoControl?.invalid || !correoControl?.value) {
      this.errorMsg = 'Debe ingresar los campos obligatorios.';
      this.cdr.detectChanges();
      return;
    }

    this.errorMsg = '';
    const correo = correoControl.value;

    // Consumir API para enviar código real al correo ingresado
    this.authService.solicitarCodigoRecuperacion(correo).subscribe({
      next: () => {
        this.pasoRecuperacion = 'INGRESAR_CODIGO';
        this.recuperarForm.get('codigo')?.setValidators([Validators.required]);
        this.recuperarForm.get('codigo')?.updateValueAndValidity();
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        // Captura el mensaje si el correo no está registrado (FA05)
        this.errorMsg =
          typeof err.error === 'string'
            ? err.error
            : err.error?.mensaje ||
              err.error?.message ||
              'El correo ingresado no se encuentra registrado.';
        this.cdr.detectChanges();
      },
    });
  }

  // Paso 2: Validar el código de verificación recibido por correo
  onValidarCodigo(): void {
    const correo = this.recuperarForm.get('correoRecuperacion')?.value;
    const codigoInput = this.recuperarForm.get('codigo')?.value;

    if (!codigoInput) {
      this.errorMsg = 'Debe ingresar los campos obligatorios.';
      this.cdr.detectChanges();
      return;
    }

    this.errorMsg = '';

    // Consumir API para validar el código generado dinámicamente en backend
    this.authService.validarCodigo(correo, codigoInput.trim()).subscribe({
      next: () => {
        this.pasoRecuperacion = 'NUEVA_CONTRASEÑA';

        this.recuperarForm
          .get('nuevaPassword')
          ?.setValidators([
            Validators.required,
            Validators.pattern(
              '^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.#_-])[A-Za-z\\d@$!%*?&.#_-]{6,}$',
            ),
          ]);
        this.recuperarForm.get('confirmarPassword')?.setValidators([Validators.required]);
        this.recuperarForm.get('nuevaPassword')?.updateValueAndValidity();
        this.recuperarForm.get('confirmarPassword')?.updateValueAndValidity();
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        // Manejo de código inválido o expirado (FA06)
        this.errorMsg =
          typeof err.error === 'string'
            ? err.error
            : err.error?.mensaje ||
              err.error?.message ||
              'El código ingresado es inválido o ha expirado.';
        this.cdr.detectChanges();
      },
    });
  }

  // Paso 3: Consumir endpoint enviando el correo real del usuario
  onRestablecerPassword(): void {
    const nuevaPassControl = this.recuperarForm.get('nuevaPassword');

    if (nuevaPassControl?.hasError('pattern')) {
      this.errorMsg =
        'La contraseña debe incluir al menos una letra mayúscula, un número y un carácter especial.';
      this.cdr.detectChanges();
      return;
    }

    if (this.recuperarForm.hasError('noCoincide')) {
      this.errorMsg = 'Las contraseñas ingresadas no coinciden.';
      this.cdr.detectChanges();
      return;
    }

    if (this.recuperarForm.invalid) {
      this.errorMsg = 'Debe ingresar los campos obligatorios.';
      this.cdr.detectChanges();
      return;
    }

    const correo = this.recuperarForm.get('correoRecuperacion')?.value;
    const newPassword = nuevaPassControl?.value;

    // En lugar del ID quemado 1, enviamos el correo y la nueva contraseña
    this.authService.restablecerPassword(correo, newPassword).subscribe({
      next: () => {
        this.successMsg = 'Su contraseña ha sido actualizada con éxito.';
        this.cdr.detectChanges();
        setTimeout(() => {
          this.cerrarRecuperacion();
        }, 2000);
      },
      error: (err: any) => {
        this.errorMsg =
          err.error?.mensaje || err.error?.message || 'Error al actualizar la contraseña.';
        this.cdr.detectChanges();
      },
    });
  }
}
