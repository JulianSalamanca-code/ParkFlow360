export interface Usuario {
  id: number;
  email: string;
  rol: string;
  nombre: string;
  fechaCreacion: string;
}

export interface UsuarioRequest {
  email: string;
  password?: string;
  rol: string;
  nombre: string;
}
