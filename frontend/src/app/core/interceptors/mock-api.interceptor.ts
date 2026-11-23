import { HttpErrorResponse, HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';
import {
  aplicarAccionIngreso,
  CONFIGURACION_MOCK,
  EMPRESA_RESUMEN_MOCK,
  ingresosSinSalida,
  marcarPagoConfirmado,
  OCUPACION_MOCK,
  pagosPorEstado,
  PARQUEADEROS_MOCK,
  REPORTES_MOCK,
  guardarNovedad,
  consultarNovedades
} from './operaciones.mock';
import {
  AccionIngreso,
  NovedadRequest
} from '../../features/operaciones/models/operaciones.model';

const MOCK_STATE_KEY = 'parkflow.mock.state';
const MOCK_NOT_FOUND = Symbol('mock-not-found');

export const mockApiInterceptor: HttpInterceptorFn = (request, next) => {
  const path = new URL(request.url, window.location.origin).pathname;
  const state = window.sessionStorage.getItem(MOCK_STATE_KEY);
  if (!isMockEndpoint(request.method, path)) {
    return next(request);
  }

  if (state === 'error') {
    return throwError(() => new HttpErrorResponse({
        status: 503,
        statusText: 'Servicio mock no disponible',
        error: { message: 'No fue posible cargar la informaciÃ³n de prueba.' },
        url: request.urlWithParams
      }));
  }

  const empty = state === 'empty';
  const body = resolveMockResponse(request.method, request.urlWithParams, path, request.body, empty);
  if (body === undefined) {
    return next(request);
  }

  if (body === MOCK_NOT_FOUND) {
    return throwError(() => new HttpErrorResponse({
        status: 404,
        statusText: 'No encontrado',
        error: { message: 'El registro solicitado no existe en los datos mock.' },
        url: request.urlWithParams
      }));
  }

  return of(new HttpResponse({ status: 200, body }));
};

function isMockEndpoint(method: string, path: string): boolean {
  return (method === 'GET' && [
    '/api/empresa/resumen',
    '/api/empresa/parqueaderos',
    '/api/empresa/reportes',
    '/api/empresa/configuracion',
    '/api/cajero/pagos',
    '/api/guardia/ingresos',
    '/api/guardia/ocupacion',
    '/api/guardia/novedades'
  ].includes(path))
    || (method === 'POST' && (
      /^\/api\/cajero\/pagos\/\d+\/confirmar$/.test(path)
      || path === '/api/guardia/novedades'
    ))
    || (method === 'PATCH' && /^\/api\/guardia\/ingresos\/\d+$/.test(path));
}

function resolveMockResponse(method: string, urlWithParams: string, path: string, requestBody: unknown, empty: boolean): unknown {
  if (method === 'GET' && path === '/api/empresa/resumen') {
    return empty ? { ...EMPRESA_RESUMEN_MOCK, parqueaderosActivos: 0, espaciosTotales: 0, espaciosOcupados: 0, ingresosHoy: 0, recaudoHoy: 0 } : EMPRESA_RESUMEN_MOCK;
  }
  if (method === 'GET' && path === '/api/empresa/parqueaderos') {
    return empty ? [] : PARQUEADEROS_MOCK;
  }
  if (method === 'GET' && path === '/api/empresa/reportes') {
    return empty ? [] : REPORTES_MOCK;
  }
  if (method === 'GET' && path === '/api/empresa/configuracion') {
    return empty ? { razonSocial: '', correoContacto: '', zonaHoraria: '' } : CONFIGURACION_MOCK;
  }
  if (method === 'GET' && path === '/api/cajero/pagos') {
    const estado = new URL(urlWithParams, window.location.origin).searchParams.get('estado');
    return empty ? [] : pagosPorEstado(estado === 'CONFIRMADO' ? 'CONFIRMADO' : 'PENDIENTE');
  }
  const confirmarPago = path.match(/^\/api\/cajero\/pagos\/(\d+)\/confirmar$/);
  if (method === 'POST' && confirmarPago) {
    const pago = marcarPagoConfirmado(Number(confirmarPago[1]));
    return pago ?? MOCK_NOT_FOUND;
  }
  if (method === 'GET' && path === '/api/guardia/ingresos') {
    return empty ? [] : ingresosSinSalida();
  }
  const accionIngreso = path.match(/^\/api\/guardia\/ingresos\/(\d+)$/);
  if (method === 'PATCH' && accionIngreso) {
    if (isAccionIngresoRequest(requestBody)) {
      return aplicarAccionIngreso(Number(accionIngreso[1]), requestBody.accion) ?? MOCK_NOT_FOUND;
    }
    return MOCK_NOT_FOUND;
  }
  if (method === 'GET' && path === '/api/guardia/ocupacion') {
    return empty ? { ...OCUPACION_MOCK, capacidad: 0, ocupados: 0, reservados: 0, disponibles: 0 } : OCUPACION_MOCK;
  }
  if (method === 'POST' && path === '/api/guardia/novedades') {
    return isNovedadRequest(requestBody) ? guardarNovedad(requestBody) : MOCK_NOT_FOUND;
  }
  if (method === 'GET' && path === '/api/guardia/novedades') {
    return empty ? [] : consultarNovedades();
  }
  return undefined;
}

function isAccionIngresoRequest(value: unknown): value is { accion: AccionIngreso } {
  if (typeof value !== 'object' || value === null || !('accion' in value)) {
    return false;
  }
  const accion = value.accion;
  return accion === 'VALIDAR_ENTRADA' || accion === 'REGISTRAR_SALIDA';
}

function isNovedadRequest(value: unknown): value is NovedadRequest {
  if (typeof value !== 'object' || value === null || !('ingresoId' in value) || !('descripcion' in value)) {
    return false;
  }
  return typeof value.ingresoId === 'number'
    && typeof value.descripcion === 'string'
    && value.descripcion.trim().length > 0;
}
