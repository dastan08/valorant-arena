import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../core/config/environment';

@Injectable({
  providedIn: 'root'
})
export class ValorantService {
  private apiUrl = `${environment.apiUrl}/valorant`;

  constructor(private http: HttpClient) {}

  /**
   * Get player stats and MMR
   */
  getPlayerStats(gameName: string, tagLine: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/stats/${gameName}/${tagLine}`);
  }

  /**
   * Get match history for a player
   */
  getMatchHistory(gameName: string, tagLine: string, region: string = 'na'): Observable<any> {
    return this.http.get(`${this.apiUrl}/matches/${gameName}/${tagLine}?region=${region}`);
  }

  /**
   * Get regional leaderboard
   */
  getLeaderboard(region: string = 'na'): Observable<any> {
    return this.http.get(`${this.apiUrl}/leaderboard/${region}`);
  }
}
