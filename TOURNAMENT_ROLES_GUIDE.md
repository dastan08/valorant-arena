# 🏆 Tournament Roles & Permissions Guide

## 📋 **Role Hierarchy**

### **1. ADMIN**
- **Full system access**
- Can manage all users
- Can assign any role
- Can access all tournaments
- Can moderate any tournament

### **2. ORGANIZER**
- **Can create and host tournaments**
- Can manage their own tournaments
- Can assign TOURNAMENT_MODERATOR role to users for specific tournaments
- Access to "My Tournaments" dashboard
- Can publish, edit, and cancel tournaments

### **3. TOURNAMENT_MODERATOR**
- **Can moderate specific tournaments** (assigned by organizers)
- Access to "My Tournaments" dashboard
- Can view tournament details
- Can manage registrations and check-ins
- **Cannot create new tournaments**
- Limited to tournaments they're assigned to

### **4. MODERATOR**
- General platform moderation
- Can moderate chat, reports, etc.
- **Cannot manage tournaments**

### **5. USER**
- Standard user
- Can register for tournaments
- Can create/join teams
- Can participate in matches

---

## 🎯 **Tournament Management Flow**

### **Step 1: Assign ORGANIZER Role**
Admin assigns ORGANIZER role to trusted users:
```
Admin Panel → User Management → Edit User → Role: ORGANIZER
```

### **Step 2: Organizer Creates Tournament**
Organizer accesses dashboard:
```
Navbar → "My Tournaments" → Create Tournament
```

### **Step 3: Assign Tournament Moderators** (Future Feature)
Organizer can assign moderators to help manage specific tournaments:
```
Tournament Management → Assign Moderators → Select Users
```

---

## 🔐 **Access Control**

### **"My Tournaments" Dashboard Access:**
- ✅ ADMIN
- ✅ ORGANIZER
- ✅ TOURNAMENT_MODERATOR
- ❌ MODERATOR
- ❌ USER

### **Create Tournament:**
- ✅ ADMIN
- ✅ ORGANIZER
- ❌ TOURNAMENT_MODERATOR
- ❌ MODERATOR
- ❌ USER

### **Manage Tournament:**
- ✅ ADMIN (all tournaments)
- ✅ ORGANIZER (own tournaments)
- ✅ TOURNAMENT_MODERATOR (assigned tournaments)
- ❌ MODERATOR
- ❌ USER

---

## 📝 **Implementation Status**

### **✅ Completed:**
1. Added ORGANIZER role to backend enum
2. Added TOURNAMENT_MODERATOR role to backend enum
3. Added roles to frontend enum
4. Updated admin user management to show new roles
5. Updated organizer guard to allow TOURNAMENT_MODERATOR
6. Updated navbar to show "My Tournaments" for both roles

### **🔄 To Be Implemented:**
1. **Tournament Moderator Assignment UI**
   - Add "Assign Moderators" button in tournament management
   - Modal to select users and assign as moderators
   - Backend API to handle assignments

2. **Tournament-Specific Permissions**
   - Check if TOURNAMENT_MODERATOR is assigned to specific tournament
   - Restrict access to only assigned tournaments

3. **Moderator Management Table**
   - Show list of moderators for each tournament
   - Remove moderator functionality

---

## 🚀 **How to Use**

### **For Admins:**
1. Go to Admin Panel → User Management
2. Find user who should be an organizer
3. Click "View" → Change role to "ORGANIZER"
4. Save changes

### **For Organizers:**
1. Click "My Tournaments" in navbar (green button)
2. Click "Create Tournament"
3. Fill in tournament details
4. Publish tournament
5. (Future) Assign moderators to help manage

### **For Tournament Moderators:**
1. Wait for organizer to assign you to a tournament
2. Access "My Tournaments" dashboard
3. View and manage assigned tournaments
4. Help with registrations, check-ins, and match management

---

## 🔧 **Database Update**

To assign roles to existing users:

```sql
-- Make a user an ORGANIZER
UPDATE users SET role = 'ORGANIZER' WHERE email = 'user@example.com';

-- Make a user a TOURNAMENT_MODERATOR
UPDATE users SET role = 'TOURNAMENT_MODERATOR' WHERE email = 'moderator@example.com';
```

---

## 📊 **Role Comparison**

| Feature | USER | MODERATOR | TOURNAMENT_MOD | ORGANIZER | ADMIN |
|---------|------|-----------|----------------|-----------|-------|
| Create Tournament | ❌ | ❌ | ❌ | ✅ | ✅ |
| Manage Own Tournaments | ❌ | ❌ | ❌ | ✅ | ✅ |
| Moderate Assigned Tournaments | ❌ | ❌ | ✅ | ✅ | ✅ |
| Moderate All Tournaments | ❌ | ❌ | ❌ | ❌ | ✅ |
| Assign Moderators | ❌ | ❌ | ❌ | ✅ | ✅ |
| Platform Moderation | ❌ | ✅ | ❌ | ❌ | ✅ |
| User Management | ❌ | ❌ | ❌ | ❌ | ✅ |

---

**Last Updated**: October 12, 2025  
**Status**: ✅ Roles Implemented, 🔄 Assignment System Pending
