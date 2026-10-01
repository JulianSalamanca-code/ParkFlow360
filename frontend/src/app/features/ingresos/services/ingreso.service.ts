import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Ingreso, IngresoRequest } from '../models/ingreso.model';

@Injectable({
  providedIn: 'root'
})
export class IngresoService {
  private apiUrl = '/api/ingresos';

  constructor(private http: HttpClient) {}

  listarTodos(): Observable<Ingreso[]> {
    return this.http.get<Ingreso[]>(this.apiUrl);
  }

  listarActivos(): Observable<Ingreso[]> {
    return this.http.get<Ingreso[]>(`${this.apiUrl}/activos`);
  }

  obtenerPorId(id: number): Observable<Ingreso> {
    return this.http.get<Ingreso>(`${this.apiUrl}/${id}`);
  }

  obtenerPorFolio(folio: string): Observable<Ingreso> {
    return this.http.get<Ingreso>(`${this.apiUrl}/folio/${folio}`);
  }

  crear(ingreso: IngresoRequest): Observable<Ingreso> {
    return this.http.post<Ingreso>(this.apiUrl, ingreso);
  }

  registrarSalida(id: number): Observable<Ingreso> {
    return this.http.put<Ingreso>(`${this.apiUrl}/${id}/salida`, {});
  }

  liberar(id: number): Observable<Ingreso> {
    return this.http.put<Ingreso>(`${this.apiUrl}/${id}/liberar`, {});
  }

  liberarPorEspacio(espacioId: number): Observable<Ingreso> {
    return this.http.put<Ingreso>(`${this.apiUrl}/espacio/${espacioId}/liberar`, {});
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Cliente
  misIngresos(): Observable<Ingreso[]> {
    return this.http.get<Ingreso[]>(`${this.apiUrl}/mios`);
  }

  reservar(ingreso: IngresoRequest): Observable<Ingreso> {
    return this.http.post<Ingreso>(`${this.apiUrl}/reservar`, ingreso);
  }
}
