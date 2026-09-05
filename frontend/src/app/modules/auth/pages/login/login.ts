import { Component, OnInit } from '@angular/core';
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
import { AuthService, Usuario } from '../../../../modules/auth/auth.service';

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
  ) {}

  ngOnInit(): void {
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
        this.errorMsg = 'Correo electrónico o contraseña incorrectos.';
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
  }

  cerrarRecuperacion(): void {
    this.mostrarModalRecuperacion = false;
    this.errorMsg = '';
  }

  // Paso 1: Presionar "Enviar Código" pasa automáticamente a pedir el código
  onEnviarCodigo(): void {
    const correoControl = this.recuperarForm.get('correoRecuperacion');

    if (correoControl?.invalid) {
      this.errorMsg = 'Debe ingresar los campos obligatorios.';
      return;
    }

    this.errorMsg = '';
    this.pasoRecuperacion = 'INGRESAR_CODIGO';

    this.recuperarForm.get('codigo')?.setValidators([Validators.required]);
    this.recuperarForm.get('codigo')?.updateValueAndValidity();
  }

  // Paso 2: Validar que el código ingresado sea estrictamente 1234
  onValidarCodigo(): void {
    const codigoInput = this.recuperarForm.get('codigo')?.value;

    if (!codigoInput) {
      this.errorMsg = 'Debe ingresar los campos obligatorios.';
      return;
    }

    // Validar código quemado '1234'
    if (codigoInput.trim() !== '1234') {
      this.errorMsg = 'El código ingresado es inválido o ha expirado.';
      return;
    }

    this.errorMsg = '';
    this.pasoRecuperacion = 'NUEVA_CONTRASEÑA';

    this.recuperarForm
      .get('nuevaPassword')
      ?.setValidators([
        Validators.required,
        Validators.pattern('^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.#_-])[A-Za-z\\d@$!%*?&.#_-]{6,}$'),
      ]);
    this.recuperarForm.get('confirmarPassword')?.setValidators([Validators.required]);
    this.recuperarForm.get('nuevaPassword')?.updateValueAndValidity();
    this.recuperarForm.get('confirmarPassword')?.updateValueAndValidity();
  }

  // Paso 3: Consumir PUT /auth/reset-password enviando userId: 1
  onRestablecerPassword(): void {
    const nuevaPassControl = this.recuperarForm.get('nuevaPassword');

    if (nuevaPassControl?.hasError('pattern')) {
      this.errorMsg =
        'La contraseña debe incluir al menos una letra mayúscula, un número y un carácter especial.';
      return;
    }

    if (this.recuperarForm.hasError('noCoincide')) {
      this.errorMsg = 'Las contraseñas ingresadas no coinciden.';
      return;
    }

    if (this.recuperarForm.invalid) {
      this.errorMsg = 'Debe ingresar los campos obligatorios.';
      return;
    }

    const userIdQuemado = 1;
    const newPassword = nuevaPassControl?.value;

    this.authService.restablecerPassword(userIdQuemado, newPassword).subscribe({
      next: () => {
        this.successMsg = 'Su contraseña ha sido actualizada con éxito.';
        setTimeout(() => {
          this.cerrarRecuperacion();
        }, 2000);
      },
      error: (err: any) => {
        this.errorMsg =
          err.error?.mensaje || err.error?.message || 'Error al actualizar la contraseña.';
      },
    });
  }
}
