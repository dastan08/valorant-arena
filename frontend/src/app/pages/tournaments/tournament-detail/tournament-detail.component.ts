import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { TournamentService } from '../../../core/services/tournament.service';
import { AuthService } from '../../../core/services/auth.service';
import { Tournament } from '../../../core/models/tournament.model';
import { User } from '../../../core/models/user.model';
import { TeamDetailDialogComponent } from '../../teams/team-detail-dialog/team-detail-dialog.component';

@Component({
  selector: 'app-tournament-detail',
  templateUrl: './tournament-detail.component.html',
  styleUrls: ['./tournament-detail.component.css']
})
export class TournamentDetailComponent implements OnInit {
  tournament: Tournament | null = null;
  loading = false;
  registering = false;
  isRegistered = false;
  canRegister = false;
  currentUserId: number | null = null;
  
  // Modal state
  showRegistrationModal = false;
  userTeams: any[] = [];
  selectedTeamId: number | null = null;
  loadingTeams = false;

  constructor(
    private route: ActivatedRoute,
    public router: Router,
    private tournamentService: TournamentService,
    private authService: AuthService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadTournament(parseInt(id));
    }
    
    this.authService.currentUser.subscribe((user: User | null) => {
      this.currentUserId = user?.id || null;
    });
  }

  loadTournament(id: number): void {
    this.loading = true;
    this.tournamentService.getTournamentById(id).subscribe({
      next: (response) => {
        console.log('Tournament API Response:', response);
        if (response.success) {
          this.tournament = response.data;
          console.log('Tournament loaded:', this.tournament);
          this.checkRegistrationStatus();
          this.checkCanRegister();
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading tournament:', error);
        this.loading = false;
      }
    });
  }

  checkRegistrationStatus(): void {
    // Check if current user/team is registered
    if (this.tournament && this.currentUserId) {
      this.isRegistered = this.tournament.registeredTeams?.some(
        team => team.members?.some(member => member.id === this.currentUserId)
      ) || false;
    }
  }

  checkCanRegister(): void {
    if (!this.tournament) return;
    
    const registeredCount = this.tournament.registeredTeams?.length || 0;
    const maxTeams = this.tournament.maxTeams || this.tournament.maxParticipants || 0;
    
    console.log('Check can register:', {
      status: this.tournament.status,
      isRegistered: this.isRegistered,
      registeredCount,
      maxTeams,
      currentUserId: this.currentUserId
    });
    
    this.canRegister = 
      (this.tournament.status === 'OPEN' || this.tournament.status === 'REGISTRATION_OPEN') &&
      !this.isRegistered &&
      registeredCount < maxTeams;
      
    console.log('Can register:', this.canRegister);
  }

  register(): void {
    if (!this.tournament || !this.canRegister) return;

    // Open the registration modal
    this.openRegistrationModal();
  }
  
  openRegistrationModal(): void {
    this.showRegistrationModal = true;
    this.selectedTeamId = null;
    // Load user's teams
    this.loadUserTeams();
  }
  
  closeRegistrationModal(): void {
    this.showRegistrationModal = false;
    this.selectedTeamId = null;
  }
  
  loadUserTeams(): void {
    this.loadingTeams = true;
    
    // Get current user's team from auth service
    this.authService.currentUser.subscribe((user: User | null) => {
      if (user && user.team) {
        this.userTeams = [user.team];
      } else {
        this.userTeams = [];
      }
      this.loadingTeams = false;
    });
  }
  
  registerAsSolo(): void {
    alert('Solo registrations are not applicable for this tournament.\n\nPlease create or join a team to participate.');
    this.closeRegistrationModal();
  }
  
  registerAsTeam(): void {
    if (!this.tournament || !this.selectedTeamId) {
      alert('Please select a team');
      return;
    }
    
    this.registering = true;
    
    const registrationData = {
      entryType: 'TEAM',
      teamId: this.selectedTeamId
    };
    
    console.log('Registering team for tournament:', this.tournament.id, registrationData);
    
    this.tournamentService.registerForTournament(this.tournament.id, registrationData).subscribe({
      next: (response) => {
        console.log('Registration response:', response);
        if (response.success) {
          alert('Successfully registered your team for the tournament!');
          this.isRegistered = true;
          this.canRegister = false;
          this.closeRegistrationModal();
          // Reload tournament data
          this.loadTournament(this.tournament!.id);
        }
        this.registering = false;
      },
      error: (error) => {
        console.error('Registration error:', error);
        const errorMessage = error.error?.message || 'Unknown error';
        alert('Failed to register: ' + errorMessage);
        this.registering = false;
      }
    });
  }

  navigateToTeam(teamId: number): void {
    // Open team detail dialog with minimal team object
    this.dialog.open(TeamDetailDialogComponent, {
      width: '800px',
      maxWidth: '95vw',
      data: { 
        team: { 
          id: teamId 
        } 
      }
    });
  }

  onImageError(event: any, team: any): void {
    // If image fails to load, mark it as default so the fallback shows
    team.logo = 'default-team-logo.png';
  }

  unregister(): void {
    if (!this.tournament || !this.isRegistered) return;

    const teamId = 1; // This should come from user's registered team
    
    this.tournamentService.unregisterTeam(this.tournament.id, teamId).subscribe({
      next: (response) => {
        if (response.success) {
          this.tournament = response.data;
          this.isRegistered = false;
          this.checkCanRegister();
        }
      },
      error: () => {}
    });
  }

  viewBracket(): void {
    if (this.tournament) {
      this.router.navigate(['/tournaments', this.tournament.id, 'bracket']);
    }
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'REGISTRATION_OPEN':
      case 'OPEN':
        return 'text-green-500';
      case 'IN_PROGRESS':
      case 'ONGOING':
        return 'text-yellow-500';
      case 'COMPLETED':
        return 'text-blue-500';
      case 'CANCELLED':
        return 'text-red-500';
      default:
        return 'text-gray-500';
    }
  }

  getStatusBadgeColor(status: string): string {
    switch (status) {
      case 'REGISTRATION_OPEN':
      case 'OPEN':
        return 'bg-green-500';
      case 'IN_PROGRESS':
      case 'ONGOING':
        return 'bg-yellow-500';
      case 'COMPLETED':
        return 'bg-blue-500';
      case 'CANCELLED':
        return 'bg-red-500';
      default:
        return 'bg-gray-500';
    }
  }
}
