import { Rol, normalizeRol } from '../auth/roles';

export interface NavItem {
  readonly id: string;
  readonly label: string;
  readonly shortLabel: string;
  readonly icon: string;
  readonly path: string;
  readonly dataTestId: string;
}

export interface NavSection {
  readonly label: string;
  readonly items: readonly NavItem[];
}

function item(
  id: string,
  label: string,
  shortLabel: string,
  icon: string,
  path: string
): NavItem {
  return { id, label, shortLabel, icon, path, dataTestId: `nav-item-${id}` };
}

const PLANOS = item('planos', 'Plano y Ocupación', 'Planos', 'map', '/planos');
const ESPACIOS = item('espacios', 'Espacios', 'Espacios', 'local_parking', '/espacios');
const TARIFAS = item('tarifas', 'Tarifas', 'Tarifas', 'sell', '/tarifas');
const INGRESOS = item('ingresos', 'Ingresos', 'Ingresos', 'receipt_long', '/ingresos');
const PAGOS = item('pagos', 'Pagos', 'Pagos', 'point_of_sale', '/pagos');
const REPORTES = item('reportes', 'Reportes', 'Reportes', 'analytics', '/reportes');
const USUARIOS = item('usuarios', 'Usuarios', 'Usuarios', 'manage_accounts', '/usuarios');

const RESERVAR = item('reservar', 'Reservar Parqueadero', 'Reservar', 'add_circle', '/reservar');
const MIS_PARQUEOS = item('mis-parqueos', 'Mis Parqueos', 'Parqueos', 'local_parking', '/mis-parqueos');
const MIS_VEHICULOS = item('mis-vehiculos', 'Mis Vehículos', 'Vehículos', 'directions_car', '/mis-vehiculos');
const PAGAR = item('pagar', 'Pagar en Línea', 'Pagar', 'payments', '/pagar');
const EMPRESA_HOME = item('empresa-home', 'Resumen', 'Resumen', 'dashboard', '/empresa');
const EMPRESA_PARQUEADEROS = item('empresa-parqueaderos', 'Parqueaderos', 'Parqueos', 'domain', '/empresa/parqueaderos');
const EMPRESA_PUESTOS = item('empresa-puestos', 'Puestos', 'Puestos', 'local_parking', '/espacios');
const EMPRESA_REPORTES = item('empresa-reportes', 'Reportes', 'Reportes', 'analytics', '/empresa/reportes');
const EMPRESA_CONFIGURACION = item('empresa-configuracion', 'Configuración', 'Config.', 'settings', '/empresa/configuracion');
const CAJERO_PAGOS = item('cajero-pagos', 'Pagos pendientes', 'Caja', 'point_of_sale', '/cajero/pagos');
const CAJERO_HISTORIAL = item('cajero-historial', 'Historial', 'Historial', 'history', '/cajero/historial');
const GUARDIA_INGRESOS = item('guardia-ingresos', 'Control de accesos', 'Accesos', 'fact_check', '/guardia/ingresos');
const GUARDIA_OCUPACION = item('guardia-ocupacion', 'Ocupación', 'Ocupación', 'local_parking', '/guardia/ocupacion');

/**
 * Una sola fuente de navegación para el sidebar y la barra inferior.
 * La barra lateral y la barra inferior consumen esta misma configuración.
 */
const SIDEBAR_BY_ROL: Readonly<Record<Rol, readonly NavSection[]>> = {
  PLATAFORMA: [
    {
      label: 'Operación Central',
      items: [PLANOS, ESPACIOS, TARIFAS, INGRESOS, PAGOS, REPORTES]
    },
    {
      label: 'Administración',
      items: [EMPRESA_PARQUEADEROS, USUARIOS]
    }
  ],
  EMPRESA: [
    {
      label: 'Administración',
      items: [EMPRESA_HOME, EMPRESA_PARQUEADEROS, EMPRESA_PUESTOS, EMPRESA_REPORTES, EMPRESA_CONFIGURACION]
    }
  ],
  CAJERO_PAGOS: [
    { label: 'Recaudo', items: [CAJERO_PAGOS, CAJERO_HISTORIAL] }
  ],
  GUARDIA: [
    { label: 'Operación', items: [GUARDIA_INGRESOS, GUARDIA_OCUPACION] }
  ],
  USUARIO: [
    {
      label: 'Mi Cuenta',
      items: [RESERVAR, MIS_PARQUEOS, MIS_VEHICULOS, PAGAR]
    }
  ]
};

/** Ítems principales de la barra inferior (máximo 4 + hamburguesa). */
const BOTTOM_BY_ROL: Readonly<Record<Rol, readonly NavItem[]>> = {
  PLATAFORMA: [PLANOS, ESPACIOS, INGRESOS, PAGOS],
  EMPRESA: [EMPRESA_HOME, EMPRESA_PARQUEADEROS, EMPRESA_PUESTOS, EMPRESA_REPORTES],
  CAJERO_PAGOS: [CAJERO_PAGOS, CAJERO_HISTORIAL],
  GUARDIA: [GUARDIA_INGRESOS, GUARDIA_OCUPACION],
  USUARIO: [RESERVAR, MIS_PARQUEOS, MIS_VEHICULOS, PAGAR]
};

export function sidebarSectionsFor(rol: string | null | undefined): readonly NavSection[] {
  const normalized = normalizeRol(rol);
  return normalized ? SIDEBAR_BY_ROL[normalized] : [];
}

export function bottomItemsFor(rol: string | null | undefined): readonly NavItem[] {
  const normalized = normalizeRol(rol);
  return normalized ? BOTTOM_BY_ROL[normalized] : [];
}
