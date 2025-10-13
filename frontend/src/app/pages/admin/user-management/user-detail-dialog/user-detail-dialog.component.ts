import { Component, Inject, OnInit, ViewEncapsulation } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AdminService } from '../../../../core/services/admin.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-user-detail-dialog',
  templateUrl: './user-detail-dialog.component.html',
  styleUrls: ['./user-detail-dialog.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class UserDetailDialogComponent implements OnInit {
  userForm: FormGroup;
  loading = false;
  user: any;

  roles = ['USER', 'MODERATOR', 'ADMIN', 'ORGANIZER', 'TOURNAMENT_MODERATOR'];
  statuses = ['ACTIVE', 'SUSPENDED', 'BANNED'];

  constructor(
    private fb: FormBuilder,
    private adminService: AdminService,
    private snackBar: MatSnackBar,
    public dialogRef: MatDialogRef<UserDetailDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.user = data.user;
    this.userForm = this.fb.group({
      username: [this.user.username, [Validators.required, Validators.minLength(3)]],
      email: [this.user.email, [Validators.required, Validators.email]],
      role: [this.user.role, Validators.required],
      status: [this.user.status, Validators.required],
      isVerified: [this.user.isVerified]
    });
  }

  ngOnInit(): void {}

  onSave(): void {
    if (this.userForm.valid) {
      this.loading = true;
      const formData = this.userForm.value;
      
      this.adminService.updateUser(this.user.id, formData).subscribe({
        next: (response) => {
          if (response.success) {
            this.snackBar.open('User updated successfully', 'Close', { duration: 3000 });
            this.dialogRef.close('updated');
          }
          this.loading = false;
        },
        error: (err) => {
          console.error('Error updating user:', err);
          this.snackBar.open('Failed to update user', 'Close', { duration: 3000 });
          this.loading = false;
        }
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
