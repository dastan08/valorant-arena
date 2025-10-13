import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface Notification {
  id: string;
  type: 'scrim' | 'team' | 'invitation' | 'match' | 'system';
  title: string;
  message: string;
  timestamp: Date;
  read: boolean;
  actionUrl?: string;
  data?: any;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private notifications = new BehaviorSubject<Notification[]>([]);
  public notifications$ = this.notifications.asObservable();

  constructor() {
    this.loadNotifications();
  }

  private loadNotifications(): void {
    const stored = localStorage.getItem('notifications');
    if (stored) {
      try {
        const notifications = JSON.parse(stored);
        this.notifications.next(notifications);
      } catch (e) {
        console.error('Error loading notifications', e);
      }
    }
  }

  private saveNotifications(): void {
    localStorage.setItem('notifications', JSON.stringify(this.notifications.value));
  }

  addNotification(notification: Omit<Notification, 'id' | 'timestamp' | 'read'>): void {
    const newNotification: Notification = {
      ...notification,
      id: this.generateId(),
      timestamp: new Date(),
      read: false
    };

    const current = this.notifications.value;
    this.notifications.next([newNotification, ...current]);
    this.saveNotifications();
  }

  markAsRead(notificationId: string): void {
    const current = this.notifications.value;
    const updated = current.map(n => 
      n.id === notificationId ? { ...n, read: true } : n
    );
    this.notifications.next(updated);
    this.saveNotifications();
  }

  markAllAsRead(): void {
    const current = this.notifications.value;
    const updated = current.map(n => ({ ...n, read: true }));
    this.notifications.next(updated);
    this.saveNotifications();
  }

  deleteNotification(notificationId: string): void {
    const current = this.notifications.value;
    const updated = current.filter(n => n.id !== notificationId);
    this.notifications.next(updated);
    this.saveNotifications();
  }

  clearAll(): void {
    this.notifications.next([]);
    this.saveNotifications();
  }

  getUnreadCount(): Observable<number> {
    return new Observable(observer => {
      this.notifications$.subscribe(notifications => {
        const unreadCount = notifications.filter(n => !n.read).length;
        observer.next(unreadCount);
      });
    });
  }

  private generateId(): string {
    return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
  }

  // Scrim-specific notification helpers
  notifyScrimChallenge(challengerTeamName: string, scrimId: number, scheduledTime: string): void {
    this.addNotification({
      type: 'scrim',
      title: '⚔️ New Scrim Challenge',
      message: `${challengerTeamName} has challenged your team to a scrim!`,
      actionUrl: '/scrims',
      data: { scrimId, scheduledTime }
    });
  }

  notifyScrimAccepted(opponentTeamName: string, scrimId: number): void {
    this.addNotification({
      type: 'scrim',
      title: '✅ Scrim Accepted',
      message: `${opponentTeamName} has accepted your scrim challenge!`,
      actionUrl: '/scrims',
      data: { scrimId }
    });
  }

  notifyScrimDeclined(opponentTeamName: string, scrimId: number, reason?: string): void {
    this.addNotification({
      type: 'scrim',
      title: '❌ Scrim Declined',
      message: `${opponentTeamName} has declined your scrim challenge.${reason ? ` Reason: ${reason}` : ''}`,
      actionUrl: '/scrims',
      data: { scrimId }
    });
  }

  notifyScrimCancelled(teamName: string, scrimId: number): void {
    this.addNotification({
      type: 'scrim',
      title: '🚫 Scrim Cancelled',
      message: `The scrim with ${teamName} has been cancelled.`,
      actionUrl: '/scrims',
      data: { scrimId }
    });
  }

  notifyScrimReminder(opponentTeamName: string, scrimId: number, timeUntil: string): void {
    this.addNotification({
      type: 'scrim',
      title: '⏰ Scrim Reminder',
      message: `Your scrim with ${opponentTeamName} starts in ${timeUntil}!`,
      actionUrl: '/scrims',
      data: { scrimId }
    });
  }

  // Team invitation notification helpers
  notifyTeamInvitation(teamName: string, invitationId: number): void {
    this.addNotification({
      type: 'invitation',
      title: '📨 Team Invitation',
      message: `You've been invited to join ${teamName}!`,
      actionUrl: '/invitations',
      data: { invitationId }
    });
  }

  notifyInvitationAccepted(username: string, teamName: string): void {
    this.addNotification({
      type: 'team',
      title: '✅ Invitation Accepted',
      message: `${username} has joined ${teamName}!`,
      actionUrl: '/teams',
      data: { username }
    });
  }

  notifyInvitationDeclined(username: string, teamName: string): void {
    this.addNotification({
      type: 'team',
      title: '❌ Invitation Declined',
      message: `${username} has declined the invitation to ${teamName}.`,
      actionUrl: '/teams',
      data: { username }
    });
  }
}
