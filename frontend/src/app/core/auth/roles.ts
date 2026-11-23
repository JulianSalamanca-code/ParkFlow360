/**
 * Roles canónicos de ParkFlow360.
 *
 * El modelo objetivo define cinco roles. Mientras el backend migra V17,
 * se acepta `ADMIN` como alias transitorio de `PLATAFORMA` para no invalidar
 * los tokens vigentes.
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
