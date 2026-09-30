import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Espacio, EspacioRequest } from '../models/espacio.model';

@Injectable({
  providedIn: 'root'
})
export class EspacioService {
  private apiUrl = '/api/espacios';

  constructor(private http: HttpClient) {}

  listarTodos(): Observable<Espacio[]> {
    return this.http.get<Espacio[]>(this.apiUrl);
  }

  listarPorEstado(estado: string): Observable<Espacio[]> {
    return this.http.get<Espacio[]>(`${this.apiUrl}/estado/${estado}`);
  }

  obtenerPorId(id: number): Observable<Espacio> {
    return this.http.get<Espacio>(`${this.apiUrl}/${id}`);
  }

  crear(espacio: EspacioRequest): Observable<Espacio> {
    return this.http.post<Espacio>(this.apiUrl, espacio);
  }

  actualizar(id: number, espacio: EspacioRequest): Observable<Espacio> {
    return this.http.put<Espacio>(`${this.apiUrl}/${id}`, espacio);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
