export interface Pago {
  id: number;
  vehiculoId: number;
  espacioId: number;
  tarifaId: number;
  ingresoId?: number | null;
  valor: number;
  fecha: string;
  metodoPago: string;
  estadoPago?: 'CONFIRMADO' | 'FALLIDO';
}

export interface PagoRequest {
  ingresoId?: number;
  vehiculoId?: number;
  espacioId?: number;
  tarifaId?: number;
  valor: number;
  metodoPago: string;
  estadoPago: 'CONFIRMADO' | 'FALLIDO';
}

/** POST /api/pagos/online */
export interface PagoOnlineRequest {
  valor: number;
  ingresoId?: number;
  vehiculoId?: number;
  espacioId?: number;
  tarifaId?: number;
}

/** Respuesta con la URL de checkout de Wompi. */
export interface PagoOnlineResponse {
  pagoId: number;
  referencia: string;
  checkoutUrl: string | null;
  valor: number;
  valorEnCentavos?: number;
  moneda?: string;
  estado?: string;
}
