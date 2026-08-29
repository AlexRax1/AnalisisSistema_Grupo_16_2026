import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RegistroQuejaPayload, RespuestaRegistroQueja } from '../models/categoria-queja.model';

@Injectable({
  providedIn: 'root'
})
export class QuejaService {
  private apiUrl = '/quejas/registrar';

  constructor(private http: HttpClient) {}

  registrarQueja(datos: RegistroQuejaPayload, fotos: File[]): Observable<RespuestaRegistroQueja> {
    const formData = new FormData();

    // El backend espera 'datos' como blob json o string json
    formData.append(
      'datos',
      new Blob([JSON.stringify(datos)], { type: 'application/json' })
    );

    // Adjuntar archivos de fotos bajo la clave 'fotos'
    fotos.forEach((foto) => {
      formData.append('fotos', foto, foto.name);
    });

    return this.http.post<RespuestaRegistroQueja>(this.apiUrl, formData);
  }
}
