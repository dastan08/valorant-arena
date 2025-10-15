import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, interval } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { environment } from '../core/config/environment';

export interface MatchmakingResponse {
  queueId: number;
  status: string;
  playerElo: number;
  playerRank: string;
  queuedAt: string;
  estimatedWaitTime: number;
  matchDetails?: any;
}

export interface QueueStats {
  playersInQueue: number;
  estimatedWaitTime: number;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class MatchmakingService {
  private apiUrl = `${environment.apiUrl}/matchmaking`;

  constructor(private http: HttpClient) {}

  joinQueue(region: string = 'NA'): Observable<ApiResponse<MatchmakingResponse>> {
    return this.http.post<ApiResponse<MatchmakingResponse>>(`${this.apiUrl}/join`, { region });
  }

  leaveQueue(): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.apiUrl}/leave`, {});
  }

  getQueueStatus(): Observable<ApiResponse<MatchmakingResponse>> {
    return this.http.get<ApiResponse<MatchmakingResponse>>(`${this.apiUrl}/status`);
  }

  getQueueStats(region: string = 'NA'): Observable<ApiResponse<QueueStats>> {
    return this.http.get<ApiResponse<QueueStats>>(`${this.apiUrl}/stats`, { params: { region } });
  }

  // Poll queue status every 2 seconds
  pollQueueStatus(): Observable<ApiResponse<MatchmakingResponse>> {
    return interval(2000).pipe(
      switchMap(() => this.getQueueStatus())
    );
  }
}
