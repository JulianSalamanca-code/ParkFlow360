import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { LoginRequest, LoginResponse, RegistroRequest } from '../models/auth.model';
import { Rol, homeRouteForRol, normalizeRol } from '../../core/auth/roles';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = '/api/auth';
  private tokenKey = 'token';
  private userKey = 'user';

  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasToken());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  private rolSubject = new BehaviorSubject<Rol | null>(this.getRol());
  public rol$: Observable<Rol | null> = this.rolSubject.asObservable();

  public isPlataforma$ = this.hasRole$('PLATAFORMA');
  public isEmpresa$ = this.hasRole$('EMPRESA');
  public isCajero$ = this.hasRole$('CAJERO_PAGOS');
  public isGuardia$ = this.hasRole$('GUARDIA');
  public isUsuario$ = this.hasRole$('USUARIO');

  /** @deprecated usar `isPlataforma$`. Se conserva por compatibilidad. */
  public isAdmin$ = this.isPlataforma$;

  /** Ruta inicial del rol actual, útil para redirecciones tras el login. */
  public homeRoute$: Observable<string> = this.rol$.pipe(
    map(rol => homeRouteForRol(rol))
  );

  constructor(private http: HttpClient) {}

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(response => this.handleSession(response))
    );
  }

  /** Inicia sesión con el ID token (credential) de Google Identity Services. */
  loginConGoogle(credential: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/google`, { credential }).pipe(
      tap(response => this.handleSession(response))
    );
  }

  registrar(registroData: RegistroRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/registro`, registroData).pipe(
      tap(response => this.handleSession(response))
    );
  }

  solicitarRecuperacion(email: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/recuperacion/solicitar`, { email });
  }

  restablecerPassword(token: string, password: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/recuperacion/restablecer`, {
      token,
      password
    });
  }

  handleGoogleCallback(code: string): Observable<LoginResponse> {
    return this.http.get<LoginResponse>(`${this.apiUrl}/google/callback`, {
      params: { code }
    }).pipe(
      tap(response => this.handleSession(response))
    );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    this.isAuthenticatedSubject.next(false);
    this.rolSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  getUser(): LoginResponse | null {
    const user = localStorage.getItem(this.userKey);
    return user ? JSON.parse(user) as LoginResponse : null;
  }

  /** Rol canónico del usuario (los tokens `ADMIN` se leen como `PLATAFORMA`). */
  getRol(): Rol | null {
    return normalizeRol(this.getUser()?.rol);
  }

  hasRole(rol: string | null | undefined): boolean {
    const esperado = normalizeRol(rol);
    return !!esperado && this.getRol() === esperado;
  }

  hasAnyRole(roles: readonly string[]): boolean {
    const actual = this.getRol();
    return !!actual && roles.some(rol => normalizeRol(rol) === actual);
  }

  /** Ruta inicial según el rol actual. */
  homeRoute(): string {
    return homeRouteForRol(this.getRol());
  }

  isLoggedIn(): boolean {
    return this.hasToken();
  }

  /** @deprecated usar `hasRole('PLATAFORMA')`. */
  isAdmin(): boolean {
    return this.hasRole('PLATAFORMA');
  }

  isUsuario(): boolean {
    return this.hasRole('USUARIO');
  }

  private hasRole$(rol: Rol): Observable<boolean> {
    return this.rol$.pipe(map(actual => actual === rol));
  }

  private handleSession(response: LoginResponse): void {
    this.setToken(response.token);
    this.setUser(response);
    this.isAuthenticatedSubject.next(true);
    this.rolSubject.next(normalizeRol(response.rol));
  }

  private hasToken(): boolean {
    return !!this.getToken();
  }

  private setToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
  }

  private setUser(user: LoginResponse): void {
    localStorage.setItem(this.userKey, JSON.stringify(user));
  }
}
