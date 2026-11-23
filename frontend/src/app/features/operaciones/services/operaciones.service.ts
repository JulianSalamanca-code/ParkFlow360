import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  AccionIngreso,
  ConfiguracionEmpresa,
  EmpresaResumen,
  IngresoOperativo,
  NovedadRequest,
  NovedadResponse,
  OcupacionOperativa,
  PagoOperativo,
  ParqueaderoOperativo,
  ReporteOperativo
} from '../models/operaciones.model';

@Injectable({ providedIn: 'root' })
export class OperacionesService {
  constructor(private http: HttpClient) {}

  getEmpresaResumen(): Observable<EmpresaResumen> {
    return this.http.get<EmpresaResumen>('/api/empresa/resumen');
  }

  getParqueaderos(): Observable<ParqueaderoOperativo[]> {
    return this.http.get<ParqueaderoOperativo[]>('/api/empresa/parqueaderos');
  }

  getReportes(): Observable<ReporteOperativo[]> {
    return this.http.get<ReporteOperativo[]>('/api/empresa/reportes');
  }

  getConfiguracion(): Observable<ConfiguracionEmpresa> {
    return this.http.get<ConfiguracionEmpresa>('/api/empresa/configuracion');
  }

  getPagosPendientes(): Observable<PagoOperativo[]> {
    return this.http.get<PagoOperativo[]>('/api/cajero/pagos?estado=PENDIENTE');
  }

  getHistorialPagos(): Observable<PagoOperativo[]> {
    return this.http.get<PagoOperativo[]>('/api/cajero/pagos?estado=CONFIRMADO');
  }

  confirmarPago(id: number): Observable<PagoOperativo> {
    return this.http.post<PagoOperativo>(`/api/cajero/pagos/${id}/confirmar`, {});
  }

  getIngresosActivos(): Observable<IngresoOperativo[]> {
    return this.http.get<IngresoOperativo[]>('/api/guardia/ingresos?estado=ACTIVOS');
  }

  actualizarIngreso(id: number, accion: AccionIngreso): Observable<IngresoOperativo> {
    return this.http.patch<IngresoOperativo>(`/api/guardia/ingresos/${id}`, { accion });
  }

  getOcupacion(): Observable<OcupacionOperativa> {
    return this.http.get<OcupacionOperativa>('/api/guardia/ocupacion');
  }

  registrarNovedad(request: NovedadRequest): Observable<NovedadResponse> {
    return this.http.post<NovedadResponse>('/api/guardia/novedades', request);
  }

  getNovedades(): Observable<NovedadResponse[]> {
    return this.http.get<NovedadResponse[]>('/api/guardia/novedades');
  }
}
