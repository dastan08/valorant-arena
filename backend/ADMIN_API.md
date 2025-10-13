# Admin API Documentation

This document outlines the admin API endpoints for the Valorant Arena platform.

## Authentication
All admin endpoints require:
- Valid JWT token in Authorization header: `Bearer <token>`
- User must have `ADMIN` role

## Base URL
```
http://localhost:8080/api/admin
```

## Dashboard Endpoints

### GET /dashboard/stats
Get dashboard statistics including user counts, team counts, etc.

**Response:**
```json
{
  "success": true,
  "message": "Dashboard stats retrieved successfully",
  "data": {
    "totalUsers": 150,
    "totalTeams": 25,
    "activeScrims": 8,
    "pendingReports": 3,
    "onlineUsers": 15,
    "totalMatches": 45
  }
}
```

### GET /dashboard/activity
Get recent platform activity feed.

**Response:**
```json
{
  "success": true,
  "message": "Recent activity retrieved successfully",
  "data": [
    {
      "description": "New user registered: player123",
      "timestamp": "2024-01-15T10:30:00",
      "icon": "person_add"
    }
  ]
}
```

### GET /dashboard/health
Get system health status.

**Response:**
```json
{
  "success": true,
  "message": "System health retrieved successfully",
  "data": {
    "database": "healthy",
    "api": "healthy",
    "discord": "healthy",
    "riot": "healthy"
  }
}
```

## User Management Endpoints

### GET /users
Get paginated list of all users with optional search.

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `search` (optional): Search term for username/email

**Response:**
```json
{
  "success": true,
  "message": "Users retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "username": "player123",
        "email": "player@example.com",
        "role": "USER",
        "status": "ACTIVE",
        "isVerified": true,
        "createdAt": "2024-01-15T10:30:00"
      }
    ],
    "totalElements": 150,
    "totalPages": 8,
    "number": 0,
    "size": 20
  }
}
```

### GET /users/{userId}
Get detailed information about a specific user.

**Response:**
```json
{
  "success": true,
  "message": "User retrieved successfully",
  "data": {
    "id": 1,
    "username": "player123",
    "email": "player@example.com",
    "role": "USER",
    "status": "ACTIVE",
    "isVerified": true,
    "team": {
      "id": 5,
      "name": "Team Alpha",
      "tag": "ALPHA"
    },
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

### PUT /users/{userId}
Update user information.

**Request Body:**
```json
{
  "username": "newusername",
  "email": "newemail@example.com",
  "role": "MODERATOR",
  "status": "ACTIVE",
  "isVerified": true
}
```

**Response:**
```json
{
  "success": true,
  "message": "User updated successfully",
  "data": {
    "id": 1,
    "username": "newusername",
    "email": "newemail@example.com",
    "role": "MODERATOR",
    "status": "ACTIVE",
    "isVerified": true
  }
}
```

### POST /users/{userId}/ban
Ban a user with reason and optional duration.

**Request Body:**
```json
{
  "reason": "Violation of terms of service",
  "duration": 7
}
```

**Response:**
```json
{
  "success": true,
  "message": "User banned successfully",
  "data": "User has been banned"
}
```

### POST /users/{userId}/unban
Unban a previously banned user.

**Response:**
```json
{
  "success": true,
  "message": "User unbanned successfully",
  "data": "User has been unbanned"
}
```

### DELETE /users/{userId}
Permanently delete a user account.

**Response:**
```json
{
  "success": true,
  "message": "User deleted successfully",
  "data": "User has been deleted"
}
```

## Team Management Endpoints

### GET /teams
Get paginated list of all teams with optional search.

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `search` (optional): Search term for team name/tag

**Response:**
```json
{
  "success": true,
  "message": "Teams retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Team Alpha",
        "tag": "ALPHA",
        "captain": {
          "id": 5,
          "username": "captain123"
        },
        "memberCount": 5,
        "maxMembers": 5,
        "elo": 1500,
        "status": "ACTIVE",
        "createdAt": "2024-01-15T10:30:00"
      }
    ],
    "totalElements": 25,
    "totalPages": 2,
    "number": 0,
    "size": 20
  }
}
```

### POST /teams/{teamId}/disband
Disband a team with administrative reason.

**Request Body:**
```json
{
  "reason": "Inactive team - no activity for 90 days"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Team disbanded successfully",
  "data": "Team has been disbanded"
}
```

## System Management Endpoints

### POST /system/clear-cache
Clear system cache.

**Response:**
```json
{
  "success": true,
  "message": "Cache cleared successfully",
  "data": "System cache has been cleared"
}
```

## Error Responses

All endpoints may return error responses in the following format:

```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

Common HTTP status codes:
- `200`: Success
- `400`: Bad Request (validation errors, etc.)
- `401`: Unauthorized (invalid/missing token)
- `403`: Forbidden (insufficient permissions)
- `404`: Not Found (resource doesn't exist)
- `500`: Internal Server Error

## Security Notes

1. All admin endpoints are protected by `@PreAuthorize("hasRole('ADMIN')")`
2. User actions are logged for audit purposes
3. Sensitive operations (ban, delete) include additional logging
4. Rate limiting may be applied to prevent abuse

## Usage Examples

### Get Dashboard Stats
```bash
curl -H "Authorization: Bearer <token>" \
     http://localhost:8080/api/admin/dashboard/stats
```

### Search Users
```bash
curl -H "Authorization: Bearer <token>" \
     "http://localhost:8080/api/admin/users?search=player&page=0&size=10"
```

### Ban User
```bash
curl -X POST \
     -H "Authorization: Bearer <token>" \
     -H "Content-Type: application/json" \
     -d '{"reason":"Cheating","duration":30}' \
     http://localhost:8080/api/admin/users/123/ban
```
