import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { TournamentService } from '../../../core/services/tournament.service';
import { Tournament, TournamentStatus } from '../../../core/models/tournament.model';
import { AssignModeratorDialogComponent } from '../assign-moderator-dialog/assign-moderator-dialog.component';
import { TournamentModeratorService, TournamentModeratorResponse } from '../../../core/services/tournament-moderator.service';

@Component({
  selector: 'app-organizer-dashboard',
  templateUrl: './organizer-dashboard.component.html',
  styleUrls: ['./organizer-dashboard.component.css']
})
export class OrganizerDashboardComponent implements OnInit {
  tournaments: Tournament[] = [];
  loading = false;
  selectedTab: 'all' | 'draft' | 'active' | 'completed' = 'all';
  tournamentModerators: { [key: number]: TournamentModeratorResponse[] } = {};

  constructor(
    private tournamentService: TournamentService,
    private moderatorService: TournamentModeratorService,
    private router: Router,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadTournaments();
  }

  loadTournaments(): void {
    this.loading = true;
    this.tournamentService.getMyTournaments().subscribe({
      next: (response) => {
        if (response.success) {
          this.tournaments = response.data;
          // Load moderators for each tournament
          this.tournaments.forEach(t => this.loadModerators(t.id));
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  get filteredTournaments(): Tournament[] {
    switch (this.selectedTab) {
      case 'draft':
        return this.tournaments.filter(t => t.status === TournamentStatus.DRAFT);
      case 'active':
        return this.tournaments.filter(t => 
          t.status === TournamentStatus.OPEN || 
          t.status === TournamentStatus.REGISTRATION_OPEN || 
          t.status === TournamentStatus.CHECK_IN ||
          t.status === TournamentStatus.ONGOING ||
          t.status === TournamentStatus.IN_PROGRESS
        );
      case 'completed':
        return this.tournaments.filter(t => t.status === TournamentStatus.COMPLETED);
      default:
        return this.tournaments;
    }
  }

  createTournament(): void {
    this.router.navigate(['/tournaments/create']);
  }

  editTournament(id: number): void {
    // TODO: Implement edit functionality
    console.log('Edit tournament:', id);
    // Prevent default navigation
    return;
  }

  viewTournament(id: number): void {
    this.router.navigate(['/tournaments', id]);
  }

  manageTournament(id: number): void {
    this.router.navigate(['/tournaments', id, 'manage']);
  }

  generateBracket(tournament: Tournament): void {
    if (confirm('Generate bracket for this tournament? This cannot be undone.')) {
      // Call bracket generation API
      console.log('Generating bracket for tournament:', tournament.id);
    }
  }

  publishTournament(tournament: Tournament): void {
    if (confirm('Publish this tournament? Registration will open and players can start registering.')) {
      this.tournamentService.publishTournament(tournament.id).subscribe({
        next: (response) => {
          if (response.success) {
            alert('Tournament published successfully!');
            this.loadTournaments(); // Reload to show updated status
          }
        },
        error: (error) => {
          alert('Failed to publish tournament: ' + (error.error?.message || 'Unknown error'));
        }
      });
    }
  }

  cancelTournament(tournament: Tournament): void {
    if (confirm('Are you sure you want to cancel this tournament?')) {
      // Call cancel API
      console.log('Cancelling tournament:', tournament.id);
    }
  }

  openAssignModeratorDialog(tournament: Tournament): void {
    const dialogRef = this.dialog.open(AssignModeratorDialogComponent, {
      width: '500px',
      panelClass: 'modern-dialog-panel',
      backdropClass: 'modern-dialog-backdrop',
      data: { tournamentId: tournament.id }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === 'assigned') {
        this.loadModerators(tournament.id);
      }
    });
  }

  loadModerators(tournamentId: number): void {
    this.moderatorService.getTournamentModerators(tournamentId).subscribe({
      next: (response) => {
        if (response.success) {
          this.tournamentModerators[tournamentId] = response.data;
        }
      },
      error: (err) => {
        console.error('Error loading moderators:', err);
      }
    });
  }

  removeModerator(tournamentId: number, userId: number): void {
    if (confirm('Remove this moderator from the tournament?')) {
      this.moderatorService.removeModerator(tournamentId, userId).subscribe({
        next: (response) => {
          if (response.success) {
            this.loadModerators(tournamentId);
          }
        },
        error: (err) => {
          console.error('Error removing moderator:', err);
        }
      });
    }
  }

  getModerators(tournamentId: number): TournamentModeratorResponse[] {
    return this.tournamentModerators[tournamentId] || [];
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'DRAFT':
        return 'text-gray-500';
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

  getStatusBadge(status: string): string {
    switch (status) {
      case 'DRAFT':
        return 'bg-gray-600';
      case 'REGISTRATION_OPEN':
      case 'OPEN':
        return 'bg-green-600';
      case 'IN_PROGRESS':
      case 'ONGOING':
        return 'bg-yellow-600';
      case 'COMPLETED':
        return 'bg-blue-600';
      case 'CANCELLED':
        return 'bg-red-600';
      default:
        return 'bg-gray-600';
    }
  }
}
