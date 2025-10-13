import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TournamentService } from '../../../core/services/tournament.service';
import { AuthService } from '../../../core/services/auth.service';
import { Match, MatchStatus } from '../../../core/models/tournament.model';

@Component({
  selector: 'app-match-detail',
  templateUrl: './match-detail.component.html',
  styleUrls: ['./match-detail.component.css']
})
export class MatchDetailComponent implements OnInit {
  match: Match | null = null;
  loading = false;
  reportingScore = false;
  confirmingScore = false;
  startingMatch = false;
  currentUserId: number | null = null;
  
  scoreForm: FormGroup;
  MatchStatus = MatchStatus;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private tournamentService: TournamentService,
    private authService: AuthService
  ) {
    this.scoreForm = this.fb.group({
      team1Score: [0, [Validators.required, Validators.min(0)]],
      team2Score: [0, [Validators.required, Validators.min(0)]],
      screenshotUrls: [[]],
      matchReplayUrl: [''],
      notes: ['']
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadMatch(parseInt(id));
    }
    
    this.authService.currentUser$.subscribe(user => {
      this.currentUserId = user?.id || null;
    });
  }

  loadMatch(id: number): void {
    this.loading = true;
    // TODO: Replace with actual match endpoint
    this.tournamentService.getTournamentById(id).subscribe({
      next: (response) => {
        if (response.success && response.data.matches) {
          this.match = response.data.matches[0]; // Temporary
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  startMatch(): void {
    if (!this.match) return;
    
    this.startingMatch = true;
    this.tournamentService.startMatch(this.match.id).subscribe({
      next: (response) => {
        if (response.success) {
          this.match = response.data;
        }
        this.startingMatch = false;
      },
      error: () => {
        this.startingMatch = false;
      }
    });
  }

  reportScore(): void {
    if (!this.match || !this.scoreForm.valid) return;
    
    this.reportingScore = true;
    const scoreData = {
      participant1Score: this.scoreForm.value.team1Score,
      participant2Score: this.scoreForm.value.team2Score,
      screenshotUrls: this.scoreForm.value.screenshotUrls,
      matchReplayUrl: this.scoreForm.value.matchReplayUrl,
      notes: this.scoreForm.value.notes
    };
    
    this.tournamentService.reportScore(this.match.id, scoreData).subscribe({
      next: (response) => {
        if (response.success) {
          this.match = response.data;
          this.scoreForm.reset();
        }
        this.reportingScore = false;
      },
      error: () => {
        this.reportingScore = false;
      }
    });
  }

  confirmScore(): void {
    if (!this.match) return;
    
    this.confirmingScore = true;
    this.tournamentService.confirmScore(this.match.id).subscribe({
      next: (response) => {
        if (response.success) {
          this.match = response.data;
        }
        this.confirmingScore = false;
      },
      error: () => {
        this.confirmingScore = false;
      }
    });
  }

  addScreenshot(url: string): void {
    if (url.trim()) {
      const screenshots = this.scoreForm.get('screenshotUrls')?.value || [];
      screenshots.push(url.trim());
      this.scoreForm.patchValue({ screenshotUrls: screenshots });
    }
  }

  removeScreenshot(index: number): void {
    const screenshots = this.scoreForm.get('screenshotUrls')?.value || [];
    screenshots.splice(index, 1);
    this.scoreForm.patchValue({ screenshotUrls: screenshots });
  }

  openDispute(): void {
    if (!this.match) return;
    
    if (confirm('Are you sure you want to dispute this match? An admin will review the case.')) {
      // TODO: Implement dispute creation
      console.log('Opening dispute for match:', this.match.id);
    }
  }

  canStartMatch(): boolean {
    return this.match?.status === MatchStatus.READY && this.isParticipant();
  }

  canReportScore(): boolean {
    return (this.match?.status === MatchStatus.READY || 
            this.match?.status === MatchStatus.IN_PROGRESS) && 
           this.isParticipant();
  }

  canConfirmScore(): boolean {
    return this.match?.status === MatchStatus.IN_PROGRESS && 
           this.isParticipant();
  }

  isParticipant(): boolean {
    if (!this.match || !this.currentUserId) return false;
    
    // Check if user is in team1 or team2
    const inTeam1 = this.match.team1?.members?.some(m => m.id === this.currentUserId);
    const inTeam2 = this.match.team2?.members?.some(m => m.id === this.currentUserId);
    
    return inTeam1 || inTeam2;
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'SCHEDULED':
        return 'text-gray-400';
      case 'READY':
        return 'text-blue-500';
      case 'IN_PROGRESS':
        return 'text-yellow-500';
      case 'COMPLETED':
        return 'text-green-500';
      case 'DISPUTED':
        return 'text-red-500';
      default:
        return 'text-gray-400';
    }
  }

  getStatusBadge(status: string): string {
    switch (status) {
      case 'SCHEDULED':
        return 'bg-gray-600';
      case 'READY':
        return 'bg-blue-600';
      case 'IN_PROGRESS':
        return 'bg-yellow-600';
      case 'COMPLETED':
        return 'bg-green-600';
      case 'DISPUTED':
        return 'bg-red-600';
      default:
        return 'bg-gray-600';
    }
  }

  getWinnerClass(teamId: number): string {
    if (this.match?.status !== MatchStatus.COMPLETED || !this.match.winner) {
      return '';
    }
    return this.match.winner.id === teamId ? 'border-green-500 bg-green-900 bg-opacity-20' : 'opacity-60';
  }
}
