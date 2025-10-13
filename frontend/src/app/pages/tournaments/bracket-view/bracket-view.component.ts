import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TournamentService } from '../../../core/services/tournament.service';
import { Tournament, Match } from '../../../core/models/tournament.model';

interface BracketRound {
  roundNumber: number;
  matches: Match[];
}

@Component({
  selector: 'app-bracket-view',
  templateUrl: './bracket-view.component.html',
  styleUrls: ['./bracket-view.component.css']
})
export class BracketViewComponent implements OnInit {
  tournament: Tournament | null = null;
  brackets: BracketRound[] = [];
  loading = false;

  constructor(
    private route: ActivatedRoute,
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
          this.organizeBracket();
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  organizeBracket(): void {
    if (!this.tournament || !this.tournament.matches) return;

    const roundMap = new Map<number, Match[]>();
    
    this.tournament.matches.forEach(match => {
      if (!roundMap.has(match.round)) {
        roundMap.set(match.round, []);
      }
      roundMap.get(match.round)!.push(match);
    });

    this.brackets = Array.from(roundMap.entries())
      .sort((a, b) => a[0] - b[0])
      .map(([roundNumber, matches]) => ({
        roundNumber,
        matches: matches.sort((a, b) => a.id - b.id)
      }));
  }

  getRoundName(roundNumber: number): string {
    if (!this.tournament) return `Round ${roundNumber}`;
    
    const totalRounds = this.brackets.length;
    const roundsFromEnd = totalRounds - roundNumber;

    if (roundsFromEnd === 0) return 'Finals';
    if (roundsFromEnd === 1) return 'Semi-Finals';
    if (roundsFromEnd === 2) return 'Quarter-Finals';
    
    return `Round ${roundNumber}`;
  }

  getMatchStatusColor(status: string): string {
    switch (status) {
      case 'SCHEDULED':
        return 'border-gray-600';
      case 'IN_PROGRESS':
        return 'border-yellow-500';
      case 'COMPLETED':
        return 'border-green-500';
      case 'DISPUTED':
        return 'border-red-500';
      default:
        return 'border-gray-600';
    }
  }

  getWinnerClass(match: Match, teamId: number): string {
    if (match.status !== 'COMPLETED' || !match.winner) return '';
    return match.winner.id === teamId ? 'font-bold text-green-500' : 'text-gray-500';
  }
}
