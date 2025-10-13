# Admin Dashboard Setup Guide

This guide will help you set up and configure the admin dashboard for the Valorant Arena platform.

## Prerequisites

1. **Database Setup**: Ensure PostgreSQL is running and the `valorant_arena` database exists
2. **Backend Running**: Spring Boot application should be running on port 8080
3. **Frontend Running**: Angular application should be running on port 4200

## Database Migration

### Step 1: Add User Status Column

Run the following SQL script to add the status column to the users table:

```bash
# Connect to your PostgreSQL database
psql -U postgres -d valorant_arena

# Run the migration script
\i backend/add_user_status.sql
```

Or manually execute:
```sql
ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE users ADD CONSTRAINT chk_user_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'BANNED'));
CREATE INDEX idx_users_status ON users(status);
```

### Step 2: Create Admin User

Create an admin user to access the dashboard:

```sql
-- Update an existing user to admin role
UPDATE users SET role = 'ADMIN' WHERE username = 'your_username';

-- Or create a new admin user (adjust as needed)
INSERT INTO users (username, email, password, role, rank, status, is_verified) 
VALUES ('admin', 'admin@valorantarena.com', '$2a$10$encoded_password', 'ADMIN', 'IRON_1', 'ACTIVE', true);
```

## Frontend Setup

### Step 1: Install Dependencies

```bash
cd frontend
npm install
```

### Step 2: Build and Serve

```bash
# Development build
ng serve

# Or production build
ng build --configuration production
```

## Backend Setup

### Step 1: Install Dependencies

```bash
cd backend
mvn clean install
```

### Step 2: Run Application

```bash
mvn spring-boot:run
```

## Accessing the Admin Dashboard

### Step 1: Login as Admin

1. Navigate to `http://localhost:4200/login`
2. Login with your admin credentials
3. You should see an "Admin" button in the navigation bar

### Step 2: Access Admin Features

Click the "Admin" button or navigate to `http://localhost:4200/admin` to access:

- **Dashboard**: Overview statistics and system health
- **User Management**: View, edit, ban/unban, and delete users
- **Team Management**: View teams and disband if necessary
- **Scrim Management**: Monitor scrimmages (placeholder)
- **Report Management**: Handle user reports (placeholder)

## Admin Dashboard Features

### 🏠 Main Dashboard
- **Real-time Statistics**: User count, team count, active scrims
- **System Health**: Database, API, Discord bot, Riot API status
- **Recent Activity**: Live feed of platform activities
- **Quick Navigation**: Direct links to management sections

### 👥 User Management
- **User Search**: Find users by username or email
- **User Details**: View and edit user information
- **Role Management**: Change user roles (USER, MODERATOR, ADMIN)
- **Account Status**: Set user status (ACTIVE, SUSPENDED, BANNED)
- **Ban System**: Temporary or permanent bans with reasons
- **User Deletion**: Complete account removal

### 🏆 Team Management
- **Team Overview**: All teams with statistics
- **Team Search**: Find teams by name or tag
- **Team Details**: View team information and members
- **Team Disbanding**: Administrative team removal

### ⚔️ System Management
- **Cache Management**: Clear system cache
- **Health Monitoring**: Check service status
- **Activity Logs**: Track administrative actions

## API Endpoints

The admin dashboard uses the following API endpoints:

### Dashboard
- `GET /api/admin/dashboard/stats` - Get statistics
- `GET /api/admin/dashboard/activity` - Get recent activity
- `GET /api/admin/dashboard/health` - Get system health

### User Management
- `GET /api/admin/users` - List users (paginated)
- `GET /api/admin/users/{id}` - Get user details
- `PUT /api/admin/users/{id}` - Update user
- `POST /api/admin/users/{id}/ban` - Ban user
- `POST /api/admin/users/{id}/unban` - Unban user
- `DELETE /api/admin/users/{id}` - Delete user

### Team Management
- `GET /api/admin/teams` - List teams (paginated)
- `POST /api/admin/teams/{id}/disband` - Disband team

### System Management
- `POST /api/admin/system/clear-cache` - Clear cache

## Security

### Authentication
- All admin endpoints require JWT authentication
- Users must have `ADMIN` role to access admin features
- Admin guard protects all admin routes on the frontend

### Authorization
- Role-based access control (RBAC)
- Admin actions are logged for audit purposes
- Sensitive operations require additional confirmation

### Best Practices
1. **Regular Monitoring**: Check system health regularly
2. **User Management**: Review user accounts periodically
3. **Activity Logs**: Monitor recent activity for suspicious behavior
4. **Backup**: Ensure database backups before major operations
5. **Testing**: Test admin functions in development environment first

## Troubleshooting

### Common Issues

1. **Admin Button Not Visible**
   - Check if user has `ADMIN` role in database
   - Verify JWT token contains correct role information
   - Clear browser cache and re-login

2. **403 Forbidden Errors**
   - Ensure user has `ADMIN` role
   - Check JWT token expiration
   - Verify backend security configuration

3. **Database Connection Issues**
   - Check PostgreSQL service is running
   - Verify database credentials in application.properties
   - Ensure database schema is up to date

4. **Frontend Build Errors**
   - Run `npm install` to update dependencies
   - Check Angular CLI version compatibility
   - Clear node_modules and reinstall if needed

### Logs and Debugging

- **Backend Logs**: Check Spring Boot console output
- **Frontend Logs**: Open browser developer tools console
- **Database Logs**: Check PostgreSQL logs for query issues
- **Network**: Use browser network tab to debug API calls

## Development Notes

### Adding New Admin Features

1. **Backend**: Add new endpoints to `AdminController`
2. **Service**: Implement business logic in `AdminService`
3. **Frontend**: Create new components in `admin/` directory
4. **Routing**: Add routes to `admin-routing.module.ts`
5. **Navigation**: Update admin dashboard navigation

### Testing

Run the admin controller tests:
```bash
cd backend
mvn test -Dtest=AdminControllerTest
```

### Future Enhancements

- **Advanced Analytics**: More detailed statistics and charts
- **Audit Logs**: Complete audit trail for all admin actions
- **Bulk Operations**: Mass user/team management
- **Report System**: Complete report management implementation
- **Notification System**: Admin alerts and notifications
- **Role Permissions**: Granular permission system

## Support

For issues or questions regarding the admin dashboard:
1. Check this documentation first
2. Review the API documentation (`ADMIN_API.md`)
3. Check application logs for error details
4. Test in development environment before production changes
