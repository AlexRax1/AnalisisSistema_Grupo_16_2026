import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-inicio-inspector',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './inicio-inspector.html',
  styleUrl: './inicio-inspector.css',
})
export class InicioInspectorComponent {
  constructor(private router: Router) {}

  irAInspecciones(): void {
    this.router.navigate(['/inspector/inspecciones']);
  }
}
