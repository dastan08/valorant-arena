import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { UserManagementComponent } from './user-management/user-management.component';
import { TeamManagementComponent } from './team-management/team-management.component';
import { ScrimManagementComponent } from './scrim-management/scrim-management.component';
import { ReportManagementComponent } from './report-management/report-management.component';
import { AdminGuard } from '../../core/guards/admin.guard';

const routes: Routes = [
  {
    path: '',
    component: AdminDashboardComponent,
    canActivate: [AdminGuard]
  },
  {
    path: 'users',
    component: UserManagementComponent,
    canActivate: [AdminGuard]
  },
  {
    path: 'teams',
    component: TeamManagementComponent,
    canActivate: [AdminGuard]
  },
  {
    path: 'scrims',
    component: ScrimManagementComponent,
    canActivate: [AdminGuard]
  },
  {
    path: 'reports',
    component: ReportManagementComponent,
    canActivate: [AdminGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdminRoutingModule { }
