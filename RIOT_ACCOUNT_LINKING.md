# Riot Account Linking Feature

## Overview
Existing users can now link their Riot account from their profile page. This feature allows users who registered without a Riot ID to add and verify their Valorant account later.

## Features Implemented

### Backend

1. **UserService** (`UserService.java`)
   - `linkRiotAccount()` - Links and verifies Riot account
   - `unlinkRiotAccount()` - Removes Riot account link
   - `getUserById()` - Fetches user profile
   - Validates Riot ID format and uniqueness
   - Calls Riot API for verification

2. **UserController** (`UserController.java`)
   - `GET /api/user/profile` - Get current user profile
   - `POST /api/user/link-riot-account` - Link Riot account
   - `DELETE /api/user/unlink-riot-account` - Unlink Riot account

3. **LinkRiotAccountRequest DTO**
   - Validates Riot ID format
   - Pattern: `GameName#TAG`

### Frontend

1. **Updated Profile Component** (`profile.component.ts`)
   - Modal dialog for linking Riot account
   - Real-time validation
   - Success/error messaging
   - Unlink functionality with confirmation

2. **Enhanced Profile UI** (`profile.component.html`)
   - "Link Account" button when no Riot ID
   - "Unlink" button when Riot ID is linked
   - Verification status indicator (✓ Verified)
   - Beautiful modal with Valorant theme

## User Flow

### Linking Riot Account

1. User navigates to Profile page
2. If no Riot ID is linked, sees "Not linked" with "Link Account" button
3. Clicks "Link Account" button
4. Modal opens with input field
5. User enters Valorant ID (e.g., `PlayerName#1234`)
6. Clicks "Link Account"
7. Backend verifies with Riot API
8. If valid:
   - Account is linked
   - User marked as verified
   - Success message shown
   - Modal closes automatically
9. If invalid:
   - Error message displayed
   - User can try again

### Unlinking Riot Account

1. User clicks "Unlink" button next to Riot ID
2. Confirmation dialog appears
3. If confirmed:
   - Riot ID removed
   - Verification status removed
   - Success message shown

## API Endpoints

### Get Profile
```http
GET /api/user/profile
Authorization: Bearer <token>

Response:
{
  "success": true,
  "message": "Profile retrieved successfully",
  "data": {
    "id": 1,
    "username": "player123",
    "email": "player@example.com",
    "valorantId": "PlayerName#1234",
    "rank": "GOLD",
    "isVerified": true,
    ...
  }
}
```

### Link Riot Account
```http
POST /api/user/link-riot-account
Authorization: Bearer <token>
Content-Type: application/json

{
  "valorantId": "PlayerName#1234"
}

Response:
{
  "success": true,
  "message": "Riot account linked successfully",
  "data": {
    "id": 1,
    "username": "player123",
    "valorantId": "PlayerName#1234",
    "isVerified": true,
    ...
  }
}
```

### Unlink Riot Account
```http
DELETE /api/user/unlink-riot-account
Authorization: Bearer <token>

Response:
{
  "success": true,
  "message": "Riot account unlinked successfully",
  "data": {
    "id": 1,
    "username": "player123",
    "valorantId": null,
    "isVerified": false,
    ...
  }
}
```

## Validation Rules

### Riot ID Format
- Must contain exactly one `#` character
- Format: `GameName#TAG`
- Example: `PlayerName#1234`

### Uniqueness
- Each Riot ID can only be linked to one account
- Prevents duplicate account linking
- Shows error if Riot ID already in use

### Verification
- Calls Riot API to verify account exists
- Only valid Riot accounts can be linked
- Sets `isVerified` flag to `true` on success

## Security

- All endpoints require authentication
- Users can only modify their own profile
- Riot API key stored securely in environment variables
- Input validation on both frontend and backend

## Error Handling

### Common Errors

1. **"Invalid Valorant ID format"**
   - Riot ID doesn't contain `#`
   - Solution: Use format `GameName#TAG`

2. **"Riot account not found"**
   - Account doesn't exist in Riot's system
   - Solution: Check spelling and tag

3. **"This Valorant ID is already linked to another account"**
   - Another user already linked this Riot ID
   - Solution: Use a different Riot ID

4. **"Failed to verify Riot account"**
   - Riot API error or network issue
   - Solution: Try again later or check API key

## UI/UX Features

### Visual Indicators
- ✓ Green checkmark for verified accounts
- "Not linked" placeholder when no Riot ID
- Loading state during verification
- Success/error messages with color coding

### Responsive Design
- Modal centered on screen
- Mobile-friendly layout
- Smooth animations
- Valorant-themed colors

### User Feedback
- Real-time validation
- Clear error messages
- Success confirmation
- Auto-close on success

## Testing

### Test Linking
1. Login to account without Riot ID
2. Go to Profile page
3. Click "Link Account"
4. Enter valid Riot ID
5. Verify success message
6. Check verification badge appears

### Test Unlinking
1. Login to account with linked Riot ID
2. Go to Profile page
3. Click "Unlink"
4. Confirm action
5. Verify Riot ID removed
6. Check verification badge disappears

### Test Validation
1. Try linking without `#` - should show error
2. Try linking non-existent account - should show error
3. Try linking already-used Riot ID - should show error

## Future Enhancements

Potential improvements:
1. **Auto-update rank** from Riot API
2. **Display match history** on profile
3. **Show competitive stats** from Riot
4. **Link multiple Riot accounts** (smurf accounts)
5. **Periodic re-verification** of linked accounts
6. **Import player avatar** from Riot
7. **Display Riot account level** and creation date

## Notes

- Riot API key must be configured (see RIOT_API_SETUP.md)
- Development API keys expire after 24 hours
- Rate limits apply (20 req/sec, 100 req/2 min)
- Users can link/unlink as many times as needed
- Linking is optional but recommended for tournaments

## Related Files

### Backend
- `UserService.java` - Business logic
- `UserController.java` - API endpoints
- `LinkRiotAccountRequest.java` - Request DTO
- `RiotApiService.java` - Riot API integration

### Frontend
- `profile.component.ts` - Component logic
- `profile.component.html` - UI template
- `user.model.ts` - User interface

## Support

For issues or questions:
1. Check Riot API key is valid
2. Verify Riot ID format is correct
3. Check backend logs for errors
4. Ensure user is authenticated
5. Test with known valid Riot ID
