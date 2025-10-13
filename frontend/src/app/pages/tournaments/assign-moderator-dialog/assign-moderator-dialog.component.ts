import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TournamentModeratorService } from '../../../core/services/tournament-moderator.service';
import { AdminService } from '../../../core/services/admin.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-assign-moderator-dialog',
  templateUrl: './assign-moderator-dialog.component.html',
  styleUrls: ['./assign-moderator-dialog.component.css']
})
export class AssignModeratorDialogComponent implements OnInit {
  assignForm: FormGroup;
  loading = false;
  tournamentId: number;
  availableUsers: any[] = [];
  loadingUsers = false;

  constructor(
    private fb: FormBuilder,
    private moderatorService: TournamentModeratorService,
    private adminService: AdminService,
    private snackBar: MatSnackBar,
    public dialogRef: MatDialogRef<AssignModeratorDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.tournamentId = data.tournamentId;
    this.assignForm = this.fb.group({
      userId: ['', Validators.required],
      notes: ['']
    });
  }

  ngOnInit(): void {
    this.loadAvailableUsers();
  }

  loadAvailableUsers(): void {
    this.loadingUsers = true;
    this.adminService.getAllUsers().subscribe({
      next: (response) => {
        if (response.success) {
          // Filter users with TOURNAMENT_MODERATOR role
          this.availableUsers = response.data.filter(
            (user: any) => user.role === 'TOURNAMENT_MODERATOR'
          );
        }
        this.loadingUsers = false;
      },
      error: (err) => {
        console.error('Error loading users:', err);
        this.snackBar.open('Failed to load users', 'Close', { duration: 3000 });
        this.loadingUsers = false;
      }
    });
  }

  onAssign(): void {
    if (this.assignForm.valid) {
      this.loading = true;
      const formData = this.assignForm.value;
      
      this.moderatorService.assignModerator(this.tournamentId, formData).subscribe({
        next: (response) => {
          if (response.success) {
            this.snackBar.open('Moderator assigned successfully', 'Close', { duration: 3000 });
            this.dialogRef.close('assigned');
          }
          this.loading = false;
        },
        error: (err) => {
          console.error('Error assigning moderator:', err);
          this.snackBar.open(err.error?.message || 'Failed to assign moderator', 'Close', { duration: 3000 });
          this.loading = false;
        }
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
