export interface EmpresaResumen {
  readonly empresa: string;
  readonly parqueaderosActivos: number;
  readonly espaciosTotales: number;
  readonly espaciosOcupados: number;
  readonly ingresosHoy: number;
  readonly recaudoHoy: number;
}

export interface ParqueaderoOperativo {
  readonly id: number;
  readonly nombre: string;
  readonly identificador: string;
  readonly direccion?: string;
  readonly ocupados?: number;
  readonly capacidad: number;
  readonly activo: boolean;
}

export interface ReporteOperativo {
  readonly periodo: string;
  readonly ingresos: number;
  readonly recaudo: number;
  readonly ocupacionPromedio: number;
}

export interface ConfiguracionEmpresa {
  readonly empresaId?: number;
  readonly razonSocial: string;
  readonly correoContacto: string;
  readonly zonaHoraria: string;
  readonly toleranciaSalidaMinutos: number;
}

export type EstadoPagoOperativo = 'PENDIENTE' | 'CONFIRMADO';

export interface PagoOperativo {
  readonly id: number;
  readonly ingresoId?: number;
  readonly referencia: string;
  readonly placa?: string;
  readonly cliente?: string;
  readonly monto: number;
  readonly fecha: string;
  readonly estado: EstadoPagoOperativo;
  readonly esDiferencia?: boolean;
}

export interface IngresoOperativo {
  readonly id: number;
  readonly placa: string;
  readonly cliente?: string;
  readonly espacio?: string;
  readonly fechaIngreso: string;
  readonly estado: 'ESPERANDO' | 'ACTIVO';
  readonly novedad?: string;
  readonly estadoPago?: string;
  readonly puedeSalir?: boolean;
  readonly fechaLimiteSalida?: string;
  readonly minutosTolerancia?: number;
  readonly valorActual?: number;
  readonly faltante?: number;
}

export interface EstadoPagoOperativoResponse {
  readonly ingresoId?: number;
  readonly estado: string;
  readonly alDia: boolean;
  readonly puedeSalir: boolean;
  readonly fechaPago?: string | null;
  readonly minutosTolerancia: number;
  readonly fechaLimiteSalida?: string | null;
  readonly valorActual?: number | null;
  readonly faltante?: number | null;
}

export interface OcupacionOperativa {
  readonly parqueadero: string;
  readonly capacidad: number;
  readonly ocupados: number;
  readonly reservados: number;
  readonly disponibles: number;
}

export type AccionIngreso = 'VALIDAR_ENTRADA' | 'REGISTRAR_SALIDA';

export interface NovedadRequest {
  readonly ingresoId: number;
  readonly descripcion: string;
}

export interface NovedadResponse extends NovedadRequest {
  readonly id: number;
  readonly fecha: string;
}
