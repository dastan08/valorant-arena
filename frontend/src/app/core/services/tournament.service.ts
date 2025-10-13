import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Tournament } from '../models/tournament.model';
import { ApiResponse } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class TournamentService {
  private apiUrl = 'http://localhost:8080/api/tournaments';

  constructor(private http: HttpClient) { }

  getAllTournaments(): Observable<ApiResponse<Tournament[]>> {
    return this.http.get<ApiResponse<Tournament[]>>(this.apiUrl);
  }

  getMyTournaments(): Observable<ApiResponse<Tournament[]>> {
    return this.http.get<ApiResponse<Tournament[]>>(`${this.apiUrl}/my-tournaments`);
  }

  getTournamentById(id: number): Observable<ApiResponse<Tournament>> {
    return this.http.get<ApiResponse<Tournament>>(`${this.apiUrl}/${id}`);
  }

  getUpcomingTournaments(): Observable<ApiResponse<Tournament[]>> {
    return this.http.get<ApiResponse<Tournament[]>>(`${this.apiUrl}/upcoming`);
  }

  getActiveTournaments(): Observable<ApiResponse<Tournament[]>> {
    return this.http.get<ApiResponse<Tournament[]>>(`${this.apiUrl}/active`);
  }

  createTournament(tournament: Partial<Tournament>): Observable<ApiResponse<Tournament>> {
    return this.http.post<ApiResponse<Tournament>>(this.apiUrl, tournament);
  }

  updateTournament(id: number, tournament: Partial<Tournament>): Observable<ApiResponse<Tournament>> {
    return this.http.put<ApiResponse<Tournament>>(`${this.apiUrl}/${id}`, tournament);
  }

  deleteTournament(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }

  publishTournament(id: number): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.apiUrl}/${id}/publish`, {});
  }

  registerForTournament(tournamentId: number, registrationData: { entryType: string, teamId?: number }): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.apiUrl}/${tournamentId}/register`, registrationData);
  }
  
  registerTeam(tournamentId: number, teamId: number): Observable<ApiResponse<Tournament>> {
    return this.http.post<ApiResponse<Tournament>>(`${this.apiUrl}/${tournamentId}/register`, {
      entryType: 'TEAM',
      teamId: teamId
    });
  }

  unregisterTeam(tournamentId: number, teamId: number): Observable<ApiResponse<Tournament>> {
    return this.http.delete<ApiResponse<Tournament>>(`${this.apiUrl}/${tournamentId}/unregister/${teamId}`);
  }

  searchTournaments(searchTerm: string): Observable<ApiResponse<Tournament[]>> {
    const params = new HttpParams().set('search', searchTerm);
    return this.http.get<ApiResponse<Tournament[]>>(`${this.apiUrl}/search`, { params });
  }

  // Bracket endpoints
  generateBracket(tournamentId: number): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`http://localhost:8080/api/brackets/tournaments/${tournamentId}/generate`, {});
  }

  getBracket(tournamentId: number): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`http://localhost:8080/api/brackets/tournaments/${tournamentId}`);
  }

  // Match endpoints
  getMatches(tournamentId: number): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`http://localhost:8080/api/matches/tournament/${tournamentId}`);
  }

  getMyMatches(): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`http://localhost:8080/api/matches/my-matches`);
  }

  reportScore(matchId: number, scoreData: any): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`http://localhost:8080/api/matches/${matchId}/report-score`, scoreData);
  }

  confirmScore(matchId: number): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`http://localhost:8080/api/matches/${matchId}/confirm-score`, {});
  }

  startMatch(matchId: number): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`http://localhost:8080/api/matches/${matchId}/start`, {});
  }
}
