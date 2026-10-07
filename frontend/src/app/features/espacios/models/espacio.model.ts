export interface Espacio {
  id: number;
  numero: string;
  tipo: string;
  estado: string;
  piso: number;
  planoId?: number;
  fila?: number;
  columna?: number;
  posX?: number;
  posY?: number;
  ancho?: number;
  alto?: number;
  fechaCreacion?: string;
}

export interface EspacioRequest {
  numero: string;
  tipo: string;
  estado: string;
  piso: number;
  planoId?: number;
  fila?: number;
  columna?: number;
  posX?: number;
  posY?: number;
  ancho?: number;
  alto?: number;
}
