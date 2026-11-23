import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, map } from 'rxjs';
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
    return forkJoin({
      empresas: this.http.get<Array<{ id: number; nombre: string; activo: boolean }>>('/api/empresas/mias'),
      parqueaderos: this.http.get<Array<{ id: number; nombre: string; activo: boolean; capacidadVehiculos: number | null }>>('/api/parqueaderos/mios'),
      espacios: this.http.get<Array<{ parqueaderoId: number | null; estado: string }>>('/api/espacios')
    }).pipe(map(({ empresas, parqueaderos, espacios }) => ({
      empresa: empresas[0]?.nombre || 'Mi empresa',
      parqueaderosActivos: parqueaderos.filter(p => p.activo).length,
      espaciosTotales: espacios.length,
      espaciosOcupados: espacios.filter(e => e.estado?.toUpperCase() === 'OCUPADO').length,
      ingresosHoy: 0,
      recaudoHoy: 0
    })));
  }

  getParqueaderos(): Observable<ParqueaderoOperativo[]> {
    return forkJoin({
      parqueaderos: this.http.get<Array<{ id: number; nombre: string; identificador: string; capacidadVehiculos: number | null; activo: boolean }>>('/api/parqueaderos/mios'),
      espacios: this.http.get<Array<{ parqueaderoId: number | null; estado: string }>>('/api/espacios')
    }).pipe(map(({ parqueaderos, espacios }) => parqueaderos.map(p => ({
      id: p.id,
      nombre: p.nombre,
      identificador: p.identificador,
      ocupados: espacios.filter(e => e.parqueaderoId === p.id && e.estado?.toUpperCase() === 'OCUPADO').length,
      capacidad: p.capacidadVehiculos ?? espacios.filter(e => e.parqueaderoId === p.id).length,
      activo: p.activo
    }))));
  }

  getReportes(): Observable<ReporteOperativo[]> {
    return this.http.get<Array<{ tipo: string; cantidad: number; total: number | null; descripcion: string }>>('/api/reportes/general')
      .pipe(map(items => items.map(item => ({
        periodo: item.descripcion,
        ingresos: item.cantidad,
        recaudo: item.total ?? 0,
        ocupacionPromedio: 0
      }))));
  }

  getConfiguracion(): Observable<ConfiguracionEmpresa> {
    return this.http.get<Array<{ nombre: string }>>('/api/empresas/mias').pipe(map(empresas => ({
      razonSocial: empresas[0]?.nombre || '',
      correoContacto: '',
      zonaHoraria: 'America/Bogota'
    })));
  }

  getPagosPendientes(): Observable<PagoOperativo[]> {
    return this.http.get<Array<{ id: number; ingresoId: number; valor: number | null; fechaEmision: string | null; estado: string }>>('/api/facturas')
      .pipe(map(facturas => facturas
        .filter(factura => factura.estado?.toUpperCase() === 'PENDIENTE')
        .map(factura => ({
          id: factura.id,
          referencia: `FACTURA-${factura.id}`,
          monto: factura.valor ?? 0,
          fecha: factura.fechaEmision ?? '',
          estado: 'PENDIENTE'
        }))));
  }

  getHistorialPagos(): Observable<PagoOperativo[]> {
    return this.http.get<Array<{ id: number; ingresoId: number; valor: number | null; fechaPago: string | null; estado: string }>>('/api/facturas')
      .pipe(map(facturas => facturas
        .filter(factura => factura.estado?.toUpperCase() === 'PAGADO' || factura.estado?.toUpperCase() === 'CONFIRMADO')
        .map(factura => ({
          id: factura.id,
          referencia: `FACTURA-${factura.id}`,
          monto: factura.valor ?? 0,
          fecha: factura.fechaPago ?? '',
          estado: 'CONFIRMADO'
        }))));
  }

  confirmarPago(id: number): Observable<PagoOperativo> {
    return this.http.post<{ id: number; valor: number | null; fechaPago: string | null }>(`/api/facturas/${id}/pagar`, {})
      .pipe(map(factura => ({
        id: factura.id,
        referencia: `FACTURA-${factura.id}`,
        monto: factura.valor ?? 0,
        fecha: factura.fechaPago ?? '',
        estado: 'CONFIRMADO'
      })));
  }

  getIngresosActivos(): Observable<IngresoOperativo[]> {
    return this.http.get<Array<{ id: number; placa: string; espacioNumero?: string; fechaEntrada: string; estado: string }>>('/api/ingresos/activos')
      .pipe(map(items => items.map(item => ({
        id: item.id,
        placa: item.placa,
        espacio: item.espacioNumero,
        fechaIngreso: item.fechaEntrada,
        estado: item.estado?.toUpperCase() === 'RESERVADO' ? 'ESPERANDO' : 'ACTIVO'
      }))));
  }

  actualizarIngreso(id: number, accion: AccionIngreso): Observable<IngresoOperativo> {
    const endpoint = accion === 'VALIDAR_ENTRADA'
      ? `/api/ingresos/${id}/llegada`
      : `/api/ingresos/${id}/salida`;
    return this.http.put<{
      id: number;
      placa: string;
      espacioNumero?: string;
      fechaEntrada: string;
      estado: string;
    }>(endpoint, {}).pipe(map(item => ({
      id: item.id,
      placa: item.placa,
      espacio: item.espacioNumero,
      fechaIngreso: item.fechaEntrada,
      estado: item.estado?.toUpperCase() === 'RESERVADO' ? 'ESPERANDO' : 'ACTIVO'
    })));
  }

  getOcupacion(): Observable<OcupacionOperativa> {
    return this.http.get<Array<{ parqueaderoId?: number; estado: string }>>('/api/espacios')
      .pipe(map(espacios => {
        const capacidad = espacios.length;
        const ocupados = espacios.filter(e => e.estado?.toUpperCase() === 'OCUPADO').length;
        const reservados = espacios.filter(e => e.estado?.toUpperCase() === 'RESERVADO').length;
        return {
          parqueadero: 'Mi parqueadero',
          capacidad,
          ocupados,
          reservados,
          disponibles: Math.max(0, capacidad - ocupados - reservados)
        };
      }));
  }

  registrarNovedad(request: NovedadRequest): Observable<NovedadResponse> {
    return this.http.put<{
      id: number;
      novedad?: string;
      fechaEntrada: string;
    }>(`/api/ingresos/${request.ingresoId}/verificar`, {
      novedad: request.descripcion
    }).pipe(map(item => ({
      id: item.id,
      ingresoId: request.ingresoId,
      descripcion: item.novedad || request.descripcion,
      fecha: item.fechaEntrada
    })));
  }

  getNovedades(): Observable<NovedadResponse[]> {
    return this.http.get<Array<{
      id: number;
      novedad?: string;
      fechaEntrada: string;
    }>>('/api/ingresos/activos').pipe(map(items =>
      items.filter(item => !!item.novedad).map(item => ({
        id: item.id,
        ingresoId: item.id,
        descripcion: item.novedad || '',
        fecha: item.fechaEntrada
      }))
    ));
  }
}
