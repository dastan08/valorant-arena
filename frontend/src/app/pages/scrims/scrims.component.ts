import { Component, OnInit } from '@angular/core';
import { ScrimService } from '../../core/services/scrim.service';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-scrims',
  templateUrl: './scrims.component.html',
  styleUrls: ['./scrims.component.css']
})
export class ScrimsComponent implements OnInit {
  pendingScrims: any[] = [];
  upcomingScrims: any[] = [];
  completedScrims: any[] = [];
  currentUser: any;
  loading = false;
  error: string | null = null;
  activeTab: 'pending' | 'upcoming' | 'completed' = 'pending';

  constructor(
    private scrimService: ScrimService,
    private authService: AuthService,
    private notificationService: NotificationService
  ) {
    this.currentUser = this.authService.currentUserValue;
  }

  ngOnInit(): void {
    // Force refresh user profile first
    this.authService.refreshUserProfile();
    
    // Subscribe to user changes to get updated team info
    this.authService.currentUser.subscribe(user => {
      console.log('User updated in scrims component:', user);
      console.log('User team:', user?.team);
      console.log('User teamRole:', user?.teamRole);
      this.currentUser = user;
      if (this.currentUser?.team) {
        this.error = null; // Clear any previous error
        this.loadScrims();
      } else {
        this.error = 'You must be in a team to view scrims';
      }
    });
  }

  loadScrims(): void {
    console.log('loadScrims called, currentUser:', this.currentUser);
    console.log('currentUser.team:', this.currentUser?.team);
    
    if (!this.currentUser?.team) {
      this.error = 'You must be in a team to view scrims';
      console.log('No team found for user');
      return;
    }

    this.loading = true;
    console.log('Fetching scrims for team ID:', this.currentUser.team.id);
    this.scrimService.getTeamScrims(this.currentUser.team.id).subscribe({
      next: (response) => {
        console.log('Scrims API response:', response);
        if (response.success) {
          const scrims = response.data;
          console.log('All scrims:', scrims);
          this.pendingScrims = scrims.filter((s: any) => s.status === 'PENDING');
          this.upcomingScrims = scrims.filter((s: any) => s.status === 'ACCEPTED');
          this.completedScrims = scrims.filter((s: any) =>
            s.status === 'COMPLETED' || s.status === 'DECLINED' || s.status === 'CANCELLED'
          );
          console.log('Pending scrims:', this.pendingScrims);
          console.log('Upcoming scrims:', this.upcomingScrims);
          console.log('Completed scrims:', this.completedScrims);
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading scrims:', err);
        this.error = 'Failed to load scrims';
        this.loading = false;
      }
    });
  }

  acceptScrim(scrimId: number): void {
    if (!this.canManageTeam()) {
      alert('Only captain or manager can accept scrims');
      return;
    }

    const scrim = this.pendingScrims.find(s => s.id === scrimId);
    this.scrimService.acceptScrim(scrimId).subscribe({
      next: (response) => {
        if (response.success) {
          alert('Scrim accepted!');
          // Create notification for the challenger team
          if (scrim) {
            this.notificationService.notifyScrimAccepted(
              this.getOpponentTeam(scrim).name,
              scrimId
            );
          }
          this.loadScrims();
        }
      },
      error: (err) => {
        console.error('Error accepting scrim:', err);
        alert('Failed to accept scrim');
      }
    });
  }

  declineScrim(scrimId: number): void {
    if (!this.canManageTeam()) {
      alert('Only captain or manager can decline scrims');
      return;
    }

    const scrim = this.pendingScrims.find(s => s.id === scrimId);
    this.scrimService.declineScrim(scrimId).subscribe({
      next: (response) => {
        if (response.success) {
          alert('Scrim declined');
          // Create notification for the challenger team
          if (scrim) {
            this.notificationService.notifyScrimDeclined(
              this.getOpponentTeam(scrim).name,
              scrimId
            );
          }
          this.loadScrims();
        }
      },
      error: (err) => {
        console.error('Error declining scrim:', err);
        alert('Failed to decline scrim');
      }
    });
  }

  cancelScrim(scrimId: number): void {
    if (!this.canManageTeam()) {
      alert('Only captain or manager can cancel scrims');
      return;
    }

    if (!confirm('Are you sure you want to cancel this scrim?')) {
      return;
    }

    this.scrimService.cancelScrim(scrimId).subscribe({
      next: (response) => {
        if (response.success) {
          alert('Scrim cancelled');
          this.loadScrims();
        }
      },
      error: (err) => {
        console.error('Error cancelling scrim:', err);
        alert('Failed to cancel scrim');
      }
    });
  }

  canManageTeam(): boolean {
    if (!this.currentUser?.team) {
      console.log('canManageTeam: No team found for user');
      return false;
    }
    
    const role = this.currentUser.teamRole;
    const team = this.currentUser.team;
    const userId = this.currentUser.id;
    
    console.log('canManageTeam: Detailed check:', {
      userId: userId,
      teamRole: role,
      team: team,
      teamCaptain: team.captain,
      teamManager: team.manager,
      teamCaptainId: team.captain?.id,
      teamManagerId: team.manager?.id
    });
    
    // Check explicit role first
    if (role === 'CAPTAIN' || role === 'MANAGER') {
      console.log('canManageTeam: Can manage via explicit role');
      return true;
    }
    
    // Fallback: Check if user is captain or manager based on team structure
    // Check if user is the team captain
    if (team.captain && team.captain.id === userId) {
      console.log('canManageTeam: Can manage as team captain');
      return true;
    }
    
    // Check if user is the team manager
    if (team.manager && team.manager.id === userId) {
      console.log('canManageTeam: Can manage as team manager');
      return true;
    }
    
    console.log('canManageTeam: Cannot manage team - no matching role or captain/manager status');
    return false;
  }

  isChallenger(scrim: any): boolean {
    const isChallenger = scrim.challengerTeam.id === this.currentUser?.team?.id;
    console.log('isChallenger check:', {
      challengerTeamId: scrim.challengerTeam.id,
      userTeamId: this.currentUser?.team?.id,
      isChallenger: isChallenger
    });
    return isChallenger;
  }

  getOpponentTeam(scrim: any): any {
    return this.isChallenger(scrim) ? scrim.opponentTeam : scrim.challengerTeam;
  }
}
