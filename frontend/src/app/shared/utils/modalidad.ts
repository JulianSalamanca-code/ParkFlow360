/**
 * RN-12: la modalidad de la reserva se deriva del tipo de tarifa.
 * No se debe usar un valor fijo como `POR HORAS` en los componentes.
 */
const MODALIDAD_POR_TIPO: Readonly<Record<string, string>> = {
  HORA: 'POR HORAS',
  DIA: 'POR DÍA',
  SEMANA: 'POR SEMANA',
  MES: 'POR MES',
  MOMENTO: 'POR MOMENTO'
};

export function modalidadFromTipo(tipo: string | null | undefined): string {
  if (!tipo) {
    return 'SIN TARIFA';
  }

  const key = tipo.trim().toUpperCase();
  return MODALIDAD_POR_TIPO[key] ?? key;
}
