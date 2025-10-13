import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ScrimService, ChallengeScrimRequest } from '../../../core/services/scrim.service';
import { TeamService } from '../../../core/services/team.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-challenge-scrim-dialog',
  templateUrl: './challenge-scrim-dialog.component.html',
  styleUrls: ['./challenge-scrim-dialog.component.css']
})
export class ChallengeScrimDialogComponent implements OnInit {
  challengerTeam: any;
  opponentTeam: any = null;
  allTeams: any[] = [];
  loading = false;
  error: string | null = null;

  // Form fields
  scheduledDate: string = '';
  scheduledTime: string = '18:00';
  selectedMap: string = 'Haven';
  bestOf: number = 1;
  message: string = '';

  valorantMaps = [
    'Ascent', 'Bind', 'Haven', 'Split', 'Icebox',
    'Breeze', 'Fracture', 'Pearl', 'Lotus', 'Sunset', 'Abyss'
  ];

  constructor(
    private dialogRef: MatDialogRef<ChallengeScrimDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { challengerTeam: any, opponentTeam?: any },
    private scrimService: ScrimService,
    private teamService: TeamService,
    private notificationService: NotificationService
  ) {
    this.challengerTeam = data.challengerTeam;
    
    console.log('Challenge Dialog Data:', data);
    console.log('Challenger Team:', this.challengerTeam);
    console.log('Opponent Team (from data):', data.opponentTeam);
    
    // Pre-select opponent team if provided
    if (data.opponentTeam) {
      this.opponentTeam = data.opponentTeam;
      console.log('Opponent team set to:', this.opponentTeam);
    }
    
    // Set default date to tomorrow
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    this.scheduledDate = tomorrow.toISOString().split('T')[0];
  }

  ngOnInit(): void {
    this.loadTeams();
  }

  loadTeams(): void {
    this.loading = true;
    this.teamService.getAllTeams().subscribe({
      next: (response) => {
        if (response.success) {
          // Filter out own team
          this.allTeams = response.data.filter((team: any) => team.id !== this.challengerTeam.id);
          
          // If opponent was pre-selected, find it in the loaded teams
          if (this.opponentTeam) {
            const preSelectedTeam = this.allTeams.find((team: any) => team.id === this.opponentTeam.id);
            if (preSelectedTeam) {
              this.opponentTeam = preSelectedTeam;
              console.log('Pre-selected opponent team:', this.opponentTeam);
            }
          }
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading teams:', err);
        this.error = 'Failed to load teams';
        this.loading = false;
      }
    });
  }

  challengeTeam(): void {
    if (!this.opponentTeam) {
      this.error = 'Please select an opponent team';
      return;
    }

    if (!this.scheduledDate) {
      this.error = 'Please select a date';
      return;
    }

    // Combine date and time
    const scheduledDateTime = `${this.scheduledDate}T${this.scheduledTime}:00`;

    const request: ChallengeScrimRequest = {
      challengerTeamId: this.challengerTeam.id,
      opponentTeamId: this.opponentTeam.id,
      scheduledTime: scheduledDateTime,
      message: this.message || undefined,
      map: this.selectedMap,
      bestOf: this.bestOf
    };

    this.loading = true;
    this.error = null;

    this.scrimService.challengeTeam(request).subscribe({
      next: (response) => {
        if (response.success) {
          alert(`Scrim challenge sent to ${this.opponentTeam.name}!`);
          
          // Create notification for the opponent team
          this.notificationService.notifyScrimChallenge(
            this.challengerTeam.name,
            response.data.id,
            scheduledDateTime
          );
          
          this.dialogRef.close(true);
        } else {
          this.error = response.message || 'Failed to send challenge';
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to send challenge';
        this.loading = false;
      }
    });
  }

  close(): void {
    this.dialogRef.close();
  }
}
