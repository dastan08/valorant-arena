import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/api-response.model';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private baseUrl = 'http://13.201.185.135:8080/api/admin';

  constructor(private http: HttpClient) {}

  // Dashboard Statistics
  getDashboardStats(): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/dashboard/stats`);
  }

  getRecentActivity(): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.baseUrl}/dashboard/activity`);
  }

  getSystemHealth(): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/dashboard/health`);
  }

  // User Management
  getAllUsers(page: number = 0, size: number = 20, search?: string): Observable<ApiResponse<any>> {
    let params = `page=${page}&size=${size}`;
    if (search) {
      params += `&search=${encodeURIComponent(search)}`;
    }
    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/users?${params}`);
  }

  getUserById(userId: number): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/users/${userId}`);
  }

  updateUser(userId: number, userData: any): Observable<ApiResponse<any>> {
    return this.http.put<ApiResponse<any>>(`${this.baseUrl}/users/${userId}`, userData);
  }

  banUser(userId: number, reason: string, duration?: number): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.baseUrl}/users/${userId}/ban`, { reason, duration });
  }

  unbanUser(userId: number): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.baseUrl}/users/${userId}/unban`, {});
  }

  deleteUser(userId: number): Observable<ApiResponse<any>> {
    return this.http.delete<ApiResponse<any>>(`${this.baseUrl}/users/${userId}`);
  }

  // Team Management
  getAllTeams(page: number = 0, size: number = 20, search?: string): Observable<ApiResponse<any>> {
    let params = `page=${page}&size=${size}`;
    if (search) {
      params += `&search=${encodeURIComponent(search)}`;
    }
    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/teams?${params}`);
  }

  getTeamById(teamId: number): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/teams/${teamId}`);
  }

  updateTeam(teamId: number, teamData: any): Observable<ApiResponse<any>> {
    return this.http.put<ApiResponse<any>>(`${this.baseUrl}/teams/${teamId}`, teamData);
  }

  disbandTeam(teamId: number, reason: string): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.baseUrl}/teams/${teamId}/disband`, { reason });
  }

  // Scrim Management
  getAllScrims(page: number = 0, size: number = 20, status?: string): Observable<ApiResponse<any>> {
    let params = `page=${page}&size=${size}`;
    if (status) {
      params += `&status=${status}`;
    }
    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/scrims?${params}`);
  }

  getScrimById(scrimId: number): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/scrims/${scrimId}`);
  }

  cancelScrim(scrimId: number, reason: string): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.baseUrl}/scrims/${scrimId}/cancel`, { reason });
  }

  // Report Management
  getAllReports(page: number = 0, size: number = 20, status?: string): Observable<ApiResponse<any>> {
    let params = `page=${page}&size=${size}`;
    if (status) {
      params += `&status=${status}`;
    }
    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/reports?${params}`);
  }

  getReportById(reportId: number): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/reports/${reportId}`);
  }

  resolveReport(reportId: number, action: string, notes?: string): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.baseUrl}/reports/${reportId}/resolve`, { action, notes });
  }

  // System Management
  getSystemLogs(page: number = 0, size: number = 50, level?: string): Observable<ApiResponse<any>> {
    let params = `page=${page}&size=${size}`;
    if (level) {
      params += `&level=${level}`;
    }
    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/system/logs?${params}`);
  }

  clearCache(): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.baseUrl}/system/clear-cache`, {});
  }

  restartServices(): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.baseUrl}/system/restart-services`, {});
  }

  // Analytics
  getUserGrowthStats(period: string = '30d'): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/analytics/user-growth?period=${period}`);
  }

  getTeamStats(): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/analytics/team-stats`);
  }

  getScrimStats(): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/analytics/scrim-stats`);
  }
}
