import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../../core/services/admin.service';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TeamDetailDialogComponent } from './team-detail-dialog/team-detail-dialog.component';
import { environment } from '../../../core/config/environment';

@Component({
  selector: 'app-team-management',
  templateUrl: './team-management.component.html',
  styleUrls: ['./team-management.component.css']
})
export class TeamManagementComponent implements OnInit {
  teams: any[] = [];
  loading = false;
  searchTerm = '';
  currentPage = 0;
  pageSize = 20;
  totalTeams = 0;
  totalPages = 0;
  Math = Math; // Expose Math to template

  displayedColumns: string[] = ['logo', 'name', 'tag', 'captain', 'members', 'elo', 'status', 'createdAt', 'actions'];

  constructor(
    private adminService: AdminService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadTeams();
  }

  loadTeams(): void {
    this.loading = true;
    this.adminService.getAllTeams(this.currentPage, this.pageSize, this.searchTerm).subscribe({
      next: (response) => {
        if (response.success) {
          this.teams = response.data.content;
          this.totalTeams = response.data.totalElements;
          this.totalPages = response.data.totalPages;
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading teams:', err);
        this.snackBar.open('Failed to load teams', 'Close', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  onSearch(): void {
    this.currentPage = 0;
    this.loadTeams();
  }

  onPageChange(event: any): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadTeams();
  }

  viewTeam(team: any): void {
    const dialogRef = this.dialog.open(TeamDetailDialogComponent, {
      width: '900px',
      maxWidth: '95vw',
      data: { team },
      panelClass: 'custom-dialog-container'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === 'updated') {
        this.loadTeams();
      }
    });
  }

  disbandTeam(team: any): void {
    const reason = prompt(`Enter reason for disbanding team "${team.name}":`);
    if (reason && reason.trim()) {
      this.adminService.disbandTeam(team.id, reason).subscribe({
        next: (response) => {
          if (response.success) {
            this.snackBar.open('Team disbanded successfully', 'Close', { duration: 3000 });
            this.loadTeams();
          }
        },
        error: (err) => {
          console.error('Error disbanding team:', err);
          this.snackBar.open('Failed to disband team', 'Close', { duration: 3000 });
        }
      });
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'text-green-500';
      case 'INACTIVE': return 'text-yellow-500';
      case 'DISBANDED': return 'text-red-500';
      default: return 'text-gray-500';
    }
  }

  getStatusIcon(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'check_circle';
      case 'INACTIVE': return 'pause_circle';
      case 'DISBANDED': return 'cancel';
      default: return 'help';
    }
  }

  getTeamLogoUrl(logoUrl: string | undefined): string {
    if (!logoUrl || logoUrl === 'default-team-logo.png') return 'assets/default-team-logo.png';
    if (logoUrl.startsWith('http')) return logoUrl;
    return `${environment.apiUrl.replace('/api', '')}${logoUrl}`;
  }
}
