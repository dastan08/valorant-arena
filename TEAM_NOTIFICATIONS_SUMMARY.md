# Team Activity Notifications & Leave System

## Overview
Complete implementation of team activity notifications and leave team functionality with captaincy transfer requirements.

## Features Implemented

### 1. **Team Leave Functionality**

#### Rules:
- ✅ **Any member can leave** at any time
- ✅ **Captain must transfer captaincy first** before leaving (if team has multiple members)
- ✅ **Captain can disband team** if they're the only member
- ✅ **All remaining members notified** when someone leaves

#### UI:
```
Regular Member:
  [Leave Team] button (red)

Captain (multiple members):
  [Leave Team (Transfer Captaincy First)] button (orange)
  → Shows warning message
  → Must use "Transfer Captaincy" option first

Captain (only member):
  [Leave & Disband Team] button (red)
  → Confirms disbanding
  → Deletes team
```

---

### 2. **Discord Notifications for All Team Activities**

All team members receive Discord DM notifications for:

| Activity | Notification | Recipients |
|----------|-------------|------------|
| **New Member Joins** | "🎉 [Player] has joined [Team]!" | All members |
| **Member Leaves** | "👋 [Player] has left [Team]" | Remaining members |
| **Member Removed** | "🚫 [Player] removed by [Manager]" | Remaining members |
| **Captaincy Transferred** | "👑 [Old] transferred to [New]" | All members |
| **Manager Assigned** | "⭐ [Player] is now manager" | All members |
| **Manager Removed** | "📋 [Player] no longer manager" | All members |
| **Invitation Sent** | "🎮 You're invited to [Team]!" | Invited player |
| **Invitation Accepted** | "🎉 [Player] joined [Team]!" | Captain + All members |
| **Invitation Declined** | "❌ [Player] declined invitation" | Captain |

---

### 3. **Backend Implementation**

#### New Discord Notification Methods:

```java
// General notification to all members
notifyTeamMembers(List<User> members, String message)

// Specific notifications
sendMemberLeftNotification(members, playerName, teamName)
sendMemberRemovedNotification(members, removedPlayer, removedBy, teamName)
sendCaptaincyTransferredNotification(members, newCaptain, oldCaptain, teamName)
sendManagerAssignedNotification(members, managerName, teamName)
sendManagerRemovedNotification(members, managerName, teamName)
sendNewMemberJoinedNotification(members, playerName, teamName)
```

#### Updated Team Management Methods:

All team management methods now send notifications:
- ✅ `acceptInvitation()` - Notifies captain + all members
- ✅ `leaveTeam()` - Notifies remaining members
- ✅ `removeMemberFromTeam()` - Notifies remaining members
- ✅ `assignManager()` - Notifies all members
- ✅ `removeManager()` - Notifies all members
- ✅ `transferCaptaincy()` - Notifies all members

---

### 4. **Leave Team Logic**

```java
@Transactional
public void leaveTeam(Long userId) {
    // Check if user is in a team
    // If captain with multiple members → REJECT
    // If captain alone → Disband team
    // If regular member → Leave + notify others
}
```

**Flow:**
```
Regular Member Leaves:
  ↓
Remove from team
  ↓
Notify remaining members
  ↓
Success

Captain Leaves (multiple members):
  ↓
Check if captain
  ↓
REJECT: "Must transfer captaincy first"

Captain Leaves (only member):
  ↓
Delete team
  ↓
Remove user from team
  ↓
Success (team disbanded)
```

---

### 5. **Frontend Features**

#### Leave Team Button:
- **Location:** Team detail dialog footer
- **Visibility:** Only shown to team members
- **Behavior:**
  - Regular members: Direct leave
  - Captain (multiple members): Warning message
  - Captain (only member): Disband confirmation

#### Service Method:
```typescript
leaveTeam(): Observable<ApiResponse<void>>
```

#### Component Methods:
```typescript
isTeamMember(): boolean
leaveTeam(): void
showCaptainLeaveWarning(): void
```

---

## API Endpoints

### Leave Team
```
POST /api/teams/leave
Authorization: Bearer <token>

Response:
{
  "success": true,
  "message": "You have left the team",
  "data": null
}

Error (Captain):
{
  "success": false,
  "message": "Captain must transfer captaincy to another member before leaving the team"
}
```

