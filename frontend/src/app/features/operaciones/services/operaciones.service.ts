import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, forkJoin, map, of, switchMap } from 'rxjs';
import {
  AccionIngreso,
  ConfiguracionEmpresa,
  EmpresaApi,
  EmpresaCrearRequest,
  EmpresaResumen,
  EstadoPagoOperativoResponse,
  IngresoOperativo,
  NovedadRequest,
  NovedadResponse,
  OcupacionOperativa,
  PagoOperativo,
  ParqueaderoCreadoResponse,
  ParqueaderoCrearRequest,
  ParqueaderoOperativo,
  ReporteOperativo
} from '../models/operaciones.model';

interface FacturaApi {
  readonly id: number;
  readonly ingresoId: number;
  readonly valor: number | null;
  readonly valorActual?: number | null;
  readonly faltante?: number | null;
  readonly estado: string;
  readonly estadoIngreso?: string;
  readonly fechaEmision?: string | null;
}

interface IngresoApi {
  readonly id: number;
  readonly placa: string;
  readonly espacioNumero?: string;
  readonly fechaEntrada: string;
  readonly estado: string;
  readonly novedad?: string;
}

@Injectable({ providedIn: 'root' })
export class OperacionesService {
  constructor(private http: HttpClient) {}

  getEmpresaResumen(): Observable<EmpresaResumen> {
    return forkJoin({
      empresas: this.http.get<EmpresaApi[]>('/api/empresas/mias'),
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
    return this.http.get<Array<{
      id: number; nombre: string; identificador: string;
      capacidadVehiculos: number | null; activo: boolean;
    }>>('/api/parqueaderos/mios').pipe(
      switchMap(parqueaderos =>
        this.http.get<Array<{ parqueaderoId: number | null; estado: string }>>('/api/espacios').pipe(
          map(espacios => parqueaderos.map(p => ({
            id: p.id,
            nombre: p.nombre,
            identificador: p.identificador,
            ocupados: espacios.filter(e => e.parqueaderoId === p.id && e.estado?.toUpperCase() === 'OCUPADO').length,
            capacidad: p.capacidadVehiculos ?? espacios.filter(e => e.parqueaderoId === p.id).length,
            activo: p.activo
          }))),
          catchError(() => of(parqueaderos.map(p => ({
            id: p.id,
            nombre: p.nombre,
            identificador: p.identificador,
            ocupados: 0,
            capacidad: p.capacidadVehiculos ?? 0,
            activo: p.activo
          }))))
        )
      )
    );
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

  getEmpresasMias(): Observable<EmpresaApi[]> {
    return this.http.get<EmpresaApi[]>('/api/empresas/mias');
  }

  getEmpresaId(): Observable<number | undefined> {
    return this.getEmpresasMias().pipe(map(empresas => empresas[0]?.id));
  }

  crearEmpresa(request: EmpresaCrearRequest): Observable<EmpresaApi> {
    return this.http.post<EmpresaApi>('/api/empresas', request);
  }

  getConfiguracion(): Observable<ConfiguracionEmpresa> {
    return this.getEmpresasMias().pipe(map(empresas => ({
      empresaId: empresas[0]?.id,
      razonSocial: empresas[0]?.nombre || '',
      correoContacto: '',
      zonaHoraria: 'America/Bogota',
      toleranciaSalidaMinutos: empresas[0]?.toleranciaSalidaMinutos ?? 30
    })));
  }

  crearParqueadero(empresaId: number, request: ParqueaderoCrearRequest): Observable<ParqueaderoCreadoResponse> {
    return this.http.post<ParqueaderoCreadoResponse>(
      `/api/empresas/${empresaId}/parqueaderos`, request);
  }

  guardarConfiguracion(empresaId: number, toleranciaSalidaMinutos: number): Observable<ConfiguracionEmpresa> {
    return this.http.put<EmpresaApi>(`/api/empresas/${empresaId}/configuracion`, {
      toleranciaSalidaMinutos
    }).pipe(map(empresa => ({
      empresaId: empresa.id,
      razonSocial: empresa.nombre,
      correoContacto: '',
      zonaHoraria: 'America/Bogota',
      toleranciaSalidaMinutos: empresa.toleranciaSalidaMinutos ?? 30
    })));
  }

  getPagosPendientes(): Observable<PagoOperativo[]> {
    return forkJoin({
      facturas: this.http.get<FacturaApi[]>('/api/facturas'),
      ingresos: this.getIngresosActivosBasic()
    }).pipe(map(({ facturas, ingresos }) => facturas
      .filter(factura => factura.estado?.toUpperCase() === 'PENDIENTE'
        || (factura.estado?.toUpperCase() === 'PAGADA'
          && factura.estadoIngreso?.toUpperCase() === 'ACTIVO'
          && (factura.faltante ?? 0) > 0))
      .map(factura => {
        const ingreso = ingresos.find(item => item.id === factura.ingresoId);
        const esDiferencia = factura.estado?.toUpperCase() === 'PAGADA';
        return {
          id: factura.id,
          ingresoId: factura.ingresoId,
          referencia: `FACTURA-${factura.id}`,
          placa: ingreso?.placa,
          cliente: ingreso?.cliente,
          monto: esDiferencia ? (factura.faltante ?? 0) : (factura.valorActual ?? factura.valor ?? 0),
          fecha: factura.fechaEmision ?? '',
          estado: 'PENDIENTE' as const,
          esDiferencia
        };
      })));
  }

  getHistorialPagos(): Observable<PagoOperativo[]> {
    return this.http.get<Array<{ id: number; ingresoId: number; valor: number | null; fechaPago: string | null; estado: string }>>('/api/facturas')
      .pipe(map(facturas => facturas
        .filter(factura => factura.estado?.toUpperCase() === 'PAGADA')
        .map(factura => ({
          id: factura.id,
          ingresoId: factura.ingresoId,
          referencia: `FACTURA-${factura.id}`,
          monto: factura.valor ?? 0,
          fecha: factura.fechaPago ?? '',
          estado: 'CONFIRMADO' as const
        }))));
  }

  confirmarPago(id: number): Observable<PagoOperativo> {
    return this.http.post<{ id: number; valor: number | null; valorActual?: number | null; fechaPago: string | null }>(`/api/facturas/${id}/pagar`, {})
      .pipe(map(factura => ({
        id: factura.id,
        referencia: `FACTURA-${factura.id}`,
        monto: factura.valorActual ?? factura.valor ?? 0,
        fecha: factura.fechaPago ?? '',
        estado: 'CONFIRMADO' as const
      })));
  }

  getIngresosActivos(): Observable<IngresoOperativo[]> {
    return this.getIngresosActivosBasic().pipe(
      switchMap(ingresos => {
        if (!ingresos.length) {
          return of(ingresos);
        }
        return forkJoin(ingresos.map(ingreso => this.getEstadoPago(ingreso.id).pipe(
          map(estado => ({ ...ingreso, ...estado }))
        )));
      })
    );
  }

  getEstadoPago(ingresoId: number): Observable<Partial<IngresoOperativo>> {
    return this.http.get<EstadoPagoOperativoResponse>(`/api/ingresos/${ingresoId}/estado-pago`)
      .pipe(map(estado => ({
        estadoPago: estado.estado,
        puedeSalir: estado.puedeSalir,
        minutosTolerancia: estado.minutosTolerancia,
        fechaLimiteSalida: estado.fechaLimiteSalida ?? undefined,
        valorActual: estado.valorActual ?? undefined,
        faltante: estado.faltante ?? undefined
      })));
  }

  private getIngresosActivosBasic(): Observable<IngresoOperativo[]> {
    return this.http.get<IngresoApi[]>('/api/ingresos/activos')
      .pipe(map(items => items.map(item => ({
        id: item.id,
        placa: item.placa,
        espacio: item.espacioNumero,
        fechaIngreso: item.fechaEntrada,
        estado: item.estado?.toUpperCase() === 'RESERVADO' ? 'ESPERANDO' : 'ACTIVO',
        novedad: item.novedad
      }))));
  }

  actualizarIngreso(id: number, accion: AccionIngreso): Observable<IngresoOperativo> {
    const endpoint = accion === 'VALIDAR_ENTRADA'
      ? `/api/ingresos/${id}/llegada`
      : `/api/ingresos/${id}/salida`;
    return this.http.put<IngresoApi>(endpoint, {}).pipe(map(item => ({
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
    return this.getIngresosActivosBasic().pipe(map(items =>
      items.filter(item => !!item.novedad).map(item => ({
        id: item.id,
        ingresoId: item.id,
        descripcion: item.novedad || '',
        fecha: item.fechaIngreso
      }))
    ));
  }
}
