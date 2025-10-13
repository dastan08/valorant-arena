# Discord Avatar Integration

## ✅ Feature Implemented

Users who login with Discord will automatically have their Discord profile picture displayed on their profile page if they haven't uploaded a custom avatar.

## 🎯 How It Works

### Priority Order:
1. **Custom Avatar** - If user uploads their own avatar
2. **Discord Avatar** - If Discord is linked and has avatar
3. **Default Avatar** - Fallback placeholder

### Backend Implementation

#### User Entity (`User.java`)
```java
@Transient
private String profilePictureUrl;

public String getProfilePictureUrl() {
    // If user has custom avatar (not default), use it
    if (avatar != null && !avatar.equals("default-avatar.png")) {
        return "/uploads/avatars/" + avatar;
    }
    
    // If Discord is linked and has avatar, use Discord CDN
    if (discordId != null && discordAvatar != null) {
        return "https://cdn.discordapp.com/avatars/" + discordId + "/" + discordAvatar + ".png";
    }
    
    // Fallback to default avatar
    return "/assets/default-avatar.png";
}
```

#### UserService
- Automatically sets `profilePictureUrl` when fetching user profile
- Returns computed URL in API response

### Frontend Implementation

#### Profile Component (`profile.component.html`)
```html
<img [src]="currentUser?.profilePictureUrl || 'assets/default-avatar.png'" 
     alt="Avatar" 
     class="w-24 h-24 rounded-full mr-6 border-2 border-valorant-red"
     onerror="this.src='assets/default-avatar.png'">
```

**Features:**
- Displays Discord avatar with red border
- Shows Discord username with icon below profile
- Fallback to default avatar if Discord image fails to load
- Responsive design

## 🎨 Discord CDN URL Format

```
https://cdn.discordapp.com/avatars/{discord_id}/{avatar_hash}.png
```

**Example:**
```
https://cdn.discordapp.com/avatars/123456789012345678/a1b2c3d4e5f6g7h8i9j0.png
```

## 📊 User Flow

### New Discord User:
1. User clicks "Sign in with Discord"
2. Authorizes on Discord
3. Account created with Discord info
4. `discordId` and `discordAvatar` saved
5. Profile automatically shows Discord avatar

### Existing User:
1. User has default avatar
2. Links Discord account
3. Profile updates to show Discord avatar
4. Can still upload custom avatar later

## 🔧 API Response

### GET /api/user/profile
```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "Player123",
    "email": "user@example.com",
    "avatar": "default-avatar.png",
    "discordId": "123456789012345678",
    "discordUsername": "DiscordUser#1234",
    "discordAvatar": "a1b2c3d4e5f6g7h8i9j0",
    "profilePictureUrl": "https://cdn.discordapp.com/avatars/123456789012345678/a1b2c3d4e5f6g7h8i9j0.png"
  }
}
```

## 🎯 Benefits

1. **Better UX** - Users see their familiar Discord avatar immediately
2. **No Upload Required** - Automatic profile picture for Discord users
3. **Consistent Identity** - Same avatar across platforms
4. **Fallback Safety** - Always has a valid image URL
5. **Custom Override** - Users can still upload custom avatars

## 🔄 Future Enhancements

### Avatar Upload Feature:
```java
@PostMapping("/upload-avatar")
public ResponseEntity<ApiResponse> uploadAvatar(
    @RequestParam("file") MultipartFile file,
    @AuthenticationPrincipal UserDetailsImpl userDetails) {
    // Save file to /uploads/avatars/
    // Update user.avatar field
    // Return new profilePictureUrl
}
```

### Avatar Size Options:
Discord CDN supports different sizes:
- `?size=16` - 16x16
- `?size=32` - 32x32
- `?size=64` - 64x64
- `?size=128` - 128x128
- `?size=256` - 256x256
- `?size=512` - 512x512
- `?size=1024` - 1024x1024

Example:
```
https://cdn.discordapp.com/avatars/{id}/{hash}.png?size=128
```

## 📝 Files Modified

### Backend:
- ✅ `User.java` - Added `profilePictureUrl` field and `getProfilePictureUrl()` method
- ✅ `UserService.java` - Sets profilePictureUrl when fetching user

### Frontend:
- ✅ `profile.component.html` - Updated avatar display with Discord support
- ✅ Shows Discord username with icon

## 🧪 Testing

### Test Cases:
1. ✅ Discord user with avatar → Shows Discord avatar
2. ✅ Discord user without avatar → Shows default avatar
3. ✅ User with custom avatar → Shows custom avatar
4. ✅ Image load failure → Falls back to default
5. ✅ Non-Discord user → Shows default avatar

### Manual Testing:
1. Login with Discord
2. Go to Profile page
3. Verify Discord avatar is displayed
4. Check Discord username appears below name
5. Verify red border around avatar

## 🎉 Result

Users now have a seamless experience with automatic profile pictures from Discord, making the platform feel more personalized and professional from day one!
