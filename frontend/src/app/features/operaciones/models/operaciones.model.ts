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
  readonly razonSocial: string;
  readonly correoContacto: string;
  readonly zonaHoraria: string;
}

export type EstadoPagoOperativo = 'PENDIENTE' | 'CONFIRMADO';

export interface PagoOperativo {
  readonly id: number;
  readonly referencia: string;
  readonly placa?: string;
  readonly cliente?: string;
  readonly monto: number;
  readonly fecha: string;
  readonly estado: EstadoPagoOperativo;
}

export interface IngresoOperativo {
  readonly id: number;
  readonly placa: string;
  readonly cliente?: string;
  readonly espacio?: string;
  readonly fechaIngreso: string;
  readonly estado: 'ESPERANDO' | 'ACTIVO';
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
