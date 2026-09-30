import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Reporte } from '../models/reporte.model';

@Injectable({
  providedIn: 'root'
})
export class ReporteService {
  private apiUrl = '/api/reportes';

  constructor(private http: HttpClient) {}

  reporteGeneral(): Observable<Reporte[]> {
    return this.http.get<Reporte[]>(`${this.apiUrl}/general`);
  }

  espaciosPorEstado(): Observable<Reporte[]> {
    return this.http.get<Reporte[]>(`${this.apiUrl}/espacios-por-estado`);
  }

  ingresosPorPeriodo(inicio: string, fin: string): Observable<Reporte[]> {
    return this.http.get<Reporte[]>(`${this.apiUrl}/ingresos-por-periodo?inicio=${inicio}&fin=${fin}`);
  }
}
