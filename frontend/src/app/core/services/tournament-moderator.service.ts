import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../config/environment';

export interface TournamentModeratorRequest {
  userId: number;
  notes?: string;
}

export interface TournamentModeratorResponse {
  id: number;
  userId: number;
  username: string;
  email: string;
  assignedById: number;
  assignedByUsername: string;
  assignedAt: string;
  notes?: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class TournamentModeratorService {
  private apiUrl = `${environment.apiUrl}/tournaments`;

  constructor(private http: HttpClient) {}

  assignModerator(tournamentId: number, request: TournamentModeratorRequest): Observable<ApiResponse<TournamentModeratorResponse>> {
    return this.http.post<ApiResponse<TournamentModeratorResponse>>(
      `${this.apiUrl}/${tournamentId}/moderators`,
      request
    );
  }

  removeModerator(tournamentId: number, userId: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(
      `${this.apiUrl}/${tournamentId}/moderators/${userId}`
    );
  }

  getTournamentModerators(tournamentId: number): Observable<ApiResponse<TournamentModeratorResponse[]>> {
    return this.http.get<ApiResponse<TournamentModeratorResponse[]>>(
      `${this.apiUrl}/${tournamentId}/moderators`
    );
  }

  checkModerator(tournamentId: number, userId: number): Observable<ApiResponse<boolean>> {
    return this.http.get<ApiResponse<boolean>>(
      `${this.apiUrl}/${tournamentId}/moderators/check/${userId}`
    );
  }
}
