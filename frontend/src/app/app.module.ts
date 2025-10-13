import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { CommonModule } from '@angular/common';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { JwtModule } from '@auth0/angular-jwt';
import { ToastrModule } from 'ngx-toastr';
import { MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { AuthInterceptor } from './core/interceptors/auth.interceptor';
import { ErrorInterceptor } from './core/interceptors/error.interceptor';

// Components
import { NavbarComponent } from './components/navbar/navbar.component';
import { HomeComponent } from './pages/home/home.component';
import { LoginComponent } from './pages/auth/login/login.component';
import { RegisterComponent } from './pages/auth/register/register.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { TournamentsComponent } from './pages/tournaments/tournaments.component';
import { TeamsModule } from './pages/teams/teams.module';
import { ProfileComponent } from './pages/profile/profile.component';
import { DiscordCallbackComponent } from './pages/auth/discord-callback/discord-callback.component';
import { MatchmakingComponent } from './pages/matchmaking/matchmaking.component';
import { InvitationsComponent } from './pages/invitations/invitations.component';
import { ScrimsComponent } from './pages/scrims/scrims.component';
import { NotificationBellComponent } from './shared/components/notification-bell/notification-bell.component';
import { ClickOutsideDirective } from './shared/directives/click-outside.directive';
import { OrganizerDashboardComponent } from './pages/tournaments/organizer-dashboard/organizer-dashboard.component';
import { AssignModeratorDialogComponent } from './pages/tournaments/assign-moderator-dialog/assign-moderator-dialog.component';
import { TournamentCreateComponent } from './pages/tournaments/tournament-create/tournament-create.component';
import { TournamentDetailComponent } from './pages/tournaments/tournament-detail/tournament-detail.component';

export function tokenGetter() {
  return localStorage.getItem('token');
}

@NgModule({
  declarations: [
    AppComponent,
    NavbarComponent,
    HomeComponent,
    LoginComponent,
    RegisterComponent,
    DashboardComponent,
    TournamentsComponent,
    ProfileComponent,
    DiscordCallbackComponent,
    MatchmakingComponent,
    InvitationsComponent,
    ScrimsComponent,
    NotificationBellComponent,
    ClickOutsideDirective,
    OrganizerDashboardComponent,
    AssignModeratorDialogComponent,
    TournamentCreateComponent,
    TournamentDetailComponent
  ],
  imports: [
    BrowserModule,
    CommonModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    TeamsModule,
    MatDialogModule,
    MatIconModule,
    MatProgressSpinnerModule,
    JwtModule.forRoot({
      config: {
        tokenGetter: tokenGetter,
        allowedDomains: ['localhost:8080'],
        disallowedRoutes: ['localhost:8080/api/auth/login', 'localhost:8080/api/auth/register']
      }
    }),
    ToastrModule.forRoot({
      timeOut: 3000,
      positionClass: 'toast-top-right',
      preventDuplicates: true,
    })
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
