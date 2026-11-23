/**
 * Roles canónicos de ParkFlow360.
 *
 * V17 normalizó la base de datos y los tokens a estos cinco valores. `ADMIN`
 * se conserva únicamente como alias de entrada para sesiones antiguas.
 */
export type Rol =
  | 'PLATAFORMA'
  | 'EMPRESA'
  | 'CAJERO_PAGOS'
  | 'GUARDIA'
  | 'USUARIO';

export const ROLES: readonly Rol[] = [
  'PLATAFORMA',
  'EMPRESA',
  'CAJERO_PAGOS',
  'GUARDIA',
  'USUARIO'
];

const ROL_ALIASES: Readonly<Record<string, Rol>> = {
  ADMIN: 'PLATAFORMA',
  PLATAFORMA: 'PLATAFORMA',
  EMPRESA: 'EMPRESA',
  CAJERO: 'CAJERO_PAGOS',
  CAJERO_PAGOS: 'CAJERO_PAGOS',
  GUARDIA: 'GUARDIA',
  USUARIO: 'USUARIO',
  CLIENTE: 'USUARIO'
};

const ROLE_LABELS: Readonly<Record<Rol, string>> = {
  PLATAFORMA: 'Plataforma',
  EMPRESA: 'Empresa',
  CAJERO_PAGOS: 'Cajero de pagos',
  GUARDIA: 'Guardia',
  USUARIO: 'Usuario'
};

/** Descripciones usadas en los formularios de alta y edición de usuarios. */
export const ROLE_DESCRIPTIONS: Readonly<Record<Rol, string>> = {
  PLATAFORMA: 'acceso total a todas las empresas',
  EMPRESA: 'administra sus parqueaderos y reportes',
  CAJERO_PAGOS: 'confirma los cobros de su parqueadero',
  GUARDIA: 'controla accesos y salidas de su parqueadero',
  USUARIO: 'reserva y gestiona sus vehículos y pagos'
};

/**
 * Pantalla inicial de cada rol.
 */
const HOME_BY_ROL: Readonly<Record<Rol, string>> = {
  PLATAFORMA: '/planos',
  EMPRESA: '/empresa',
  CAJERO_PAGOS: '/cajero/pagos',
  GUARDIA: '/guardia/ingresos',
  USUARIO: '/mis-parqueos'
};

/** Normaliza el rol recibido del backend o de localStorage. */
export function normalizeRol(value: string | null | undefined): Rol | null {
  if (!value) {
    return null;
  }

  return ROL_ALIASES[value.trim().toUpperCase()] ?? null;
}

/** Etiqueta legible para mostrar el rol en la interfaz. */
export function roleLabel(value: string | null | undefined): string {
  const rol = normalizeRol(value);
  return rol ? ROLE_LABELS[rol] : '';
}

/** Ruta inicial para el rol indicado. */
export function homeRouteForRol(value: string | null | undefined): string {
  const rol = normalizeRol(value);
  return rol ? HOME_BY_ROL[rol] : '/auth/login';
}
