import { Component, OnInit, OnDestroy } from '@angular/core';
import { MatchmakingService } from '../../services/matchmaking.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-matchmaking',
  templateUrl: './matchmaking.component.html',
  styleUrls: ['./matchmaking.component.css']
})
export class MatchmakingComponent implements OnInit, OnDestroy {
  inQueue = false;
  queueStatus: any = null;
  queueStats: any = null;
  selectedRegion = 'NA';
  private statusSubscription?: Subscription;
  private statsInterval?: any;

  constructor(
    private matchmakingService: MatchmakingService
  ) {}

  ngOnInit(): void {
    this.loadQueueStats();
    // Refresh stats every 5 seconds
    this.statsInterval = setInterval(() => {
      if (!this.inQueue) {
        this.loadQueueStats();
      }
    }, 5000);
  }

  ngOnDestroy(): void {
    if (this.inQueue) {
      this.leaveQueue();
    }
    this.stopPolling();
    if (this.statsInterval) {
      clearInterval(this.statsInterval);
    }
  }

  joinQueue(): void {
    this.matchmakingService.joinQueue(this.selectedRegion).subscribe({
      next: (response) => {
        if (response.success) {
          this.inQueue = true;
          this.queueStatus = response.data;
          this.startPolling();
        }
      },
      error: (error) => {
        console.error('Failed to join queue:', error);
        alert('Failed to join queue. Please try again.');
      }
    });
  }

  leaveQueue(): void {
    this.matchmakingService.leaveQueue().subscribe({
      next: (response) => {
        this.inQueue = false;
        this.queueStatus = null;
        this.stopPolling();
        this.loadQueueStats();
      },
      error: (error) => {
        console.error('Failed to leave queue:', error);
      }
    });
  }

  private startPolling(): void {
    this.statusSubscription = this.matchmakingService.pollQueueStatus().subscribe({
      next: (response) => {
        if (response.success) {
          this.queueStatus = response.data;
          
          if (this.queueStatus.status === 'MATCH_FOUND') {
            this.stopPolling();
            alert('Match found! Redirecting to match...');
            // TODO: Redirect to match page
          }
        }
      },
      error: () => {
        // Player likely not in queue anymore
        this.inQueue = false;
        this.queueStatus = null;
        this.stopPolling();
      }
    });
  }

  private stopPolling(): void {
    if (this.statusSubscription) {
      this.statusSubscription.unsubscribe();
    }
  }

  private loadQueueStats(): void {
    this.matchmakingService.getQueueStats(this.selectedRegion).subscribe({
      next: (response) => {
        if (response.success) {
          this.queueStats = response.data;
        }
      },
      error: (error) => {
        console.error('Failed to load queue stats:', error);
      }
    });
  }

  getWaitTime(): string {
    if (!this.queueStatus || !this.queueStatus.queuedAt) {
      return '0s';
    }
    
    const queuedAt = new Date(this.queueStatus.queuedAt);
    const now = new Date();
    const diffSeconds = Math.floor((now.getTime() - queuedAt.getTime()) / 1000);
    
    if (diffSeconds < 60) {
      return `${diffSeconds}s`;
    }
    
    const minutes = Math.floor(diffSeconds / 60);
    const seconds = diffSeconds % 60;
    return `${minutes}m ${seconds}s`;
  }

  getWaitTimeSeconds(): number {
    if (!this.queueStatus || !this.queueStatus.queuedAt) {
      return 0;
    }
    
    const queuedAt = new Date(this.queueStatus.queuedAt);
    const now = new Date();
    return Math.floor((now.getTime() - queuedAt.getTime()) / 1000);
  }

  // Make Math available in template
  Math = Math;
}
