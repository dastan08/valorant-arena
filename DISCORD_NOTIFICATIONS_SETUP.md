# Discord Notifications Setup Guide

## Overview
The Valorant Arena now sends Discord DM notifications to users when they receive team invitations, and to team captains when invitations are accepted or declined.

## Features Implemented

### 1. **Team Invitation Notifications**
When a player is invited to a team:
- ✅ Receives a Discord DM with:
  - Team name
  - Who invited them
  - Custom invitation message
  - Link to accept/decline

### 2. **Invitation Accepted Notifications**
When a player accepts an invitation:
- ✅ Team captain receives a Discord DM
- ✅ Notification includes player name and team name

### 3. **Invitation Declined Notifications**
When a player declines an invitation:
- ✅ Team captain receives a Discord DM
- ✅ Notification includes player name and team name

## Setup Instructions

### Step 1: Create a Discord Bot

1. Go to [Discord Developer Portal](https://discord.com/developers/applications)
2. Click "New Application"
3. Give it a name (e.g., "Valorant Arena Bot")
4. Go to "Bot" section
5. Click "Add Bot"
6. **Copy the Bot Token** (you'll need this)
7. Enable these **Privileged Gateway Intents**:
   - ✅ Message Content Intent
   - ✅ Server Members Intent

### Step 2: Configure Bot Permissions

1. Go to "OAuth2" → "URL Generator"
2. Select these scopes:
   - ✅ `bot`
3. Select these bot permissions:
   - ✅ Send Messages
   - ✅ Read Messages/View Channels
4. Copy the generated URL and open it in browser
5. Invite the bot to your Discord server (optional, for testing)

### Step 3: Add Bot Token to Application

**Option 1: Environment Variable (Recommended)**
```bash
# Windows
set DISCORD_BOT_TOKEN=your_bot_token_here

# Linux/Mac
export DISCORD_BOT_TOKEN=your_bot_token_here
```

**Option 2: Update application.yml**
```yaml
discord:
  bot:
    token: your_bot_token_here
```

### Step 4: Restart Backend
```bash
mvn spring-boot:run
```

## How It Works

### Notification Flow

```
User Action                    →  Discord Notification
─────────────────────────────────────────────────────────
Captain invites player         →  Player receives DM
Player accepts invitation      →  Captain receives DM
Player declines invitation     →  Captain receives DM
```

### Requirements for Notifications

- ✅ User must have Discord account linked
- ✅ User must have `discordId` in database
- ✅ Bot must be able to DM the user
- ✅ User must allow DMs from server members

### Notification Format

**Invitation Received:**
```
🎮 **Team Invitation**

You've been invited to join **Team Phoenix**!

**Invited by:** PlayerName
**Message:** "Join us for competitive play!"

Visit Valorant Arena to accept or decline this invitation.
```

**Invitation Accepted:**
```
🎉 **Team Update**

**PlayerName** has accepted your invitation and joined **Team Phoenix**!
```

**Invitation Declined:**
```
❌ **Team Update**

**PlayerName** has declined your invitation to join **Team Phoenix**.
```

## Testing

### Test the Notifications

1. **Link Discord Account**
   - Login to Valorant Arena
   - Link your Discord account via OAuth

2. **Create a Team**
   - Create a team as captain

3. **Invite a Player**
   - Search for a player with Discord linked
   - Send invitation
   - Player should receive Discord DM

4. **Accept/Decline**
   - Player accepts or declines
   - Captain should receive Discord DM

## Troubleshooting

### No Notifications Received?

**Check:**
1. ✅ Discord account is linked (`discordId` exists in database)
2. ✅ Bot token is configured correctly
3. ✅ User allows DMs from server members
4. ✅ Check backend logs for errors

**Common Issues:**

**"Cannot send messages to this user"**
- User has DMs disabled
- User is not in a mutual server with the bot
- Solution: User must enable DMs or join a server with the bot

**"Invalid Bot Token"**
- Token is incorrect or expired
- Solution: Regenerate token in Discord Developer Portal

**"No notification sent"**
- Discord account not linked
- Solution: User must link Discord via OAuth

### Backend Logs

Check logs for Discord notification status:
```
INFO  - Discord notification sent to user: 123456789
ERROR - Failed to send Discord notification: Cannot send messages to this user
```

## Database Schema

The system uses existing Discord fields in the `users` table:
```sql
discord_id VARCHAR(255)        -- Discord user ID
discord_username VARCHAR(255)  -- Discord username
discord_avatar VARCHAR(255)    -- Discord avatar hash
```

## Security Notes

- ✅ Bot token is stored securely in environment variable
- ✅ Notifications only sent to linked Discord accounts
- ✅ No sensitive data included in notifications
- ✅ All Discord API calls are logged

## Future Enhancements

Potential additions:
- 🔔 Match found notifications
- 🏆 Tournament updates
- 📊 Team performance summaries
- 💬 Team chat integration

## API Endpoints

The Discord notification service is automatically triggered by:
- `POST /api/teams/{teamId}/invite` - Sends invitation notification
- `POST /api/teams/invitations/{id}/accept` - Sends acceptance notification
- `POST /api/teams/invitations/{id}/decline` - Sends decline notification

## Code Structure

```
DiscordNotificationService.java
├── sendDirectMessage()                    - Core DM sending
├── sendTeamInvitationNotification()       - Invitation DM
├── sendInvitationAcceptedNotification()   - Acceptance DM
└── sendInvitationDeclinedNotification()   - Decline DM
```

## Support

For issues or questions:
1. Check backend logs
2. Verify Discord bot configuration
3. Test with Discord Developer Portal
4. Check user's Discord privacy settings
