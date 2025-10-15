import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ValorantService {
  private apiUrl = 'http://13.201.185.135:8080/api/valorant';

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
