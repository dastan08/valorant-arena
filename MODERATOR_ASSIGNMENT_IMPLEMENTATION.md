# 🎯 Tournament Moderator Assignment System - Implementation Guide

## ✅ **What Was Implemented**

### **Backend Components:**

1. **TournamentModerator Entity** (`TournamentModerator.java`)
   - Links tournaments with moderators
   - Tracks who assigned the moderator
   - Stores assignment date and notes
   - Unique constraint: one user can only be assigned once per tournament

2. **TournamentModeratorRepository** (`TournamentModeratorRepository.java`)
   - Find moderators by tournament
   - Find tournaments by moderator
   - Check if user is moderator for tournament
   - Delete assignments

3. **TournamentModeratorService** (`TournamentModeratorService.java`)
   - Assign moderator to tournament
   - Remove moderator from tournament
   - Get all moderators for a tournament
   - Check moderator permissions
   - Validation: Only users with TOURNAMENT_MODERATOR or ADMIN role can be assigned

4. **TournamentModeratorController** (`TournamentModeratorController.java`)
   - POST `/api/tournaments/{id}/moderators` - Assign moderator
   - DELETE `/api/tournaments/{id}/moderators/{userId}` - Remove moderator
   - GET `/api/tournaments/{id}/moderators` - List moderators
   - GET `/api/tournaments/{id}/moderators/check/{userId}` - Check if user is moderator

5. **DTOs**
   - `TournamentModeratorRequest` - For assignment requests
   - `TournamentModeratorResponse` - For API responses

### **Frontend Components:**

1. **TournamentModeratorService** (`tournament-moderator.service.ts`)
   - Angular service for API calls
   - Methods for assign, remove, list, and check moderators

2. **AssignModeratorDialogComponent** (Dialog)
   - Beautiful modal for assigning moderators
   - Dropdown to select users with TOURNAMENT_MODERATOR role
   - Optional notes field
   - Validation and error handling

---

## 🚀 **How to Use**

### **Step 1: Assign TOURNAMENT_MODERATOR Role**
First, users need the TOURNAMENT_MODERATOR role:

```sql
UPDATE users SET role = 'TOURNAMENT_MODERATOR' WHERE email = 'moderator@example.com';
```

Or via Admin Panel:
```
Admin Panel → User Management → Edit User → Role: TOURNAMENT_MODERATOR
```

### **Step 2: Create Database Table**
Run this SQL to create the tournament_moderators table:

```sql
CREATE TABLE tournament_moderators (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    assigned_by BIGINT NOT NULL,
    assigned_at DATETIME NOT NULL,
    notes VARCHAR(500),
    UNIQUE KEY unique_tournament_user (tournament_id, user_id),
    FOREIGN KEY (tournament_id) REFERENCES tournaments(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by) REFERENCES users(id)
);
```

### **Step 3: Integrate into Tournament Management**
Add "Assign Moderator" button to tournament management page:

```typescript
// In organizer-dashboard.component.ts
openAssignModeratorDialog(tournament: any): void {
  const dialogRef = this.dialog.open(AssignModeratorDialogComponent, {
    width: '500px',
    data: { tournamentId: tournament.id }
  });

  dialogRef.afterClosed().subscribe(result => {
    if (result === 'assigned') {
      // Refresh moderators list
      this.loadModerators(tournament.id);
    }
  });
}
```

---

## 📋 **API Endpoints**

### **1. Assign Moderator**
```http
POST /api/tournaments/{tournamentId}/moderators
Authorization: Bearer <token>
Content-Type: application/json

{
  "userId": 123,
  "notes": "Assigned to help with check-ins"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Moderator assigned successfully",
  "data": {
    "id": 1,
    "userId": 123,
    "username": "moderator1",
    "email": "mod@example.com",
    "assignedById": 456,
    "assignedByUsername": "organizer1",
    "assignedAt": "2025-10-12T23:45:00",
    "notes": "Assigned to help with check-ins"
  }
}
```

### **2. Remove Moderator**
```http
DELETE /api/tournaments/{tournamentId}/moderators/{userId}
Authorization: Bearer <token>
```

### **3. List Moderators**
```http
GET /api/tournaments/{tournamentId}/moderators
Authorization: Bearer <token>
```

### **4. Check Moderator**
```http
GET /api/tournaments/{tournamentId}/moderators/check/{userId}
Authorization: Bearer <token>
```

---

## 🔒 **Permissions**

### **Who Can Assign Moderators:**
- ✅ Tournament Organizer (creator)
- ✅ ADMIN

### **Who Can Be Assigned:**
- ✅ Users with TOURNAMENT_MODERATOR role
- ✅ Users with ADMIN role

### **Who Can Remove Moderators:**
- ✅ Tournament Organizer (creator)
- ✅ ADMIN

### **Who Can View Moderators:**
- ✅ Tournament Organizer
- ✅ Assigned Moderators
- ✅ ADMIN

---

## 🎨 **UI Components**

### **Assign Moderator Dialog:**
- Modern gradient design (green theme)
- Dropdown to select users
- Notes field for additional context
- Loading states
- Error handling
- Validation

### **Features:**
- Only shows users with TOURNAMENT_MODERATOR role
- Displays username and email for selection
- Shows helpful message if no moderators available
- Prevents duplicate assignments
- Beautiful animations and transitions

---

## 📊 **Database Schema**

```sql
tournament_moderators
├── id (BIGINT, PK, AUTO_INCREMENT)
├── tournament_id (BIGINT, FK → tournaments.id)
├── user_id (BIGINT, FK → users.id)
├── assigned_by (BIGINT, FK → users.id)
├── assigned_at (DATETIME)
└── notes (VARCHAR(500))

UNIQUE (tournament_id, user_id)
```

---

## 🔄 **Next Steps**

### **To Complete the System:**

1. **Add Moderator List to Tournament Management**
   - Show list of assigned moderators
   - Remove button for each moderator
   - Display assignment date and notes

2. **Update Organizer Dashboard**
   - Add "Assign Moderator" button
   - Show moderator count badge
   - Quick access to moderator management

3. **Add Permission Checks**
   - Check if TOURNAMENT_MODERATOR has access to specific tournament
   - Filter tournaments in dashboard based on assignments
   - Show only assigned tournaments to moderators

4. **Add Module Declaration**
   - Add `AssignModeratorDialogComponent` to `app.module.ts` declarations
   - Import required Material modules

---

## 📝 **Module Integration**

Add to `app.module.ts`:

```typescript
import { AssignModeratorDialogComponent } from './pages/tournaments/assign-moderator-dialog/assign-moderator-dialog.component';

@NgModule({
  declarations: [
    // ... other components
    AssignModeratorDialogComponent
  ],
  // ...
})
```

---

## ✅ **Testing Checklist**

- [ ] Create user with TOURNAMENT_MODERATOR role
- [ ] Assign moderator to tournament
- [ ] Verify moderator can access tournament
- [ ] Remove moderator from tournament
- [ ] Try assigning same moderator twice (should fail)
- [ ] Try assigning user without TOURNAMENT_MODERATOR role (should fail)
- [ ] Verify only organizer/admin can assign/remove
- [ ] Check moderator list displays correctly

---

**Status**: ✅ Backend Complete, 🔄 Frontend Integration Pending  
**Last Updated**: October 12, 2025
