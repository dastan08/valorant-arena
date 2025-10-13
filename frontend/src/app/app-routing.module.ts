import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { OrganizerGuard } from './core/guards/organizer.guard';

import { HomeComponent } from './pages/home/home.component';
import { LoginComponent } from './pages/auth/login/login.component';
import { RegisterComponent } from './pages/auth/register/register.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { TournamentsComponent } from './pages/tournaments/tournaments.component';
import { TeamsComponent } from './pages/teams/teams.component';
import { ProfileComponent } from './pages/profile/profile.component';
import { DiscordCallbackComponent } from './pages/auth/discord-callback/discord-callback.component';
import { MatchmakingComponent } from './pages/matchmaking/matchmaking.component';
import { InvitationsComponent } from './pages/invitations/invitations.component';
import { ScrimsComponent } from './pages/scrims/scrims.component';
import { OrganizerDashboardComponent } from './pages/tournaments/organizer-dashboard/organizer-dashboard.component';
import { TournamentCreateComponent } from './pages/tournaments/tournament-create/tournament-create.component';
import { TournamentDetailComponent } from './pages/tournaments/tournament-detail/tournament-detail.component';

const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'auth/discord/callback', component: DiscordCallbackComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: 'matchmaking', component: MatchmakingComponent, canActivate: [AuthGuard] },
  { path: 'tournaments', component: TournamentsComponent },
  { path: 'tournaments/create', component: TournamentCreateComponent, canActivate: [OrganizerGuard] },
  { path: 'tournaments/organizer', component: OrganizerDashboardComponent, canActivate: [OrganizerGuard] },
  { path: 'tournaments/:id', component: TournamentDetailComponent },
  { path: 'teams', component: TeamsComponent },
  { path: 'scrims', component: ScrimsComponent, canActivate: [AuthGuard] },
  { path: 'invitations', component: InvitationsComponent, canActivate: [AuthGuard] },
  { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard] },
  { 
    path: 'admin', 
    loadChildren: () => import('./pages/admin/admin.module').then(m => m.AdminModule)
  },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
