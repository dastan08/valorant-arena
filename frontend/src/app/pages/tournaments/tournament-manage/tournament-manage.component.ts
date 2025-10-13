import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TournamentService } from '../../../core/services/tournament.service';
import { Tournament, TournamentStatus } from '../../../core/models/tournament.model';

@Component({
  selector: 'app-tournament-manage',
  templateUrl: './tournament-manage.component.html',
  styleUrls: ['./tournament-manage.component.css']
})
export class TournamentManageComponent implements OnInit {
  tournament: Tournament | null = null;
  loading = false;
  selectedTab: 'overview' | 'participants' | 'matches' | 'settings' = 'overview';
  generatingBracket = false;

  TournamentStatus = TournamentStatus;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private tournamentService: TournamentService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadTournament(parseInt(id));
    }
  }

  loadTournament(id: number): void {
    this.loading = true;
    this.tournamentService.getTournamentById(id).subscribe({
      next: (response) => {
        if (response.success) {
          this.tournament = response.data;
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  generateBracket(): void {
    if (!this.tournament) return;
    
    if (confirm('Generate bracket for this tournament? This cannot be undone.')) {
      this.generatingBracket = true;
      this.tournamentService.generateBracket(this.tournament.id).subscribe({
        next: (response) => {
          if (response.success) {
            this.loadTournament(this.tournament!.id);
          }
          this.generatingBracket = false;
        },
        error: () => {
          this.generatingBracket = false;
        }
      });
    }
  }

  viewBracket(): void {
    if (this.tournament) {
      this.router.navigate(['/tournaments', this.tournament.id, 'bracket']);
    }
  }

  disqualifyParticipant(participantId: number): void {
    if (confirm('Are you sure you want to disqualify this participant?')) {
      // TODO: Implement disqualification
      console.log('Disqualifying participant:', participantId);
    }
  }

  cancelMatch(matchId: number): void {
    if (confirm('Are you sure you want to cancel this match?')) {
      // TODO: Implement match cancellation
      console.log('Cancelling match:', matchId);
    }
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'DRAFT':
        return 'text-gray-500';
      case 'OPEN':
      case 'REGISTRATION_OPEN':
        return 'text-green-500';
      case 'CHECK_IN':
        return 'text-blue-500';
      case 'ONGOING':
      case 'IN_PROGRESS':
        return 'text-yellow-500';
      case 'COMPLETED':
        return 'text-blue-500';
      default:
        return 'text-gray-500';
    }
  }

  getCheckedInCount(): number {
    return this.tournament?.registeredTeams?.filter(t => (t as any).checkedIn).length || 0;
  }

  getParticipantStatus(participant: any): string {
    if (participant.checkedIn) return 'Checked In';
    if (participant.disqualified) return 'Disqualified';
    return 'Registered';
  }

  getParticipantStatusColor(participant: any): string {
    if (participant.checkedIn) return 'text-green-500';
    if (participant.disqualified) return 'text-red-500';
    return 'text-gray-400';
  }
}
