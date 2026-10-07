export interface Plano {
  id: number;
  nombre: string;
  descripcion?: string;
  imagenUrl?: string;
  piso?: number;
  filas: number;
  columnas: number;
  ancho?: number;
  alto?: number;
  totalEspacios: number;
  fechaCreacion?: string;
}

export interface PlanoRequest {
  nombre: string;
  descripcion?: string;
  imagenUrl?: string;
  piso?: number;
  filas?: number;
  columnas?: number;
  ancho?: number;
  alto?: number;
}

export interface GenerarEspaciosRequest {
  prefijo?: string;
  tipo: string;
  piso?: number;
  filas?: number;
  columnas?: number;
  estadoInicial?: string;
}
