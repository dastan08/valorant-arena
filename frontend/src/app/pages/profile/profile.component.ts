import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../core/services/auth.service';
import { User } from '../../core/models/user.model';
import { ValorantService } from '../../services/valorant.service';
import { environment } from '../../core/config/environment';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  private apiUrl = environment.apiUrl;
  currentUser: User | null = null;
  showLinkRiotModal = false;
  valorantId = '';
  isLinking = false;
  linkError = '';
  linkSuccess = '';
  
  // Valorant stats
  playerStats: any = null;
  matchHistory: any[] = [];
  loadingStats = false;
  loadingMatches = false;

  constructor(
    private authService: AuthService,
    private http: HttpClient,
    private valorantService: ValorantService
  ) {}

  ngOnInit(): void {
    this.loadUserProfile();
  }

  loadUserProfile(): void {
    this.http.get<any>(`${this.apiUrl}/user/profile`).subscribe({
      next: (response) => {
        if (response.success) {
          this.currentUser = response.data;
          // Load Valorant stats if Riot ID is linked
          if (this.currentUser?.valorantId && !this.currentUser.valorantId.startsWith('NOT_LINKED')) {
            this.loadValorantStats();
            this.loadMatchHistory();
          }
        }
      },
      error: (error) => {
        console.error('Error loading profile:', error);
      }
    });
  }
  
  loadValorantStats(): void {
    if (!this.currentUser?.valorantId) return;
    
    this.loadingStats = true;
    
    // Use the new backend endpoint that fetches from Henrik API
    this.http.get<any>(`${this.apiUrl}/user/valorant-stats`).subscribe({
      next: (response) => {
        this.loadingStats = false;
        if (response.success && response.data) {
          this.playerStats = response.data;
          console.log('Valorant stats loaded:', this.playerStats);
        } else {
          console.log('No Valorant stats available');
        }
      },
      error: (error) => {
        this.loadingStats = false;
        console.error('Error loading Valorant stats:', error);
      }
    });
  }
  
  loadMatchHistory(): void {
    if (!this.currentUser?.valorantId) return;
    
    this.loadingMatches = true;
    
    // Use the new backend endpoint that fetches matches
    this.http.get<any>(`${this.apiUrl}/user/valorant-matches?mode=competitive&size=10`).subscribe({
      next: (response) => {
        this.loadingMatches = false;
        if (response.success && response.data) {
          this.matchHistory = response.data;
          console.log('Match history loaded:', this.matchHistory);
        } else {
          console.log('No match history available');
        }
      },
      error: (error) => {
        this.loadingMatches = false;
        console.error('Error loading match history:', error);
      }
    });
  }

  openLinkRiotModal(): void {
    this.showLinkRiotModal = true;
    this.valorantId = '';
    this.linkError = '';
    this.linkSuccess = '';
  }

  closeLinkRiotModal(): void {
    this.showLinkRiotModal = false;
    this.valorantId = '';
    this.linkError = '';
    this.linkSuccess = '';
  }

  linkRiotAccount(): void {
    if (!this.valorantId.trim()) {
      this.linkError = 'Please enter your Valorant ID';
      return;
    }

    if (!this.valorantId.includes('#')) {
      this.linkError = 'Valorant ID must be in format: GameName#TAG';
      return;
    }

    this.isLinking = true;
    this.linkError = '';
    this.linkSuccess = '';

    this.http.post<any>(`${this.apiUrl}/user/link-riot-account`, {
      valorantId: this.valorantId
    }).subscribe({
      next: (response) => {
        this.isLinking = false;
        if (response.success) {
          this.linkSuccess = 'Riot account linked successfully!';
          this.currentUser = response.data;
          setTimeout(() => {
            this.closeLinkRiotModal();
          }, 2000);
        }
      },
      error: (error) => {
        this.isLinking = false;
        this.linkError = error.error?.message || 'Failed to link Riot account';
      }
    });
  }

  unlinkRiotAccount(): void {
    if (!confirm('Are you sure you want to unlink your Riot account?')) {
      return;
    }

    this.http.delete<any>(`${this.apiUrl}/user/unlink-riot-account`).subscribe({
      next: (response) => {
        if (response.success) {
          this.currentUser = response.data;
          alert('Riot account unlinked successfully');
        }
      },
      error: (error) => {
        alert(error.error?.message || 'Failed to unlink Riot account');
      }
    });
  }

  linkDiscordAccount(): void {
    // Redirect to Discord OAuth
    const discordClientId = '1318959803062366288'; // Your Discord OAuth client ID
    const redirectUri = encodeURIComponent('http://localhost:4200/auth/discord/callback');
    const scope = encodeURIComponent('identify email');
    const discordAuthUrl = `https://discord.com/api/oauth2/authorize?client_id=${discordClientId}&redirect_uri=${redirectUri}&response_type=code&scope=${scope}`;
    
    window.location.href = discordAuthUrl;
  }
}
