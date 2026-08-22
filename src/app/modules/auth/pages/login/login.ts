import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService, Usuario } from '../../../../modules/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent {
  correo: string = 'ciudadano@muni.gt';
  password: string = '123456';
  errorMsg: string = '';

  constructor(private authService: AuthService, private router: Router) {}

  onLogin() {
    this.authService.login(this.correo, this.password).subscribe({
      next: (user: Usuario) => {
        this.router.navigate(['/portal-ciudadano']);
      },
      error: (err: Error) => {
        this.errorMsg = err.message;
      }
    });
  }
}
