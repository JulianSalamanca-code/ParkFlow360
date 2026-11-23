import {
  ConfiguracionEmpresa,
  EmpresaResumen,
  IngresoOperativo,
  NovedadRequest,
  NovedadResponse,
  OcupacionOperativa,
  PagoOperativo,
  ParqueaderoOperativo,
  ReporteOperativo
} from '../../features/operaciones/models/operaciones.model';

export const EMPRESA_RESUMEN_MOCK: EmpresaResumen = {
  empresa: 'Movilidad Central',
  parqueaderosActivos: 2,
  espaciosTotales: 180,
  espaciosOcupados: 126,
  ingresosHoy: 84,
  recaudoHoy: 1845000
};

export const PARQUEADEROS_MOCK: ParqueaderoOperativo[] = [
  { id: 1, nombre: 'Centro Plaza', identificador: 'CP-042', direccion: 'Cra. 7 # 18-40', ocupados: 72, capacidad: 100, activo: true },
  { id: 2, nombre: 'Torre Norte', identificador: 'TN-118', direccion: 'Calle 94 # 15-20', ocupados: 54, capacidad: 80, activo: true }
];

export const REPORTES_MOCK: ReporteOperativo[] = [
  { periodo: 'Hoy', ingresos: 84, recaudo: 1845000, ocupacionPromedio: 70 },
  { periodo: 'Ayer', ingresos: 112, recaudo: 2310000, ocupacionPromedio: 78 },
  { periodo: 'Esta semana', ingresos: 648, recaudo: 14780000, ocupacionPromedio: 74 }
];

export const CONFIGURACION_MOCK: ConfiguracionEmpresa = {
  razonSocial: 'Movilidad Central S.A.S.',
  correoContacto: 'operaciones@movilidadcentral.co',
  zonaHoraria: 'America/Bogota'
};

export const PAGOS_MOCK: PagoOperativo[] = [
  { id: 301, referencia: 'FAC-2026-0301', placa: 'KLM-482', cliente: 'Laura Méndez', monto: 12000, fecha: '09:42', estado: 'PENDIENTE' },
  { id: 302, referencia: 'FAC-2026-0302', placa: 'JPR-915', cliente: 'Andrés Gil', monto: 8000, fecha: '09:36', estado: 'PENDIENTE' },
  { id: 303, referencia: 'FAC-2026-0303', placa: 'MNV-227', cliente: 'Camila Ruiz', monto: 15000, fecha: '09:18', estado: 'CONFIRMADO' }
];

export const INGRESOS_MOCK: IngresoOperativo[] = [
  { id: 501, placa: 'KLM-482', cliente: 'Laura Méndez', espacio: 'A-014', fechaIngreso: '09:42', estado: 'ESPERANDO' },
  { id: 502, placa: 'JPR-915', cliente: 'Andrés Gil', espacio: 'B-008', fechaIngreso: '09:36', estado: 'ACTIVO' }
];

export const OCUPACION_MOCK: OcupacionOperativa = {
  parqueadero: 'Centro Plaza',
  capacidad: 100,
  ocupados: 72,
  reservados: 8,
  disponibles: 20
};

const pagos = PAGOS_MOCK.map(pago => ({ ...pago }));
const ingresos = INGRESOS_MOCK.map(ingreso => ({ ...ingreso }));
const novedades: NovedadResponse[] = [];

export function pagosPorEstado(estado: PagoOperativo['estado']): PagoOperativo[] {
  return pagos.filter(pago => pago.estado === estado);
}

export function marcarPagoConfirmado(id: number): PagoOperativo | undefined {
  const index = pagos.findIndex(pago => pago.id === id);
  if (index < 0) {
    return undefined;
  }
  pagos[index] = { ...pagos[index], estado: 'CONFIRMADO' };
  return pagos[index];
}

export function ingresosSinSalida(): IngresoOperativo[] {
  return [...ingresos];
}

export function aplicarAccionIngreso(
  id: number,
  accion: 'VALIDAR_ENTRADA' | 'REGISTRAR_SALIDA'
): IngresoOperativo | undefined {
  const index = ingresos.findIndex(ingreso => ingreso.id === id);
  if (index < 0) {
    return undefined;
  }
  const actual = ingresos[index];
  if (accion === 'VALIDAR_ENTRADA' && actual.estado === 'ESPERANDO') {
    ingresos[index] = { ...actual, estado: 'ACTIVO' };
  } else if (accion === 'REGISTRAR_SALIDA' && actual.estado === 'ACTIVO') {
    ingresos.splice(index, 1);
    return { ...actual, estado: 'ACTIVO' };
  }
  return ingresos[index];
}

export function guardarNovedad(request: NovedadRequest): NovedadResponse {
  const novedad: NovedadResponse = {
    ...request,
    id: novedades.length + 1,
    fecha: new Date().toISOString()
  };
  novedades.unshift(novedad);
  return novedad;
}

export function consultarNovedades(): NovedadResponse[] {
  return [...novedades];
}
