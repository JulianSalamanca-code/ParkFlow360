import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { GenerarEspaciosRequest, Plano, PlanoRequest } from '../models/plano.model';
import { Espacio } from '../../espacios/models/espacio.model';

@Injectable({
  providedIn: 'root'
})
export class PlanoService {
  private apiUrl = '/api/planos';

  constructor(private http: HttpClient) {}

  listarTodos(): Observable<Plano[]> {
    return this.http.get<Plano[]>(this.apiUrl);
  }

  obtenerPorId(id: number): Observable<Plano> {
    return this.http.get<Plano>(`${this.apiUrl}/${id}`);
  }

  crear(plano: PlanoRequest): Observable<Plano> {
    return this.http.post<Plano>(this.apiUrl, plano);
  }

  actualizar(id: number, plano: PlanoRequest): Observable<Plano> {
    return this.http.put<Plano>(`${this.apiUrl}/${id}`, plano);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /** Genera en bloque los espacios del plano (cuadrícula). */
  generarEspacios(id: number, request: GenerarEspaciosRequest): Observable<Espacio[]> {
    return this.http.post<Espacio[]>(`${this.apiUrl}/${id}/generar-espacios`, request);
  }

  /** Espacios ubicados en el plano (para el mapa de ocupación). */
  listarEspacios(id: number): Observable<Espacio[]> {
    return this.http.get<Espacio[]>(`${this.apiUrl}/${id}/espacios`);
  }
}
