import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NotificationService, Notification } from '../../../core/services/notification.service';

@Component({
  selector: 'app-notification-bell',
  templateUrl: './notification-bell.component.html',
  styleUrls: ['./notification-bell.component.css']
})
export class NotificationBellComponent implements OnInit {
  notifications: Notification[] = [];
  unreadCount = 0;
  showDropdown = false;

  constructor(
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.notificationService.notifications$.subscribe(notifications => {
      this.notifications = notifications.slice(0, 5); // Show only latest 5
      this.unreadCount = notifications.filter(n => !n.read).length;
    });
  }

  toggleDropdown(): void {
    this.showDropdown = !this.showDropdown;
  }

  closeDropdown(): void {
    this.showDropdown = false;
  }

  markAsRead(notification: Notification, event: Event): void {
    event.stopPropagation();
    this.notificationService.markAsRead(notification.id);
  }

  deleteNotification(notification: Notification, event: Event): void {
    event.stopPropagation();
    this.notificationService.deleteNotification(notification.id);
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead();
  }

  viewAll(): void {
    this.closeDropdown();
    this.router.navigate(['/notifications']);
  }

  handleNotificationClick(notification: Notification): void {
    if (!notification.read) {
      this.notificationService.markAsRead(notification.id);
    }
    
    if (notification.actionUrl) {
      this.closeDropdown();
      this.router.navigate([notification.actionUrl]);
    }
  }

  getNotificationIcon(type: string): string {
    switch (type) {
      case 'scrim': return '⚔️';
      case 'invitation': return '📨';
      case 'team': return '👥';
      case 'match': return '🎮';
      case 'system': return '🔔';
      default: return '📢';
    }
  }

  getTimeAgo(timestamp: Date): string {
    const now = new Date();
    const notificationTime = new Date(timestamp);
    const diffMs = now.getTime() - notificationTime.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;
    return notificationTime.toLocaleDateString();
  }
}
