import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-ban-user-dialog',
  templateUrl: './ban-user-dialog.component.html',
  styleUrls: ['./ban-user-dialog.component.css']
})
export class BanUserDialogComponent {
  banForm: FormGroup;
  user: any;

  banDurations = [
    { label: 'Permanent', value: null },
    { label: '1 Day', value: 1 },
    { label: '3 Days', value: 3 },
    { label: '7 Days', value: 7 },
    { label: '30 Days', value: 30 },
    { label: '90 Days', value: 90 }
  ];

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<BanUserDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.user = data.user;
    this.banForm = this.fb.group({
      reason: ['', [Validators.required, Validators.minLength(10)]],
      duration: [null]
    });
  }

  onBan(): void {
    if (this.banForm.valid) {
      this.dialogRef.close(this.banForm.value);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
