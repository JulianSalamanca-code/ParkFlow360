import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Pago, PagoRequest } from '../models/pago.model';

@Injectable({
  providedIn: 'root'
})
export class PagoService {
  private apiUrl = '/api/pagos';

  constructor(private http: HttpClient) {}

  listarTodos(): Observable<Pago[]> {
    return this.http.get<Pago[]>(this.apiUrl);
  }

  listarPorVehiculo(vehiculoId: number): Observable<Pago[]> {
    return this.http.get<Pago[]>(`${this.apiUrl}/vehiculo/${vehiculoId}`);
  }

  listarPorEspacio(espacioId: number): Observable<Pago[]> {
    return this.http.get<Pago[]>(`${this.apiUrl}/espacio/${espacioId}`);
  }

  obtenerPorId(id: number): Observable<Pago> {
    return this.http.get<Pago>(`${this.apiUrl}/${id}`);
  }

  crear(pago: PagoRequest): Observable<Pago> {
    return this.http.post<Pago>(this.apiUrl, pago);
  }

  actualizar(id: number, pago: PagoRequest): Observable<Pago> {
    return this.http.put<Pago>(`${this.apiUrl}/${id}`, pago);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
