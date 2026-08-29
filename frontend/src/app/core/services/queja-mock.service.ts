import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { Queja, EstadoQueja } from '../models/queja.model';

@Injectable({
  providedIn: 'root'
})
export class QuejaMockService {
  private quejasMock: Queja[] = [
    {
      id: '1',
      correlativo: 'QUE-2026-0001',
      titulo: 'Bache profundo en vía principal',
      descripcion: 'Existe un bache peligroso cerca del semáforo.',
      dpiCiudadano: '2540123450101',
      zona: 10,
      direccion: '12 Calle y 4ta Avenida',
      categoria: 'Vías e Infraestructura',
      estado: 'EN_INSPECCION',
      prioridad: 'ALTA',
      fechaCreacion: new Date('2026-08-15'),
      fotosAntes: ['https://placehold.co/400x300?text=Bache+Zona+10'],
      historialEstados: [
        { estado: 'REGISTRADA', fecha: new Date('2026-08-15'), comentario: 'Queja creada por el ciudadano', responsable: 'Ciudadano' },
        { estado: 'EN_INSPECCION', fecha: new Date('2026-08-16'), comentario: 'Asignado a inspector de campo', responsable: 'Muni Admin' }
      ]
    },
    {
      id: '2',
      correlativo: 'QUE-2026-0002',
      titulo: 'Fuga de agua potable',
      descripcion: 'Tubería rota derramando agua en la acera.',
      dpiCiudadano: '1890123450101',
      zona: 7,
      direccion: 'Calzada Roosevelt 15-20',
      categoria: 'Agua Potable y Alcantarillado',
      estado: 'REGISTRADA',
      prioridad: 'MEDIA',
      fechaCreacion: new Date('2026-08-20'),
      fotosAntes: ['https://placehold.co/400x300?text=Fuga+Zona+7'],
      historialEstados: [
        { estado: 'REGISTRADA', fecha: new Date('2026-08-20'), comentario: 'Queja registrada', responsable: 'Ciudadano' }
      ]
    }
  ];

  obtenerTodas(): Observable<Queja[]> {
    return of(this.quejasMock);
  }

  crearQueja(nuevaQueja: Partial<Queja>): Observable<Queja> {
    const correlativoNum = (this.quejasMock.length + 1).toString().padStart(4, '0');
    const quejaCompleta: Queja = {
      id: (this.quejasMock.length + 1).toString(),
      correlativo: `QUE-2026-${correlativoNum}`,
      titulo: nuevaQueja.titulo || 'Sin título',
      descripcion: nuevaQueja.descripcion || '',
      dpiCiudadano: nuevaQueja.dpiCiudadano || '2540123450101',
      zona: nuevaQueja.zona || 1,
      direccion: nuevaQueja.direccion || '',
      categoria: nuevaQueja.categoria || 'Vías e Infraestructura',
      estado: 'REGISTRADA',
      prioridad: 'MEDIA',
      fechaCreacion: new Date(),
      fotosAntes: nuevaQueja.fotosAntes || ['https://placehold.co/400x300?text=Evidencia'],
      historialEstados: [
        { estado: 'REGISTRADA', fecha: new Date(), comentario: 'Registro inicial generado', responsable: 'Ciudadano' }
      ]
    };
    this.quejasMock.push(quejaCompleta);
    return of(quejaCompleta);
  }

  actualizarEstado(id: string, nuevoEstado: EstadoQueja, comentario: string, responsable: string): Observable<boolean> {
    const q = this.quejasMock.find(item => item.id === id);
    if (q) {
      q.estado = nuevoEstado;
      q.historialEstados.push({ estado: nuevoEstado, fecha: new Date(), comentario, responsable });
      return of(true);
    }
    return of(false);
  }
}
