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
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../../modules/auth/auth.service';

@Component({
  selector: 'app-registro',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './registro.html',
  styleUrl: './registro.css',
})
export class RegistroComponent implements OnInit {
  registroForm!: FormGroup;
  errorMsg: string = '';
  successMsg: string = '';
  formEnviado: boolean = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.registroForm = this.fb.group(
      {
        dpi: ['', [Validators.required, Validators.pattern('^[0-9]{13}$')]],
        nombres: ['', Validators.required],
        apellidos: ['', Validators.required],
        telefono: ['', [Validators.required, Validators.pattern('^[0-9]{8}$')]],
        direccion: ['', Validators.required],
        correo: ['', [Validators.required, Validators.email]],
        password: [
          '',
          [
            Validators.required,
            Validators.minLength(6),
            Validators.pattern(
              '^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.#_-])[A-Za-z\\d@$!%*?&.#_-]{6,}$',
            ),
          ],
        ],
        confirmPassword: ['', Validators.required],
      },
      {
        validators: this.validarQueCoincidanContrasenas,
      },
    );
  }

  validarQueCoincidanContrasenas(control: AbstractControl): ValidationErrors | null {
    const pass = control.get('password')?.value;
    const confirmPass = control.get('confirmPassword')?.value;

    if (pass && confirmPass && pass !== confirmPass) {
      control.get('confirmPassword')?.setErrors({ noCoincide: true });
      return { noCoincide: true };
    }
    return null;
  }

  onRegister(): void {
    this.formEnviado = true;
    this.errorMsg = '';
    this.successMsg = '';

    if (this.registroForm.invalid) {
      if (
        this.registroForm.errors?.['noCoincide'] ||
        this.registroForm.get('confirmPassword')?.hasError('noCoincide')
      ) {
        this.errorMsg = 'Las contraseñas ingresadas no coinciden.';
      } else if (this.registroForm.get('telefono')?.hasError('pattern')) {
        this.errorMsg = 'El número telefónico debe contener exactamente 8 dígitos numéricos.';
      } else if (this.registroForm.get('password')?.invalid) {
        this.errorMsg =
          'La contraseña debe contener al menos 6 caracteres, una letra mayúscula, un número y un carácter especial.';
      } else {
        this.errorMsg = 'Debe completar todos los campos obligatorios.';
      }
      return;
    }

    const datosUsuario = this.registroForm.value;

    this.authService.registrarCiudadano(datosUsuario).subscribe({
      next: () => {
        this.successMsg = 'Su cuenta ha sido creada exitosamente. Ya puede iniciar sesión.';
        setTimeout(() => {
          this.router.navigate(['/auth/login']);
        }, 2500);
      },
      error: (err: any) => {
        let mensajeBackend = '';

        if (typeof err.error === 'string') {
          mensajeBackend = err.error;
        } else if (err.error?.mensaje || err.error?.message) {
          mensajeBackend = err.error.mensaje || err.error.message;
        } else if (err.error?.errors && Array.isArray(err.error.errors)) {
          mensajeBackend = err.error.errors.map((e: any) => e.defaultMessage || e).join(', ');
        }

        const msgLower = mensajeBackend.toLowerCase();

        if (msgLower.includes('dpi')) {
          this.errorMsg = 'El número de DPI ingresado ya se encuentra registrado en el sistema.';
        } else if (msgLower.includes('correo') || msgLower.includes('email')) {
          this.errorMsg = 'El correo electrónico ingresado ya está asociado a otra cuenta.';
        } else {
          this.errorMsg = mensajeBackend || 'Ocurrió un error al registrar la cuenta.';
        }
      },
    });
  }

  onCancelar(): void {
    this.registroForm.reset();
    this.router.navigate(['/auth/login']);
  }

  esCampoInvalido(campo: string): boolean {
    const c = this.registroForm.get(campo);
    return !!(c && c.invalid && (c.touched || this.formEnviado));
  }
}
