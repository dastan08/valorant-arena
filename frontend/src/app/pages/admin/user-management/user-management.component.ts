import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../../core/services/admin.service';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { UserDetailDialogComponent } from './user-detail-dialog/user-detail-dialog.component';
import { BanUserDialogComponent } from './ban-user-dialog/ban-user-dialog.component';

@Component({
  selector: 'app-user-management',
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.css']
})
export class UserManagementComponent implements OnInit {
  users: any[] = [];
  loading = false;
  searchTerm = '';
  currentPage = 0;
  pageSize = 20;
  totalUsers = 0;
  totalPages = 0;
  Math = Math; // Expose Math to template

  displayedColumns: string[] = ['avatar', 'username', 'email', 'role', 'team', 'status', 'createdAt', 'actions'];

  constructor(
    private adminService: AdminService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.adminService.getAllUsers(this.currentPage, this.pageSize, this.searchTerm).subscribe({
      next: (response) => {
        if (response.success) {
          this.users = response.data.content;
          this.totalUsers = response.data.totalElements;
          this.totalPages = response.data.totalPages;
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading users:', err);
        this.snackBar.open('Failed to load users', 'Close', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  onSearch(): void {
    this.currentPage = 0;
    this.loadUsers();
  }

  onPageChange(event: any): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadUsers();
  }

  viewUser(user: any): void {
    const dialogRef = this.dialog.open(UserDetailDialogComponent, {
      width: '800px',
      maxWidth: '95vw',
      data: { user },
      panelClass: 'custom-dialog-container'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === 'updated') {
        this.loadUsers();
      }
    });
  }

  banUser(user: any): void {
    const dialogRef = this.dialog.open(BanUserDialogComponent, {
      width: '500px',
      maxWidth: '95vw',
      data: { user },
      panelClass: 'custom-dialog-container'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.adminService.banUser(user.id, result.reason, result.duration).subscribe({
          next: (response) => {
            if (response.success) {
              this.snackBar.open('User banned successfully', 'Close', { duration: 3000 });
              this.loadUsers();
            }
          },
          error: (err) => {
            console.error('Error banning user:', err);
            this.snackBar.open('Failed to ban user', 'Close', { duration: 3000 });
          }
        });
      }
    });
  }

  unbanUser(user: any): void {
    if (confirm(`Are you sure you want to unban ${user.username}?`)) {
      this.adminService.unbanUser(user.id).subscribe({
        next: (response) => {
          if (response.success) {
            this.snackBar.open('User unbanned successfully', 'Close', { duration: 3000 });
            this.loadUsers();
          }
        },
        error: (err) => {
          console.error('Error unbanning user:', err);
          this.snackBar.open('Failed to unban user', 'Close', { duration: 3000 });
        }
      });
    }
  }

  deleteUser(user: any): void {
    if (confirm(`Are you sure you want to permanently delete ${user.username}? This action cannot be undone.`)) {
      this.adminService.deleteUser(user.id).subscribe({
        next: (response) => {
          if (response.success) {
            this.snackBar.open('User deleted successfully', 'Close', { duration: 3000 });
            this.loadUsers();
          }
        },
        error: (err) => {
          console.error('Error deleting user:', err);
          this.snackBar.open('Failed to delete user', 'Close', { duration: 3000 });
        }
      });
    }
  }

  getRoleClass(role: string): string {
    switch (role) {
      case 'ADMIN': return 'bg-red-500 text-white';
      case 'MODERATOR': return 'bg-yellow-500 text-black';
      case 'USER': return 'bg-blue-500 text-white';
      default: return 'bg-gray-500 text-white';
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'text-green-500';
      case 'BANNED': return 'text-red-500';
      case 'SUSPENDED': return 'text-yellow-500';
      default: return 'text-gray-500';
    }
  }

  getStatusIcon(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'check_circle';
      case 'BANNED': return 'block';
      case 'SUSPENDED': return 'pause_circle';
      default: return 'help';
    }
  }
}
