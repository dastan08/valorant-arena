import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/auth.model';
import { environment } from '../config/environment';

export interface Scrim {
  id: number;
  challengerTeam: any;
  opponentTeam: any;
  scheduledTime: string;
  status: 'PENDING' | 'ACCEPTED' | 'DECLINED' | 'CANCELLED' | 'IN_PROGRESS' | 'COMPLETED';
  message?: string;
  map: string;
  bestOf: number;
  result?: 'CHALLENGER_WIN' | 'OPPONENT_WIN' | 'DRAW' | 'CANCELLED';
  challengerScore?: number;
  opponentScore?: number;
  createdAt: string;
  updatedAt: string;
  respondedAt?: string;
  completedAt?: string;
}

export interface ChallengeScrimRequest {
  challengerTeamId: number;
  opponentTeamId: number;
  scheduledTime: string;
  message?: string;
  map: string;
  bestOf: number;
}

@Injectable({
  providedIn: 'root'
})
export class ScrimService {
  private apiUrl = `${environment.apiUrl}/scrims`;

  constructor(private http: HttpClient) {}

  challengeTeam(request: ChallengeScrimRequest): Observable<ApiResponse<Scrim>> {
    return this.http.post<ApiResponse<Scrim>>(`${this.apiUrl}/challenge`, request);
  }

  acceptScrim(scrimId: number): Observable<ApiResponse<Scrim>> {
    return this.http.post<ApiResponse<Scrim>>(`${this.apiUrl}/${scrimId}/accept`, {});
  }

  declineScrim(scrimId: number, reason?: string): Observable<ApiResponse<Scrim>> {
    return this.http.post<ApiResponse<Scrim>>(`${this.apiUrl}/${scrimId}/decline`, { reason });
  }

  cancelScrim(scrimId: number, reason?: string): Observable<ApiResponse<Scrim>> {
    return this.http.post<ApiResponse<Scrim>>(`${this.apiUrl}/${scrimId}/cancel`, { reason });
  }

  getTeamScrims(teamId: number): Observable<ApiResponse<Scrim[]>> {
    return this.http.get<ApiResponse<Scrim[]>>(`${this.apiUrl}/team/${teamId}`);
  }

  getPendingScrims(teamId: number): Observable<ApiResponse<Scrim[]>> {
    return this.http.get<ApiResponse<Scrim[]>>(`${this.apiUrl}/team/${teamId}/pending`);
  }

  getUpcomingScrims(teamId: number): Observable<ApiResponse<Scrim[]>> {
    return this.http.get<ApiResponse<Scrim[]>>(`${this.apiUrl}/team/${teamId}/upcoming`);
  }

  getScrimById(scrimId: number): Observable<ApiResponse<Scrim>> {
    return this.http.get<ApiResponse<Scrim>>(`${this.apiUrl}/${scrimId}`);
  }
}
