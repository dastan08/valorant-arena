# Discord Notification Troubleshooting Guide

## ❌ Issue: "Cannot send messages to this user" (Error 50007)

This is the most common Discord notification error. It means the bot cannot send DMs to the user.

### Why This Happens:

1. **User has DMs disabled from server members**
2. **User doesn't share a server with the bot**
3. **User has blocked the bot**
4. **User's privacy settings block DMs**

### Solutions:

#### Option 1: User Enables DMs (Recommended)

**For the invited player:**

1. Open Discord
2. Go to **User Settings** (gear icon)
3. Navigate to **Privacy & Safety**
4. Enable **"Allow direct messages from server members"**
5. Try the invitation again

#### Option 2: Join a Mutual Server

**Both the bot and user must be in the same Discord server:**

1. Create a Discord server for Valorant Arena
2. Invite the bot to the server (use OAuth URL from Discord Developer Portal)
3. Invite all users to join the server
4. Users can now receive DMs from the bot

**Bot Invite URL Format:**
```
https://discord.com/api/oauth2/authorize?client_id=YOUR_BOT_CLIENT_ID&permissions=2048&scope=bot
```

Replace `YOUR_BOT_CLIENT_ID` with your bot's client ID from Discord Developer Portal.

#### Option 3: Alternative Notification Method

If Discord DMs don't work, users will still see notifications:
- ✅ In-app notification bell (navbar)
- ✅ Invitations page (`/invitations`)
- ✅ Email notifications (if implemented)

---

## 🔍 How to Check if Notifications Work

### Step 1: Verify Discord Link

1. Login to Valorant Arena
2. Check if Discord is linked (profile page)
3. Verify `discordId` exists in database:

```sql
SELECT id, username, discord_id, discord_username 
FROM users 
WHERE discord_id IS NOT NULL;
```

### Step 2: Test Bot Access

**Test if bot can DM a user:**

1. Go to Discord Developer Portal
2. Navigate to your bot
3. Use Discord's API test tool or:
   - Have the user send a message to the bot first
   - Bot can then reply (this establishes DM channel)

### Step 3: Check Backend Logs

Look for these log messages:

**Success:**
```
✅ Discord notification sent successfully to user: 255973321403793410
```

**Failure:**
```
❌ Cannot send DM: User has DMs disabled or doesn't share a server with the bot
```

**Not Configured:**
```
Discord bot token not configured. Skipping notification.
```

---

## 🛠️ Common Issues & Fixes

### Issue 1: Bot Token Invalid

**Symptoms:**
- Error: "401 Unauthorized"
- Logs show authentication errors

**Fix:**
1. Go to Discord Developer Portal
2. Regenerate bot token
3. Update `application.yml`:
```yaml
discord:
  bot:
    token: YOUR_NEW_BOT_TOKEN
```
4. Restart backend

### Issue 2: Bot Missing Permissions

**Symptoms:**
- Error: "403 Forbidden"
- Bot can't send messages

**Fix:**
1. Go to Discord Developer Portal → Bot
2. Enable these permissions:
   - ✅ Send Messages
   - ✅ Read Messages/View Channels
3. Reinvite bot to server with new permissions

### Issue 3: Discord Account Not Linked

**Symptoms:**
- No notification sent
- Logs show: "User does not have Discord linked"

**Fix:**
1. User must link Discord account
2. Go to Profile → Link Discord
3. Complete OAuth flow
4. Verify `discordId` is saved

### Issue 4: Rate Limiting

**Symptoms:**
- Error: "429 Too Many Requests"
- Notifications stop working temporarily

**Fix:**
- Discord has rate limits (5 DMs per 5 seconds)
- Wait a few seconds and try again
- Implement rate limiting in code (future enhancement)

---

## 📋 Pre-Flight Checklist

Before testing notifications:

- [ ] Discord bot created and token copied
- [ ] Bot token added to `application.yml`
- [ ] Backend restarted after adding token
- [ ] Bot invited to a Discord server
- [ ] Test user joined the same Discord server
- [ ] Test user has Discord account linked in Valorant Arena
- [ ] Test user has DMs enabled in Discord privacy settings
- [ ] Backend logs show no errors

---

## 🧪 Testing Procedure

### Test 1: Basic DM Test

1. **Setup:**
   - User A: Has Discord linked, DMs enabled
   - User B: Has Discord linked, DMs enabled
   - Both in same Discord server as bot

2. **Test:**
   - User A creates a team
   - User A invites User B
   - Check User B's Discord DMs

3. **Expected Result:**
   - User B receives invitation DM
   - Backend logs: "✅ Discord notification sent successfully"

### Test 2: Accept/Decline Notifications

1. **Test Accept:**
   - User B accepts invitation
   - Check User A's Discord DMs
   - Should receive "Player joined team" notification

2. **Test Decline:**
   - User B declines invitation
   - Check User A's Discord DMs
   - Should receive "Player declined" notification

---

## 🔧 Debug Commands

### Check Discord ID in Database

```sql
SELECT 
    u.id,
    u.username,
    u.discord_id,
    u.discord_username,
    t.name as team_name
FROM users u
LEFT JOIN teams t ON u.team_id = t.id
WHERE u.discord_id IS NOT NULL;
```

### Check Pending Invitations

```sql
SELECT 
    ti.id,
    ti.status,
    ti.created_at,
    t.name as team_name,
    invited.username as invited_user,
    invited.discord_id as invited_discord_id,
    inviter.username as inviter
FROM team_invitations ti
JOIN teams t ON ti.team_id = t.id
JOIN users invited ON ti.invited_user_id = invited.id
JOIN users inviter ON ti.invited_by_id = inviter.id
WHERE ti.status = 'PENDING';
```

### Test Bot Token

```bash
# Test bot token with curl (Windows PowerShell)
$headers = @{
    "Authorization" = "Bot YOUR_BOT_TOKEN"
}
Invoke-RestMethod -Uri "https://discord.com/api/v10/users/@me" -Headers $headers
```

---

## 📞 Support Resources

- **Discord Developer Portal:** https://discord.com/developers/applications
- **Discord API Docs:** https://discord.com/developers/docs/intro
- **Discord Error Codes:** https://discord.com/developers/docs/topics/opcodes-and-status-codes

---

## 💡 Alternative Solutions

If Discord DMs continue to fail:

### Option A: Discord Webhook (Server Channel)

Instead of DMs, post notifications to a Discord server channel:

1. Create a channel: `#team-invitations`
2. Create webhook for the channel
3. Post notifications there
4. Users get @mentions

### Option B: In-App Only

Disable Discord notifications and rely on:
- In-app notification bell
- Email notifications
- Browser push notifications (future)

### Option C: Discord Bot Commands

Users can query their invitations via bot commands:
```
/invitations - Check pending invitations
/accept <id> - Accept invitation
/decline <id> - Decline invitation
```

---

## 🎯 Best Practices

1. **Always check if Discord is linked** before attempting notification
2. **Log all notification attempts** for debugging
3. **Don't fail the main operation** if Discord notification fails
4. **Provide fallback notification methods** (in-app, email)
5. **Inform users** about Discord DM requirements during onboarding
6. **Create a mutual server** for all Valorant Arena users

---

## ✅ Success Indicators

Notifications are working correctly when:

- ✅ Backend logs show "Discord notification sent successfully"
- ✅ Users receive DMs in Discord within 1-2 seconds
- ✅ No 403/50007 errors in logs
- ✅ Both invitation and response notifications work
- ✅ Notifications include correct team/player names
