import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';

import { TeamsComponent } from './teams.component';
import { CreateTeamDialogComponent } from './create-team-dialog/create-team-dialog.component';
import { TeamDetailDialogComponent } from './team-detail-dialog/team-detail-dialog.component';
import { ChallengeScrimDialogComponent } from './challenge-scrim-dialog/challenge-scrim-dialog.component';

@NgModule({
  declarations: [
    TeamsComponent,
    CreateTeamDialogComponent,
    TeamDetailDialogComponent,
    ChallengeScrimDialogComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    MatProgressSpinnerModule,
    MatIconModule
  ],
  exports: [
    TeamsComponent
  ]
})
export class TeamsModule { }
