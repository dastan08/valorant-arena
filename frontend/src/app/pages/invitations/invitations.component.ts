import { Component, OnInit } from '@angular/core';
import { TeamService } from '../../core/services/team.service';
import { Router } from '@angular/router';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-invitations',
  templateUrl: './invitations.component.html',
  styleUrls: ['./invitations.component.css']
})
export class InvitationsComponent implements OnInit {
  invitations: any[] = [];
  loading = false;
  error: string | null = null;

  constructor(
    private teamService: TeamService,
    private router: Router,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadInvitations();
  }

  loadInvitations(): void {
    this.loading = true;
    this.teamService.getPendingInvitations().subscribe({
      next: (response) => {
        if (response.success) {
          this.invitations = response.data;
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading invitations:', err);
        this.error = 'Failed to load invitations';
        this.loading = false;
      }
    });
  }

  acceptInvitation(invitation: any): void {
    this.loading = true;
    this.error = null;

    this.teamService.acceptInvitation(invitation.id).subscribe({
      next: (response) => {
        if (response.success) {
          alert(`You joined ${invitation.teamName}!`);
          this.router.navigate(['/teams']);
        } else {
          this.error = response.message || 'Failed to accept invitation';
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to accept invitation';
        this.loading = false;
      }
    });
  }

  declineInvitation(invitation: any): void {
    if (!confirm(`Are you sure you want to decline the invitation from ${invitation.teamName}?`)) {
      return;
    }

    this.loading = true;
    this.error = null;

    this.teamService.declineInvitation(invitation.id).subscribe({
      next: (response) => {
        if (response.success) {
          this.loadInvitations();
        } else {
          this.error = response.message || 'Failed to decline invitation';
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to decline invitation';
        this.loading = false;
      }
    });
  }

  getTeamLogo(invitation: any): string {
    return invitation.teamLogo || 'assets/default-team-logo.png';
  }
}
