export interface EmpresaResumen {
  readonly empresa: string;
  readonly parqueaderosActivos: number;
  readonly espaciosTotales: number;
  readonly espaciosOcupados: number;
  readonly ingresosHoy: number;
  readonly recaudoHoy: number;
}

export interface EmpresaApi {
  readonly id: number;
  readonly nombre: string;
  readonly identificador: string;
  readonly activo: boolean;
  readonly toleranciaSalidaMinutos: number;
}

export interface EmpresaCrearRequest {
  readonly nombre: string;
  readonly identificador: string;
  readonly usuarioEmail: string;
  readonly toleranciaSalidaMinutos: number;
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

export interface TipoVehiculoCrearRequest {
  readonly tipo: string;
  readonly cantidadFija: boolean;
  readonly cantidad: number | null;
}

export interface EspacioCrearRequest {
  readonly numero: string;
  readonly tipo: string;
  readonly piso: number;
  readonly fila?: number;
  readonly columna?: number;
}

export interface ParqueaderoCrearRequest {
  readonly nombre: string;
  readonly identificador: string;
  readonly capacidadVehiculos: number | null;
  readonly tipos: readonly TipoVehiculoCrearRequest[];
  readonly espacios: readonly EspacioCrearRequest[];
  readonly cajeroEmail: string;
  readonly cajeroPassword: string;
  readonly cajeroNombre: string;
}

export interface TipoVehiculoCreadoResponse {
  readonly id: number;
  readonly tipo: string;
  readonly cantidadFija: boolean;
  readonly cantidad: number | null;
  readonly espaciosOcupados: number;
}

export interface ParqueaderoCreadoResponse {
  readonly id: number;
  readonly empresaId: number;
  readonly nombre: string;
  readonly identificador: string;
  readonly capacidadVehiculos: number | null;
  readonly activo: boolean;
  readonly cajeroEmail: string;
  readonly tipos: readonly TipoVehiculoCreadoResponse[];
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
