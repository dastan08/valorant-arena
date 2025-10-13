import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Team } from '../models/user.model';
import { ApiResponse } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class TeamService {
  private apiUrl = 'http://localhost:8080/api/teams';

  constructor(private http: HttpClient) { }

  getAllTeams(): Observable<ApiResponse<Team[]>> {
    return this.http.get<ApiResponse<Team[]>>(this.apiUrl);
  }

  getTeamById(id: number): Observable<ApiResponse<Team>> {
    return this.http.get<ApiResponse<Team>>(`${this.apiUrl}/${id}`);
  }

  getTopTeams(limit: number = 10): Observable<ApiResponse<Team[]>> {
    return this.http.get<ApiResponse<Team[]>>(`${this.apiUrl}/top?limit=${limit}`);
  }

  createTeam(team: Partial<Team>): Observable<ApiResponse<Team>> {
    return this.http.post<ApiResponse<Team>>(`${this.apiUrl}/create`, team);
  }

  updateTeam(id: number, team: Partial<Team>): Observable<ApiResponse<Team>> {
    return this.http.put<ApiResponse<Team>>(`${this.apiUrl}/${id}`, team);
  }

  deleteTeam(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }

  addMember(teamId: number, userId: number): Observable<ApiResponse<Team>> {
    return this.http.post<ApiResponse<Team>>(`${this.apiUrl}/${teamId}/members/${userId}`, {});
  }

  searchTeams(searchTerm: string): Observable<ApiResponse<Team[]>> {
    return this.http.get<ApiResponse<Team[]>>(`${this.apiUrl}/search?q=${searchTerm}`);
  }

  // Team Management Methods
  removeMember(teamId: number, memberId: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${teamId}/members/${memberId}`);
  }

  assignManager(teamId: number, memberId: number): Observable<ApiResponse<Team>> {
    return this.http.post<ApiResponse<Team>>(`${this.apiUrl}/${teamId}/assign-manager/${memberId}`, {});
  }

  removeManager(teamId: number): Observable<ApiResponse<Team>> {
    return this.http.delete<ApiResponse<Team>>(`${this.apiUrl}/${teamId}/remove-manager`);
  }

  transferCaptaincy(teamId: number, newCaptainId: number): Observable<ApiResponse<Team>> {
    return this.http.post<ApiResponse<Team>>(`${this.apiUrl}/${teamId}/transfer-captaincy/${newCaptainId}`, {});
  }

  changeMemberRole(teamId: number, memberId: number, role: string): Observable<ApiResponse<void>> {
    return this.http.put<ApiResponse<void>>(`${this.apiUrl}/${teamId}/members/${memberId}/role?role=${role}`, {});
  }

  searchUsers(query: string): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.apiUrl}/search-users?query=${query}`);
  }

  invitePlayer(teamId: number, userId: number, message?: string): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.apiUrl}/${teamId}/invite`, {
      userId: userId,
      message: message || 'Join our team!'
    });
  }

  getTeamInvitations(teamId: number): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.apiUrl}/${teamId}/invitations`);
  }

  cancelInvitation(invitationId: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/invitations/${invitationId}/cancel`);
  }

  getPendingInvitations(): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.apiUrl}/invitations/pending`);
  }

  acceptInvitation(invitationId: number): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.apiUrl}/invitations/${invitationId}/accept`, {});
  }

  declineInvitation(invitationId: number): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.apiUrl}/invitations/${invitationId}/decline`, {});
  }

  leaveTeam(): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.apiUrl}/leave`, {});
  }

  updateTeamLogo(teamId: number, logoUrl: string): Observable<ApiResponse<any>> {
    return this.http.put<ApiResponse<any>>(`${this.apiUrl}/${teamId}/logo?logoUrl=${encodeURIComponent(logoUrl)}`, {});
  }

  uploadTeamLogo(file: File): Observable<ApiResponse<string>> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ApiResponse<string>>('http://localhost:8080/api/upload/team-logo', formData);
  }
}
