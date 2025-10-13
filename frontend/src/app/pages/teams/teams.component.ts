import { Component, OnDestroy, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatDialog } from '@angular/material/dialog';
import { TeamService } from '../../core/services/team.service';
import { Team } from '../../core/models/user.model';
import { CreateTeamDialogComponent } from './create-team-dialog/create-team-dialog.component';
import { TeamDetailDialogComponent } from './team-detail-dialog/team-detail-dialog.component';
import { AuthService } from '../../core/services/auth.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-teams',
  templateUrl: './teams.component.html',
  styleUrls: ['./teams.component.css']
})
export class TeamsComponent implements OnInit, OnDestroy {
  private apiUrl = 'http://localhost:8080/api';
  teams: any[] = [];
  loading = false;
  currentUser: any;
  private subscriptions = new Subscription();

  constructor(
    private teamService: TeamService,
    private authService: AuthService,
    private dialog: MatDialog,
    private http: HttpClient
  ) {
    this.currentUser = this.authService.currentUserValue ?? this.authService.getCurrentUser();
  }

  ngOnInit(): void {
    this.subscriptions.add(
      this.authService.currentUser.subscribe(user => {
        this.currentUser = user;
      })
    );
    this.loadUserProfile();
    this.loadTeams();
  }

  loadUserProfile(): void {
    this.http.get<any>(`${this.apiUrl}/user/profile`).subscribe({
      next: (response) => {
        if (response.success) {
          this.currentUser = response.data;
          console.log('User profile loaded in teams:', this.currentUser);
          console.log('User team:', this.currentUser?.team);
        }
      },
      error: (err) => {
        console.error('Error loading profile in teams:', err);
      }
    });
  }

  computeTeamLogoUrl(logo: string): string {
    if (!logo || logo === 'default-team-logo.png') {
      return 'assets/default-team-logo.png';
    }
    if (logo.startsWith('http')) {
      return logo;
    }
    const timestamp = new Date().getTime();
    return `http://localhost:8080${logo}?t=${timestamp}`;
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  loadTeams(): void {
    this.loading = true;
    this.teamService.getAllTeams().subscribe({
      next: (response) => {
        console.log('Teams response:', response);
        if (response.success) {
          this.teams = response.data.map((team: any) => ({
            ...team,
            logoUrl: this.computeTeamLogoUrl(team.logo)
          }));
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading teams:', err);
        console.error('Error status:', err.status);
        console.error('Error message:', err.message);
        console.error('Error details:', err.error);
        this.loading = false;
      }
    });
  }

  openCreateTeamDialog(): void {
    if (!this.currentUser) {
      // Handle user not logged in
      return;
    }

    const dialogRef = this.dialog.open(CreateTeamDialogComponent, {
      width: '550px',
      maxWidth: '95vw',
      maxHeight: '90vh',
      panelClass: 'modern-dialog-panel',
      hasBackdrop: true,
      disableClose: false,
      backdropClass: 'modern-dialog-backdrop',
      data: { userId: this.currentUser.id }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.loadTeams(); // Refresh the teams list
        this.authService.refreshUserProfile(); // Refresh user profile to get team info
        this.loadUserProfile(); // Also reload local user profile
      }
    });
  }

  viewTeam(team: Team): void {
    const dialogRef = this.dialog.open(TeamDetailDialogComponent, {
      width: '700px',
      maxWidth: '95vw',
      maxHeight: '90vh',
      panelClass: 'modern-dialog-panel',
      hasBackdrop: true,
      disableClose: false,
      backdropClass: 'modern-dialog-backdrop',
      data: { team: team }
    });

    dialogRef.afterClosed().subscribe(() => {
      this.loadTeams(); // Refresh teams list after dialog closes
      this.authService.refreshUserProfile(); // Refresh user profile to get team info
      this.loadUserProfile(); // Also reload local user profile
    });
  }

  getMemberAvatar(member: any): string {
    // Priority: Discord avatar > Custom avatar > First letter
    if (member.discordId && member.discordAvatar) {
      return `https://cdn.discordapp.com/avatars/${member.discordId}/${member.discordAvatar}.png`;
    }
    return ''; // Return empty to show letter avatar
  }

  getMemberInitial(member: any): string {
    return (member.username || 'U')[0].toUpperCase();
  }
}
