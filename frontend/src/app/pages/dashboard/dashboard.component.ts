import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatDialog } from '@angular/material/dialog';
import { AuthService } from '../../core/services/auth.service';
import { User } from '../../core/models/user.model';
import { TeamDetailDialogComponent } from '../teams/team-detail-dialog/team-detail-dialog.component';
import { environment } from '../../core/config/environment';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  private apiUrl = environment.apiUrl;
  currentUser: User | null = null;
  loading = false;

  constructor(
    private authService: AuthService,
    private http: HttpClient,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    // First, set the cached user immediately
    this.currentUser = this.authService.currentUserValue;
    console.log('Initial cached user:', this.currentUser);
    console.log('User team:', this.currentUser?.team);
    
    // Then try to load fresh profile
    this.loadUserProfile();
  }

  loadUserProfile(): void {
    this.loading = true;
    this.http.get<any>(`${this.apiUrl}/user/profile`).subscribe({
      next: (response) => {
        if (response.success) {
          this.currentUser = response.data;
          console.log('User profile loaded:', this.currentUser);
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading profile:', err);
        console.error('Error status:', err.status);
        console.error('Error message:', err.message);
        console.error('Error details:', err.error);
        // Fallback to cached user
        this.currentUser = this.authService.currentUserValue;
        this.loading = false;
      }
    });
  }

  getWinRate(): number {
    if (!this.currentUser || this.currentUser.stats.matchesPlayed === 0) {
      return 0;
    }
    return (this.currentUser.stats.wins / this.currentUser.stats.matchesPlayed) * 100;
  }

  getKDA(): string {
    if (!this.currentUser || this.currentUser.stats.deaths === 0) {
      return '0.00';
    }
    const kda = (this.currentUser.stats.kills + this.currentUser.stats.assists) / this.currentUser.stats.deaths;
    return kda.toFixed(2);
  }

  openTeamDetail(): void {
    if (!this.currentUser?.team) {
      return;
    }

    this.dialog.open(TeamDetailDialogComponent, {
      width: '800px',
      maxWidth: '95vw',
      data: { team: this.currentUser.team },
      panelClass: 'custom-dialog-container'
    });
  }
}
