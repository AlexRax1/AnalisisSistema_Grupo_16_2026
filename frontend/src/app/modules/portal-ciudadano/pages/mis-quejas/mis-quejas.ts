import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { QuejaMockService } from '../../../../core/services/queja-mock.service';
import { Queja } from '../../../../core/models/queja.model';

@Component({
  selector: 'app-mis-quejas',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './mis-quejas.html',
  styleUrl: './mis-quejas.css'
})
export class MisQuejasComponent implements OnInit {
  quejas: Queja[] = [];
  quejaSeleccionada?: Queja;

  constructor(private quejaService: QuejaMockService) {}

  ngOnInit(): void {
    this.quejaService.obtenerTodas().subscribe((data: Queja[]) => {
      this.quejas = data;
      if (data.length > 0) this.quejaSeleccionada = data[0];
    });
  }

  verDetalle(queja: Queja) {
    this.quejaSeleccionada = queja;
  }
}
