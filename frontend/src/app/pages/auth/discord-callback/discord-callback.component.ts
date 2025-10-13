import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-discord-callback',
  template: `
    <div class="min-h-screen bg-valorant-dark flex items-center justify-center">
      <div class="text-center">
        <div class="animate-spin rounded-full h-16 w-16 border-b-2 border-valorant-red mx-auto"></div>
        <p class="mt-4 text-white text-lg">Completing Discord login...</p>
      </div>
    </div>
  `
})
export class DiscordCallbackComponent implements OnInit {
  private apiUrl = 'http://localhost:8080/api/auth';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private toastr: ToastrService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    // Get authorization code from URL
    const code = this.route.snapshot.queryParams['code'];
    const error = this.route.snapshot.queryParams['error'];

    if (error) {
      this.toastr.error('Discord authorization failed', 'Error');
      this.router.navigate(['/login']);
      return;
    }

    if (!code) {
      this.toastr.error('No authorization code received', 'Error');
      this.router.navigate(['/login']);
      return;
    }

    // Send code to backend
    console.log('Sending code to backend:', code);
    this.http.post<any>(`${this.apiUrl}/discord/callback`, { code }).subscribe({
      next: (response) => {
        console.log('Backend response:', response);
        if (response.success && response.data && response.data.token) {
          console.log('Token received:', response.data.token.substring(0, 20) + '...');
          
          // Store JWT token
          localStorage.setItem('token', response.data.token);
          
          // Verify token was stored
          const storedToken = localStorage.getItem('token');
          console.log('Token stored successfully:', storedToken ? 'Yes' : 'No');
          console.log('Is authenticated:', this.authService.isAuthenticated());
          
          // Handle auth response through AuthService
          this.authService.handleAuthResponse(response.data);
          
          this.toastr.success('Login successful!', 'Welcome ' + response.data.username);
          
          // Redirect to dashboard after a short delay to ensure token is processed
          setTimeout(() => {
            console.log('Navigating to dashboard...');
            this.router.navigate(['/dashboard']).then(success => {
              console.log('Navigation result:', success);
            });
          }, 1000);
        } else {
          console.error('Login failed - invalid response:', response);
          this.toastr.error('Login failed - invalid response', 'Error');
          this.router.navigate(['/login']);
        }
      },
      error: (error) => {
        console.error('Discord login error:', error);
        this.toastr.error(error.error?.message || 'Discord login failed', 'Error');
        this.router.navigate(['/login']);
      }
    });
  }
}
