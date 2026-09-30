import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Tarifa, TarifaRequest } from '../models/tarifa.model';

@Injectable({
  providedIn: 'root'
})
export class TarifaService {
  private apiUrl = '/api/tarifas';

  constructor(private http: HttpClient) {}

  listarTodos(): Observable<Tarifa[]> {
    return this.http.get<Tarifa[]>(this.apiUrl);
  }

  listarPorTipo(tipo: string): Observable<Tarifa[]> {
    return this.http.get<Tarifa[]>(`${this.apiUrl}/tipo/${tipo}`);
  }

  obtenerPorId(id: number): Observable<Tarifa> {
    return this.http.get<Tarifa>(`${this.apiUrl}/${id}`);
  }

  crear(tarifa: TarifaRequest): Observable<Tarifa> {
    return this.http.post<Tarifa>(this.apiUrl, tarifa);
  }

  actualizar(id: number, tarifa: TarifaRequest): Observable<Tarifa> {
    return this.http.put<Tarifa>(`${this.apiUrl}/${id}`, tarifa);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
