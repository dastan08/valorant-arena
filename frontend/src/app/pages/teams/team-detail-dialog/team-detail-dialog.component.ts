import { Component, Inject, OnInit, ViewChild, ElementRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialog } from '@angular/material/dialog';
import { TeamService } from '../../../core/services/team.service';
import { AuthService } from '../../../core/services/auth.service';
import { User } from '../../../core/models/user.model';
import { ChallengeScrimDialogComponent } from '../challenge-scrim-dialog/challenge-scrim-dialog.component';

@Component({
  selector: 'app-team-detail-dialog',
  templateUrl: './team-detail-dialog.component.html',
  styleUrls: ['./team-detail-dialog.component.css']
})
export class TeamDetailDialogComponent implements OnInit {
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;
  
  private apiUrl = 'http://localhost:8080/api';
  team: any;
  currentUser: User | null = null;
  loading = false;
  error: string | null = null;
  pendingInvitations: any[] = [];
  searchTerm = '';
  searchResults: User[] = [];
  inviteMessage = '';
  showInviteSection = false;
  loadingInvitations = false;
  searching = false;
  teamLogoUrl: string = '';

  constructor(
    private dialogRef: MatDialogRef<TeamDetailDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { team: any },
    private teamService: TeamService,
    private authService: AuthService,
    private dialog: MatDialog,
    private http: HttpClient
  ) {
    this.team = data.team;
    // Get current user from auth service
    this.currentUser = this.authService.currentUserValue;
    console.log('Constructor - Current User:', this.currentUser);
    console.log('Constructor - User has team:', !!this.currentUser?.team);
  }

  ngOnInit(): void {
    this.updateTeamLogoUrl();
    this.loadUserProfile(); // Load user profile with team info
    this.loadTeamDetails();
    if (this.canManageTeam()) {
      this.loadPendingInvitations();
    }
  }

  loadUserProfile(): void {
    this.http.get<any>(`${this.apiUrl}/user/profile`).subscribe({
      next: (response) => {
        if (response.success) {
          this.currentUser = response.data;
          console.log('=== Challenge Button Debug (After Profile Load) ===');
          console.log('Current User:', this.currentUser);
          console.log('Current User Team:', this.currentUser?.team);
          console.log('Viewing Team:', this.team);
          console.log('Has Team:', !!this.currentUser?.team);
          console.log('Different Team:', this.currentUser?.team?.id !== this.team.id);
          console.log('Can Manage Own Team:', this.canManageOwnTeam());
        }
      },
      error: (err) => {
        console.error('Error loading user profile:', err);
      }
    });
  }

  updateTeamLogoUrl(): void {
    this.teamLogoUrl = this.computeTeamLogoUrl(this.team.logo);
  }

