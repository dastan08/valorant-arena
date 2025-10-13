import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../../core/services/admin.service';
import { AuthService } from '../../../core/services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
  stats = {
    totalUsers: 0,
    totalTeams: 0,
    activeScrims: 0,
    pendingReports: 0,
    onlineUsers: 0,
    totalMatches: 0
  };

  recentActivity: any[] = [];
  systemHealth = {
    database: 'healthy',
    api: 'healthy',
    discord: 'healthy',
    riot: 'healthy'
  };

  loading = true;
  currentUser: any;

  constructor(
    private adminService: AdminService,
    private authService: AuthService,
    private router: Router
  ) {
    this.currentUser = this.authService.currentUserValue;
  }

  ngOnInit(): void {
    this.checkAdminAccess();
    this.loadDashboardData();
  }

  checkAdminAccess(): void {
    if (!this.currentUser || this.currentUser.role !== 'ADMIN') {
      this.router.navigate(['/']);
      return;
    }
  }

  loadDashboardData(): void {
    this.loading = true;
    
    // Load dashboard statistics
    this.adminService.getDashboardStats().subscribe({
      next: (response) => {
        if (response.success) {
          this.stats = response.data;
        }
      },
      error: (err) => {
        console.error('Error loading dashboard stats:', err);
      }
    });

    // Load recent activity
    this.adminService.getRecentActivity().subscribe({
      next: (response) => {
        if (response.success) {
          this.recentActivity = response.data;
        }
      },
      error: (err) => {
        console.error('Error loading recent activity:', err);
      }
    });

    // Load system health
    this.adminService.getSystemHealth().subscribe({
      next: (response) => {
        if (response.success) {
          this.systemHealth = response.data;
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading system health:', err);
        this.loading = false;
      }
    });
  }

  getHealthStatusClass(status: string): string {
    switch (status) {
      case 'healthy': return 'text-green-500';
      case 'warning': return 'text-yellow-500';
      case 'error': return 'text-red-500';
      default: return 'text-gray-500';
    }
  }

  getHealthIcon(status: string): string {
    switch (status) {
      case 'healthy': return 'check_circle';
      case 'warning': return 'warning';
      case 'error': return 'error';
      default: return 'help';
    }
  }

  navigateToUsers(): void {
    this.router.navigate(['/admin/users']);
  }

  navigateToTeams(): void {
    this.router.navigate(['/admin/teams']);
  }

  navigateToScrims(): void {
    this.router.navigate(['/admin/scrims']);
  }

  navigateToReports(): void {
    this.router.navigate(['/admin/reports']);
  }
}
