import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { TeamService } from '../../../core/services/team.service';

@Component({
  selector: 'app-create-team-dialog',
  templateUrl: './create-team-dialog.component.html',
  styleUrls: ['./create-team-dialog.component.css']
})
export class CreateTeamDialogComponent implements OnInit {
  teamForm: FormGroup;
  loading = false;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private teamService: TeamService,
    private dialogRef: MatDialogRef<CreateTeamDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { userId: number }
  ) {
    this.teamForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(32)]],
      tag: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(5)]],
      description: ['', [Validators.maxLength(500)]],
      maxMembers: [5, [Validators.required, Validators.min(2), Validators.max(10)]]
    });
  }

  ngOnInit(): void {}

  onSubmit(): void {
    console.log('onSubmit called');
    console.log('Form valid:', this.teamForm.valid);
    console.log('Form value:', this.teamForm.value);
    
    if (this.teamForm.invalid) {
      console.log('Form is invalid', this.teamForm.errors);
      Object.keys(this.teamForm.controls).forEach(key => {
        const control = this.teamForm.get(key);
        if (control?.invalid) {
          console.log(`${key} is invalid:`, control.errors);
        }
      });
      return;
    }

    this.loading = true;
    this.error = null;

    // Prepare team data (userId comes from authentication)
    const teamData = {
      name: this.teamForm.value.name,
      tag: this.teamForm.value.tag,
      description: this.teamForm.value.description || '',
      maxMembers: this.teamForm.value.maxMembers,
      logo: '' // Optional: can be added later
    };

    console.log('Calling createTeam service with data:', teamData);
    this.teamService.createTeam(teamData).subscribe({
      next: (response) => {
        console.log('Create team response:', response);
        this.loading = false;
        if (response.success) {
          this.dialogRef.close(true);
        } else {
          this.error = response.message || 'Failed to create team';
        }
      },
      error: (err) => {
        console.error('Create team error:', err);
        console.error('Error status:', err.status);
        console.error('Error details:', err.error);
        this.loading = false;
        
        // Better error messages
        if (err.status === 0) {
          this.error = 'Cannot connect to server. Please make sure the backend is running.';
        } else if (err.status === 404) {
          this.error = 'API endpoint not found. Please check the backend configuration.';
        } else if (err.status === 400) {
          this.error = err.error?.message || 'Invalid data. Please check your inputs.';
        } else if (err.status === 401) {
          this.error = 'You are not authorized. Please log in again.';
        } else {
          this.error = err.error?.message || `Server error (${err.status}). Please try again.`;
        }
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }

  // Helper to easily access form controls in the template
  get f() {
    return this.teamForm.controls;
  }
}
