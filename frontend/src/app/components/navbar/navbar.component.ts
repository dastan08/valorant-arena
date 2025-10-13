import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { TeamService } from '../../core/services/team.service';
import { NotificationService } from '../../core/services/notification.service';
import { User, UserRole } from '../../core/models/user.model';
import { interval } from 'rxjs';
import { switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {
  currentUser: User | null = null;
  isMenuOpen = false;
  profilePictureUrl: string = 'assets/default-avatar.png';
  pendingInvitationsCount = 0;

  constructor(
    private authService: AuthService,
    private teamService: TeamService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.authService.currentUser.subscribe(user => {
      this.currentUser = user;
      this.profilePictureUrl = this.computeProfilePictureUrl(user);
      console.log('Navbar - Current user updated:', user);
      console.log('Navbar - Computed profile URL:', this.profilePictureUrl);
      
      if (user) {
        this.loadPendingInvitationsCount();
        // Poll for new invitations every 30 seconds
        interval(30000).pipe(
          switchMap(() => this.teamService.getPendingInvitations())
        ).subscribe({
          next: (response) => {
            if (response.success) {
              this.pendingInvitationsCount = response.data.length;
            }
          }
        });
      }
    });
  }

  loadPendingInvitationsCount(): void {
    this.teamService.getPendingInvitations().subscribe({
      next: (response) => {
        if (response.success) {
          this.pendingInvitationsCount = response.data.length;
          
          // Create notifications for new invitations
          response.data.forEach((invitation: any) => {
            // Check if notification already exists to avoid duplicates
            const existingNotifications = this.notificationService['notifications'].value;
            const alreadyNotified = existingNotifications.some(
              (n: any) => n.type === 'invitation' && n.data?.invitationId === invitation.id
            );
            
            if (!alreadyNotified) {
              this.notificationService.notifyTeamInvitation(
                invitation.teamName,
                invitation.id
              );
            }
          });
        }
      },
      error: (err) => {
        console.error('Error loading invitations count:', err);
      }
    });
  }

  toggleMenu(): void {
    this.isMenuOpen = !this.isMenuOpen;
  }

  logout(): void {
    this.authService.logout();
  }

  isAuthenticated(): boolean {
    return this.authService.isAuthenticated();
  }

  isAdmin(): boolean {
    return this.currentUser?.role === UserRole.ADMIN;
  }

  isOrganizer(): boolean {
    return this.currentUser?.role === UserRole.ORGANIZER || 
           this.currentUser?.role === UserRole.TOURNAMENT_MODERATOR || 
           this.currentUser?.role === UserRole.ADMIN;
  }

  private computeProfilePictureUrl(user: User | null): string {
    if (!user) {
      return 'assets/default-avatar.svg';
    }

    // Priority: Discord > profilePictureUrl > avatar > default
    // Check if Discord is linked
    if (user.discordId && user.discordAvatar) {
      return `https://cdn.discordapp.com/avatars/${user.discordId}/${user.discordAvatar}.png`;
    }

    if (user.profilePictureUrl) {
      return user.profilePictureUrl;
    }

    if (user.avatar && user.avatar !== 'default-avatar.png') {
      return `/uploads/avatars/${user.avatar}`;
    }

    return 'assets/default-avatar.svg';
  }

  onImageError(event: Event): void {
    const target = event.target as HTMLImageElement;
    target.src = 'assets/default-avatar.svg';
  }
}