---

## Notification Examples

### Member Joins
```
🎉 Team Update

rohitbiswas1 has joined Team Phoenix! Welcome to the team!
```

### Member Leaves
```
👋 Team Update

rohitbiswas1 has left Team Phoenix.
```

### Member Removed
```
🚫 Team Update

rohitbiswas1 has been removed from Team Phoenix by Dastan08.
```

### Captaincy Transferred
```
👑 Team Update

Dastan08 has transferred captaincy of Team Phoenix to rohitbiswas1.
```

### Manager Assigned
```
⭐ Team Update

rohitbiswas1 has been assigned as manager of Team Phoenix.
```

### Manager Removed
```
📋 Team Update

rohitbiswas1 is no longer the manager of Team Phoenix.
```

---

## User Flows

### Regular Member Leaves Team

1. Open team detail dialog
2. Click "Leave Team" button
3. Confirm action
4. User removed from team
5. Remaining members get Discord notification
6. Page refreshes

### Captain Tries to Leave (Multiple Members)

1. Open team detail dialog
2. See "Leave Team (Transfer Captaincy First)" button
3. Click button
4. See warning: "Must transfer captaincy first"
5. Go to Members section
6. Click "Transfer Captaincy" on a member
7. Select new captain
8. Now can leave team as regular member

### Captain Leaves (Only Member)

1. Open team detail dialog
2. See "Leave & Disband Team" button
3. Click button
4. Confirm: "Are you sure you want to disband?"
5. Team deleted
6. User removed from team
7. Page refreshes

---

## Testing Checklist

### Leave Team:
- [ ] Regular member can leave team
- [ ] Remaining members get notification
- [ ] Captain cannot leave with multiple members
- [ ] Captain can leave if only member
- [ ] Team is disbanded when captain leaves alone

### Notifications:
- [ ] New member join → All members notified
- [ ] Member leaves → Remaining members notified
- [ ] Member removed → Remaining members notified
- [ ] Captaincy transferred → All members notified
- [ ] Manager assigned → All members notified
- [ ] Manager removed → All members notified

### UI:
- [ ] Leave button shows for team members
- [ ] Leave button hidden for non-members
- [ ] Captain sees appropriate button text
- [ ] Confirmation dialogs work
- [ ] Page refreshes after leaving

---

## Database Changes

No schema changes required. Uses existing:
- `users.team_id` - Team membership
- `users.team_role` - User role
- `users.discord_id` - For notifications

---

## Error Handling

All notification failures are caught and logged:
```java
try {
    discordNotificationService.sendMemberLeftNotification(...);
} catch (Exception e) {
    log.error("Failed to send Discord notifications: {}", e.getMessage());
}
```

**Important:** Notification failures don't block the main operation. If Discord is down, the team action still completes successfully.

---

## Configuration

No additional configuration needed. Uses existing:
```yaml
discord:
  bot:
    token: YOUR_BOT_TOKEN
```

---

## Benefits

1. **Transparency:** All members know what's happening in the team
2. **Real-time:** Instant Discord notifications
3. **Safety:** Captain can't accidentally leave without transferring
4. **Flexibility:** Members can leave anytime
5. **Community:** Keeps everyone informed and engaged

---

## Future Enhancements

Potential additions:
- 📧 Email notifications (in addition to Discord)
- 🔔 In-app notification center
- 📊 Team activity log/history
- ⏰ Scheduled notifications digest
- 🎯 Customizable notification preferences
- 📱 Mobile push notifications

---

## Files Modified

### Backend:
1. `DiscordNotificationService.java` - Added team activity notifications
2. `TeamManagementService.java` - Updated all methods with notifications
3. `TeamManagementService.java` - Enhanced leave team logic

### Frontend:
1. `team.service.ts` - Added leaveTeam() method
2. `team-detail-dialog.component.html` - Added leave team buttons
3. `team-detail-dialog.component.ts` - Added leave team methods

---

## Summary

✅ **Complete team activity notification system**
✅ **Enforced captaincy transfer before leaving**
✅ **All members notified of all team changes**
✅ **Flexible leave system for all members**
✅ **Discord integration for real-time updates**
✅ **User-friendly UI with clear messaging**

The team is now a living, breathing entity where all members stay informed of every change! 🎮
