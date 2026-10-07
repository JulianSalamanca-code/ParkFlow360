export interface Pago {
  id: number;
  vehiculoId?: number;
  espacioId?: number;
  tarifaId?: number;
  valor: number;
  fecha: string;
  metodoPago?: string;
  estado?: string;
  referencia?: string;
  wompiTransactionId?: string;
  moneda?: string;
  usuarioId?: number;
  ingresoId?: number;
}

export interface PagoRequest {
  vehiculoId: number;
  espacioId: number;
  tarifaId: number;
  valor: number;
  metodoPago: string;
}

export interface PagoOnlineRequest {
  valor: number;
  ingresoId?: number;
  vehiculoId?: number;
  espacioId?: number;
  tarifaId?: number;
}

export interface PagoOnlineResponse {
  pagoId: number;
  referencia: string;
  checkoutUrl: string;
  valor: number;
  valorEnCentavos: number;
  moneda: string;
  estado: string;
}
