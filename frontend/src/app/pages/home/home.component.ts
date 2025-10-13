import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TournamentService } from '../../core/services/tournament.service';
import { Tournament } from '../../core/models/tournament.model';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  upcomingTournaments: Tournament[] = [];
  loading = false;

  constructor(
    private tournamentService: TournamentService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadUpcomingTournaments();
  }

  loadUpcomingTournaments(): void {
    this.loading = true;
    this.tournamentService.getUpcomingTournaments().subscribe({
      next: (response) => {
        if (response.success) {
          this.upcomingTournaments = response.data.slice(0, 3);
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  navigateToTournaments(): void {
    this.router.navigate(['/tournaments']);
  }
}
