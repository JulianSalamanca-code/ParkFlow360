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
