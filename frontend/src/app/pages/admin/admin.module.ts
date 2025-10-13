import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

// Material Imports
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';

import { AdminRoutingModule } from './admin-routing.module';

// Components
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { UserManagementComponent } from './user-management/user-management.component';
import { TeamManagementComponent } from './team-management/team-management.component';
import { ScrimManagementComponent } from './scrim-management/scrim-management.component';
import { ReportManagementComponent } from './report-management/report-management.component';

// Dialog Components
import { UserDetailDialogComponent } from './user-management/user-detail-dialog/user-detail-dialog.component';
import { BanUserDialogComponent } from './user-management/ban-user-dialog/ban-user-dialog.component';
import { TeamDetailDialogComponent } from './team-management/team-detail-dialog/team-detail-dialog.component';
import { ScrimDetailDialogComponent } from './scrim-management/scrim-detail-dialog/scrim-detail-dialog.component';
import { ReportDetailDialogComponent } from './report-management/report-detail-dialog/report-detail-dialog.component';

@NgModule({
  declarations: [
    AdminDashboardComponent,
    UserManagementComponent,
    TeamManagementComponent,
    ScrimManagementComponent,
    ReportManagementComponent,
    UserDetailDialogComponent,
    BanUserDialogComponent,
    TeamDetailDialogComponent,
    ScrimDetailDialogComponent,
    ReportDetailDialogComponent
  ],
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatDialogModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatTableModule,
    MatPaginatorModule,
    AdminRoutingModule
  ]
})
export class AdminModule { }
