export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  tipo: string;
  email: string;
  rol: string;
  nombre: string;
}

export interface RegistroRequest {
  nombre: string;
  email: string;
  password: string;
}
