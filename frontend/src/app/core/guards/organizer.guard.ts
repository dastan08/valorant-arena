import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { UserRole } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class OrganizerGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean {
    const currentUser = this.authService.getCurrentUser();
    
    // Check if user is authenticated
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
      return false;
    }
    
    // Check if user has ORGANIZER, TOURNAMENT_MODERATOR, or ADMIN role
    if (currentUser?.role === UserRole.ORGANIZER || 
        currentUser?.role === UserRole.TOURNAMENT_MODERATOR || 
        currentUser?.role === UserRole.ADMIN) {
      return true;
    }
    
    // User doesn't have permission
    this.router.navigate(['/dashboard']);
    return false;
  }
}
