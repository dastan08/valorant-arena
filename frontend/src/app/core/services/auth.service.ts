import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { JwtHelperService } from '@auth0/angular-jwt';
import { LoginRequest, RegisterRequest, AuthResponse, ApiResponse } from '../models/auth.model';
import { User } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';
  private currentUserSubject: BehaviorSubject<User | null>;
  public currentUser: Observable<User | null>;
  private isFetchingProfile = false;

  constructor(
    private http: HttpClient,
    private router: Router,
    private jwtHelper: JwtHelperService
  ) {
    this.currentUserSubject = new BehaviorSubject<User | null>(null);
    this.currentUser = this.currentUserSubject.asObservable();
    this.loadCurrentUser();
  }

  public get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  register(request: RegisterRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/register`, request)
      .pipe(
        tap(response => {
          if (response.success && response.data) {
            this.handleAuthResponse(response.data);
          }
        })
      );
  }

  login(request: LoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/login`, request)
      .pipe(
        tap(response => {
          if (response.success && response.data) {
            this.handleAuthResponse(response.data);
          }
        })
      );
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  isAuthenticated(): boolean {
    const token = localStorage.getItem('token');
    return token != null && !this.jwtHelper.isTokenExpired(token);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  handleAuthResponse(authResponse: AuthResponse): void {
    localStorage.setItem('token', authResponse.token);
    console.log('AuthService - Handling auth response for:', authResponse.username);
    
    // Fetch profile ONCE after login
    this.fetchUserProfileOnce();
  }
  
  private loadCurrentUser(): void {
    const token = localStorage.getItem('token');
    const userJson = localStorage.getItem('currentUser');
    
    if (userJson) {
      try {
        const user = JSON.parse(userJson);
        this.currentUserSubject.next(user);
      } catch (e) {
        console.error('Error parsing user data', e);
        localStorage.removeItem('currentUser');
      }
    } else if (token && !this.jwtHelper.isTokenExpired(token)) {
      // If we have a valid token but no user data, fetch it
      this.fetchUserProfile();
    }
  }

  private fetchUserProfileOnce(): void {
    if (this.isFetchingProfile) {
      return;
    }
    
    this.fetchUserProfile();
  }

  private fetchUserProfile(): void {
    this.isFetchingProfile = true;
    
    this.http.get<ApiResponse<User>>('http://localhost:8080/api/user/profile')
      .subscribe({
        next: (response) => {
          this.isFetchingProfile = false;
          if (response.success && response.data) {
            console.log('AuthService - Profile fetched:', response.data);
            localStorage.setItem('currentUser', JSON.stringify(response.data));
            this.currentUserSubject.next(response.data);
          }
        },
        error: (err) => {
          this.isFetchingProfile = false;
          console.error('Failed to fetch user profile:', err);
        }
      });
  }

  getCurrentUser(): User | null {
    // Return the current user from the subject if available
    if (this.currentUserValue) {
      return this.currentUserValue;
    }
    
    // Otherwise, try to get it from localStorage
    const userJson = localStorage.getItem('currentUser');
    if (userJson) {
      try {
        const user = JSON.parse(userJson);
        this.currentUserSubject.next(user);
        return user;
      } catch (e) {
        console.error('Error parsing user data from localStorage', e);
        localStorage.removeItem('currentUser');
      }
    }
    
    return null;
  }

  refreshUserProfile(): void {
    this.fetchUserProfile();
  }
}