  loadTeamDetails(): void {
    this.loading = true;
    this.teamService.getTeamById(this.team.id).subscribe({
      next: (response) => {
        if (response.success) {
          this.team = response.data;
          this.updateTeamLogoUrl();
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading team details:', err);
        this.loading = false;
      }
    });
  }

  loadPendingInvitations(): void {
    this.loadingInvitations = true;
    this.teamService.getTeamInvitations(this.team.id).subscribe({
      next: (response) => {
        if (response.success) {
          this.pendingInvitations = response.data;
        }
        this.loadingInvitations = false;
      },
      error: (err) => {
        console.error('Error loading invitations:', err);
        this.loadingInvitations = false;
      }
    });
  }

  cancelInvitation(invitationId: number): void {
    if (!confirm('Are you sure you want to cancel this invitation?')) {
      return;
    }

    this.loading = true;
    this.teamService.cancelInvitation(invitationId).subscribe({
      next: (response) => {
        if (response.success) {
          this.loadPendingInvitations();
        } else {
          this.error = response.message || 'Failed to cancel invitation';
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to cancel invitation';
        this.loading = false;
      }
    });
  }

  isCaptain(): boolean {
    return this.currentUser?.id === this.team.captain.id;
  }

  isManager(): boolean {
    if (!this.team.manager) {
      return this.isCaptain();
    }
    return this.currentUser?.id === this.team.manager.id;
  }

  canManageTeam(): boolean {
    return this.isCaptain() || this.isManager();
  }

  getEffectiveManager(): User {
    return this.team.manager || this.team.captain;
  }

  getRoleBadgeClass(member: User): string {
    if (member.id === this.team.captain.id) {
      return 'bg-yellow-500 text-black';
    } else if (this.team.manager && member.id === this.team.manager.id) {
      return 'bg-blue-500 text-white';
    }
    return 'bg-gray-600 text-gray-300';
  }

  getRoleLabel(member: User): string {
    if (member.id === this.team.captain.id) {
      return 'CAPTAIN';
    } else if (this.team.manager && member.id === this.team.manager.id) {
      return 'MANAGER';
    }
    return member.teamRole || 'PLAYER';
  }

  removeMember(member: User): void {
    if (!confirm(`Are you sure you want to remove ${member.username} from the team?`)) {
      return;
    }

    this.loading = true;
    this.error = null;

    this.teamService.removeMember(this.team.id, member.id).subscribe({
      next: (response) => {
        if (response.success) {
          this.loadTeamDetails();
        } else {
          this.error = response.message || 'Failed to remove member';
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to remove member';
        this.loading = false;
      }
    });
  }

  assignManager(member: User): void {
    this.loading = true;
    this.error = null;

    this.teamService.assignManager(this.team.id, member.id).subscribe({
      next: (response) => {
        if (response.success) {
          this.team = response.data;
        } else {
          this.error = response.message || 'Failed to assign manager';
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to assign manager';
        this.loading = false;
      }
    });
  }

  removeManager(): void {
    if (!confirm('Are you sure you want to remove the manager role?')) {
      return;
    }

    this.loading = true;
    this.error = null;

    this.teamService.removeManager(this.team.id).subscribe({
      next: (response) => {
        if (response.success) {
          this.team = response.data;
        } else {
          this.error = response.message || 'Failed to remove manager';
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to remove manager';
        this.loading = false;
      }
    });
  }

  transferCaptaincy(member: User): void {
    if (!confirm(`Are you sure you want to transfer captaincy to ${member.username}?`)) {
      return;
    }

    this.loading = true;
    this.error = null;

    this.teamService.transferCaptaincy(this.team.id, member.id).subscribe({
      next: (response) => {
        if (response.success) {
          this.team = response.data;
        } else {
          this.error = response.message || 'Failed to transfer captaincy';
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to transfer captaincy';
        this.loading = false;
      }
    });
  }

  searchPlayers(): void {
    if (!this.searchTerm || this.searchTerm.length < 2) {
      this.searchResults = [];
      return;
    }

    this.searching = true;
    this.teamService.searchUsers(this.searchTerm).subscribe({
      next: (response) => {
        if (response.success) {
          // Filter out users already in the team
          this.searchResults = response.data.filter((user: User) => 
            !this.team.members.some((member: any) => member.id === user.id)
          );
        }
        this.searching = false;
      },
      error: (err) => {
        console.error('Error searching players:', err);
        this.searching = false;
      }
    });
  }

  invitePlayer(user: User): void {
    this.loading = true;
    this.error = null;

    // Call invite API
    this.teamService.invitePlayer(this.team.id, user.id).subscribe({
      next: (response) => {
        if (response.success) {
          this.searchTerm = '';
          this.searchResults = [];
          alert(`Invitation sent to ${user.username}!`);
          this.loadPendingInvitations(); // Refresh invitations list
          this.loadTeamDetails();
        } else {
          this.error = response.message || 'Failed to invite player';
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to invite player';
        this.loading = false;
      }
    });
  }

  getMemberAvatar(member: User): string {
    // Priority: Discord avatar > Custom avatar
    if (member.discordId && member.discordAvatar) {
      return `https://cdn.discordapp.com/avatars/${member.discordId}/${member.discordAvatar}.png`;
    }
    if (member.avatar && member.avatar !== 'default-avatar.png') {
      return member.avatar;
    }
    return '';
  }

  getMemberInitial(member: User): string {
    return (member.username || 'U')[0].toUpperCase();
  }

  close(): void {
    this.dialogRef.close();
  }

  isTeamMember(): boolean {
    return this.currentUser?.team?.id === this.team.id;
  }

  leaveTeam(): void {
    const confirmMessage = this.isCaptain() && this.team.members.length === 1
      ? 'Are you sure you want to leave and disband this team? This action cannot be undone.'
      : 'Are you sure you want to leave this team?';
    
    if (!confirm(confirmMessage)) {
      return;
    }

    this.loading = true;
    this.error = null;

    this.teamService.leaveTeam().subscribe({
      next: (response) => {
        if (response.success) {
          alert('You have left the team');
          this.dialogRef.close(true); // Close and refresh
          window.location.reload(); // Refresh to update user's team status
        } else {
          this.error = response.message || 'Failed to leave team';
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to leave team';
        this.loading = false;
      }
    });
  }

  showCaptainLeaveWarning(): void {
    alert('As captain, you must transfer captaincy to another member before leaving the team. Use the "Transfer Captaincy" option in the Members section.');
  }

  openLogoUpload(): void {
    this.fileInput.nativeElement.click();
  }

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    
    if (!file) return;
    
    // Validate file type
    if (!file.type.startsWith('image/')) {
      alert('Please select an image file');
      return;
    }
    
    // Validate file size (5MB)
    if (file.size > 5 * 1024 * 1024) {
      alert('File size must be less than 5MB');
      return;
    }
    
    this.loading = true;
    this.error = null;
    
    // Upload file
    this.teamService.uploadTeamLogo(file).subscribe({
      next: (response: any) => {
        console.log('Upload response:', response);
        if (response.success) {
          const logoUrl = response.data;
          console.log('Uploaded logo URL:', logoUrl);
          // Update team logo
          this.teamService.updateTeamLogo(this.team.id, logoUrl).subscribe({
            next: (updateResponse: any) => {
              console.log('Update response:', updateResponse);
              if (updateResponse.success) {
                this.team.logo = logoUrl;
                console.log('Team logo updated to:', this.team.logo);
                // Reload team details to ensure fresh data
                this.loadTeamDetails();
                alert('Team logo updated successfully!');
              } else {
                this.error = updateResponse.message || 'Failed to update logo';
              }
              this.loading = false;
            },
            error: (err: any) => {
              this.error = err.error?.message || 'Failed to update logo';
              this.loading = false;
            }
          });
        } else {
          this.error = response.message || 'Failed to upload file';
          this.loading = false;
        }
      },
      error: (err: any) => {
        this.error = err.error?.message || 'Failed to upload file';
        this.loading = false;
      }
    });
    
    // Reset file input
    event.target.value = '';
  }

  computeTeamLogoUrl(logo: string): string {
    console.log('Computing logo URL for:', logo);
    if (!logo || logo === 'default-team-logo.png') {
      return 'assets/default-team-logo.png';
    }
    if (logo.startsWith('http')) {
      return logo;
    }
    // Add timestamp to prevent caching
    const timestamp = new Date().getTime();
    const fullUrl = `http://localhost:8080${logo}?t=${timestamp}`;
    console.log('Full logo URL:', fullUrl);
    return fullUrl;
  }

  canManageOwnTeam(): boolean {
    if (!this.currentUser?.team) return false;
    
    // Check by teamRole first
    const role = this.currentUser.teamRole;
    if (role === 'CAPTAIN' || role === 'MANAGER') {
      return true;
    }
    
    // Fallback: Check if user is captain or manager based on team structure
    const team = this.currentUser.team;
    const userId = this.currentUser.id;
    
    // Check if user is the team captain
    if (team.captain && team.captain.id === userId) {
      return true;
    }
    
    // Check if user is the team manager
    if (team.manager && team.manager.id === userId) {
      return true;
    }
    
    return false;
  }

  openChallengeDialog(): void {
    if (!this.currentUser?.team) {
      alert('You must be in a team to challenge other teams');
      return;
    }

    this.dialog.open(ChallengeScrimDialogComponent, {
      width: '600px',
      maxWidth: '95vw',
      data: { 
        challengerTeam: this.currentUser.team,
        opponentTeam: this.team  // Pre-select the team being viewed
      },
      panelClass: 'custom-dialog-container'
    });
  }
}
