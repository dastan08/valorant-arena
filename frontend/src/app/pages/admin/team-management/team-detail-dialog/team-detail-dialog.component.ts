import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-team-detail-dialog',
  template: `
    <div class="bg-gray-900 text-white p-6 rounded-lg">
      <h2 class="text-2xl font-bold mb-4">Team Details - {{ data.team.name }}</h2>
      <p>Team detail dialog content will be implemented here.</p>
      <div class="flex justify-end mt-6">
        <button mat-raised-button (click)="dialogRef.close()">Close</button>
      </div>
    </div>
  `
})
export class TeamDetailDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<TeamDetailDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {}
}
