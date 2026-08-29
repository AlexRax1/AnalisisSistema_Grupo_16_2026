import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { QuejaMockService } from '../../../../core/services/queja-mock.service';
import { Queja } from '../../../../core/models/queja.model';

@Component({
  selector: 'app-crear-queja',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './crear-queja.html',
  styleUrl: './crear-queja.css'
})
export class CrearQuejaComponent {
  nuevaQueja: Partial<Queja> = {
    titulo: '',
    descripcion: '',
    zona: 1,
    direccion: '',
    categoria: 'Vías e Infraestructura',
    dpiCiudadano: '2540123450101'
  };

  // Simulación de carga de archivos
  fotoSimuladaUrl: string = '';

  categorias: string[] = [
    'Vías e Infraestructura',
    'Agua Potable y Alcantarillado',
    'Alumbrado Público',
    'Limpieza y Desechos',
    'Parques y Áreas Verdes'
  ];

  zonas: number[] = Array.from({ length: 25 }, (_, i) => i + 1);

  constructor(private quejaService: QuejaMockService, private router: Router) {}

  simularCargaFoto(event: any) {
    const file = event.target.files[0];
    if (file) {
      // Simula la previsualización/carga guardando un placeholder visual
      this.fotoSimuladaUrl = `https://placehold.co/600x400?text=Evidencia+${file.name}`;
    }
  }

  onSubmit() {
    if (this.fotoSimuladaUrl) {
      this.nuevaQueja.fotosAntes = [this.fotoSimuladaUrl];
    } else {
      this.nuevaQueja.fotosAntes = ['https://placehold.co/600x400?text=Foto+Sin+Archivo'];
    }

    this.quejaService.crearQueja(this.nuevaQueja).subscribe({
      next: (quejaCreada: Queja) => {
        alert(`¡Queja registrada con éxito!\nCorrelativo generado: ${quejaCreada.correlativo}`);
        this.router.navigate(['/portal-ciudadano']);
      }
    });
  }
}
