import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TournamentService } from '../../core/services/tournament.service';
import { Tournament } from '../../core/models/tournament.model';

@Component({
  selector: 'app-tournaments',
  templateUrl: './tournaments.component.html',
  styleUrls: ['./tournaments.component.css']
})
export class TournamentsComponent implements OnInit {
  tournaments: Tournament[] = [];
  loading = false;

  constructor(
    private tournamentService: TournamentService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadTournaments();
  }

  loadTournaments(): void {
    this.loading = true;
    this.tournamentService.getUpcomingTournaments().subscribe({
      next: (response) => {
        if (response.success) {
          this.tournaments = response.data;
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  viewTournamentDetails(tournamentId: number): void {
    this.router.navigate(['/tournaments', tournamentId]);
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'UPCOMING':
      case 'REGISTRATION_OPEN':
        return 'text-green-500';
      case 'IN_PROGRESS':
        return 'text-yellow-500';
      case 'COMPLETED':
        return 'text-blue-500';
      case 'CANCELLED':
        return 'text-red-500';
      default:
        return 'text-gray-500';
    }
  }
}
