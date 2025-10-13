# 🏆 Tournament Creation Guide

## 📍 **How to Create Tournaments**

### **For Users with ORGANIZER Role:**

1. **Access the Organizer Dashboard**
   - Navigate to: `http://localhost:4200/tournaments/organizer`
   - Or click **"My Tournaments"** in the navbar (green button)

2. **Create a New Tournament**
   - Click the **"Create Tournament"** button
   - Fill in tournament details
   - Save as draft or publish immediately

---

## 🔐 **Access Requirements**

### **Who Can Create Tournaments?**
- Users with **ORGANIZER** role
- Users with **ADMIN** role (admins have all permissions)

### **How to Get ORGANIZER Role?**
Currently, the ORGANIZER role must be assigned by an admin through:
1. **Admin Panel** → User Management
2. Edit user and change role to "ORGANIZER"

---

## 🎯 **Features Implemented**

### **1. Route Configuration** ✅
- **Path**: `/tournaments/organizer`
- **Component**: `OrganizerDashboardComponent`
- **Guard**: `OrganizerGuard` (role-based access control)

### **2. Navigation Links** ✅
- **Desktop**: Green "My Tournaments" button in navbar
- **Mobile**: "My Tournaments" link in mobile menu
- **Visibility**: Only shown to ORGANIZER and ADMIN users

### **3. Role-Based Guard** ✅
- **File**: `frontend/src/app/core/guards/organizer.guard.ts`
- **Function**: Protects organizer routes from unauthorized access
- **Behavior**:
  - Redirects to login if not authenticated
  - Redirects to dashboard if user doesn't have permission
  - Allows access for ORGANIZER and ADMIN roles

### **4. User Role Enum** ✅
- Added `ORGANIZER` to `UserRole` enum
- **File**: `frontend/src/app/core/models/user.model.ts`
- **Values**: USER, MODERATOR, ADMIN, ORGANIZER

---

## 📋 **Organizer Dashboard Features**

### **Tournament Management:**
- ✅ View all your tournaments
- ✅ Filter by status (All, Draft, Active, Completed)
- ✅ Create new tournaments
- ✅ Edit draft tournaments
- ✅ Publish tournaments
- ✅ Manage active tournaments
- ✅ Generate brackets
- ✅ Cancel tournaments

### **Tournament Statuses:**
- **DRAFT**: Tournament is being created
- **REGISTRATION_OPEN**: Teams can register
- **CHECK_IN**: Teams are checking in
- **IN_PROGRESS**: Tournament is ongoing
- **COMPLETED**: Tournament has ended
- **CANCELLED**: Tournament was cancelled

---

## 🚀 **How to Use**

### **Step 1: Get ORGANIZER Role**
Ask an admin to assign you the ORGANIZER role:
```
1. Admin logs into Admin Panel
2. Goes to User Management
3. Finds your user
4. Changes role to "ORGANIZER"
5. Saves changes
```

### **Step 2: Access Organizer Dashboard**
```
1. Log in to your account
2. Look for "My Tournaments" button in navbar (green)
3. Click to access organizer dashboard
```

### **Step 3: Create Tournament**
```
1. Click "Create Tournament" button
2. Fill in tournament details:
   - Name
   - Description
   - Format (Single Elimination, Double Elimination, etc.)
   - Prize Pool
   - Entry Fee
   - Max Teams
   - Start Date
   - Registration Deadline
3. Save as draft or publish
```

### **Step 4: Manage Tournament**
```
1. View tournament in dashboard
2. Use action buttons:
   - Edit (for drafts)
   - Publish (to open registration)
   - Manage (to handle registrations)
   - Generate Bracket (after check-in)
   - Cancel (if needed)
```

---

## 🔧 **Technical Details**

### **Files Modified:**

1. **`app-routing.module.ts`**
   - Added route for organizer dashboard
   - Imported OrganizerGuard

2. **`navbar.component.ts`**
   - Added `isOrganizer()` method
   - Checks for ORGANIZER or ADMIN role

3. **`navbar.component.html`**
   - Added "My Tournaments" link (desktop)
   - Added "My Tournaments" link (mobile)

4. **`user.model.ts`**
   - Added ORGANIZER to UserRole enum

5. **`organizer.guard.ts`** (NEW)
   - Created role-based guard
   - Protects organizer routes

---

## 🎨 **UI Elements**

### **Navbar Button (Desktop):**
- **Color**: Green (`text-green-300`)
- **Border**: Green with 30% opacity
- **Label**: "My Tournaments"
- **Position**: Between "Dashboard" and "Admin"

### **Mobile Menu:**
- **Color**: Green text
- **Border**: Green left border
- **Label**: "My Tournaments"

---

## 🔒 **Security**

### **Route Protection:**
- `OrganizerGuard` prevents unauthorized access
- Redirects non-authenticated users to login
- Redirects unauthorized users to dashboard

### **Role Checking:**
```typescript
isOrganizer(): boolean {
  return this.currentUser?.role === 'ORGANIZER' || 
         this.currentUser?.role === 'ADMIN';
}
```

---

## 📝 **Backend Requirements**

### **User Role in Database:**
Make sure the backend supports the ORGANIZER role:

```sql
-- Check if ORGANIZER role exists in users table
SELECT DISTINCT role FROM users;

-- If not, update a user to ORGANIZER
UPDATE users SET role = 'ORGANIZER' WHERE id = <user_id>;
```

### **Backend Enum:**
Ensure the backend has ORGANIZER in the Role enum:

```java
public enum Role {
    USER,
    MODERATOR,
    ADMIN,
    ORGANIZER
}
```

---

## ✅ **Testing Checklist**

- [ ] User with ORGANIZER role can see "My Tournaments" button
- [ ] User with ADMIN role can see "My Tournaments" button
- [ ] User with USER role CANNOT see "My Tournaments" button
- [ ] Clicking "My Tournaments" navigates to `/tournaments/organizer`
- [ ] Non-organizers are redirected when accessing the route directly
- [ ] "Create Tournament" button works
- [ ] Tournament list displays correctly
- [ ] Tab filtering works (All, Draft, Active, Completed)
- [ ] Mobile menu shows "My Tournaments" for organizers

---

## 🐛 **Troubleshooting**

### **"My Tournaments" button not showing:**
- Check if user has ORGANIZER or ADMIN role
- Refresh the page after role change
- Check browser console for errors

### **Access denied when clicking button:**
- Verify user role in database
- Check if OrganizerGuard is working
- Ensure user is logged in

### **Route not found:**
- Verify Angular app has recompiled
- Check browser console for routing errors
- Ensure OrganizerDashboardComponent is imported

---

## 📊 **Summary**

**Access Point**: Navbar → "My Tournaments" (green button)

**URL**: `/tournaments/organizer`

**Required Role**: ORGANIZER or ADMIN

**Features**:
- Create tournaments
- Manage tournaments
- View tournament statistics
- Generate brackets
- Publish/cancel tournaments

---

**Last Updated**: October 12, 2025  
**Status**: ✅ Fully Implemented and Ready to Use
