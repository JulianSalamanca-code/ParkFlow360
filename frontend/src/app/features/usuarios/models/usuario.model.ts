import { Rol } from '../../../core/auth/roles';

export interface Usuario {
  id: number;
  email: string;
  rol: Rol;
  nombre: string;
  fechaCreacion: string;
}

export interface UsuarioRequest {
  email: string;
  password?: string;
  rol: Rol;
  nombre: string;
}
