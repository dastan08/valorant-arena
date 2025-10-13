# 🔧 Database Fix Scripts

## Problem
The database tables are missing `createdAt` and `updatedAt` columns, causing Discord login and other features to fail with errors like:
```
Unknown column 'u1_0.createdAt' in 'field list'
Unknown column 't2_0.createdAt' in 'field list'
```

---

## ✅ Solution - Choose ONE of these options:

### **Option 1: Fresh Start (RECOMMENDED)** 
**File:** `complete_database_setup.sql`

**Use this if:**
- You don't have important data to preserve
- You want a clean database with correct schema
- This is the fastest and cleanest solution

**What it does:**
- Drops the entire `valorant_arena` database
- Creates a fresh database
- Lets Hibernate automatically create all tables with correct columns

**How to run:**
```bash
# Using MySQL command line
mysql -u root -pabcd < "d:/github repos/windsurf-project/backend/complete_database_setup.sql"

# OR in MySQL Workbench
# 1. Open the file
# 2. Execute the script
```

**After running:**
```bash
# Kill any running backend
taskkill /F /IM java.exe

# Start backend (it will create all tables automatically)
cd "d:/github repos/windsurf-project/backend"
mvn spring-boot:run
```

---

### **Option 2: Preserve Existing Data**
**File:** `add_missing_columns.sql`

**Use this if:**
- You have existing users/teams/data you want to keep
- You only want to add the missing columns

**What it does:**
- Checks each table for missing `createdAt` and `updatedAt` columns
- Adds them only if they don't exist
- Updates existing rows with current timestamps
- Preserves all your existing data

**How to run:**
```bash
# Using MySQL command line
mysql -u root -pabcd < "d:/github repos/windsurf-project/backend/add_missing_columns.sql"

# OR in MySQL Workbench
# 1. Open the file
# 2. Execute the script
```

**After running:**
```bash
# Kill any running backend
taskkill /F /IM java.exe

# Start backend
cd "d:/github repos/windsurf-project/backend"
mvn spring-boot:run
```

---

## 📋 Step-by-Step Instructions

### **Step 1: Choose Your Script**
- **Fresh start?** → Use `complete_database_setup.sql`
- **Keep data?** → Use `add_missing_columns.sql`

### **Step 2: Run the SQL Script**

**Method A: MySQL Command Line**
```bash
mysql -u root -pabcd < "d:/github repos/windsurf-project/backend/[SCRIPT_NAME].sql"
```

**Method B: MySQL Workbench**
1. Open MySQL Workbench
2. Connect to your database
3. File → Open SQL Script
4. Select the script file
5. Click Execute (⚡ icon)

### **Step 3: Verify Script Ran Successfully**
You should see:
```
✓ Database recreated successfully! (Option 1)
OR
✓ All missing columns added successfully! (Option 2)
```

### **Step 4: Kill Any Running Backend**
```bash
taskkill /F /IM java.exe
```

### **Step 5: Start Backend**
```bash
cd "d:/github repos/windsurf-project/backend"
mvn spring-boot:run
```

Wait for the server to start (look for "Started ValorantArenaApplication")

### **Step 6: Test**
1. **Option 1 users:** Register a new account first
2. **Option 2 users:** Login with existing credentials
3. Try Discord login
4. Everything should work now! ✅

---

## 🔍 Troubleshooting

### **Script fails with "Duplicate column" error**
- This is OK! It means the column already exists
- The script will continue and add other missing columns

### **Backend still fails after running script**
1. Make sure you restarted the backend AFTER running the script
2. Check backend logs for any other errors
3. Verify the columns were added:
   ```sql
   USE valorant_arena;
   DESCRIBE users;
   DESCRIBE teams;
   ```
   You should see `createdAt` and `updatedAt` columns

### **Can't connect to MySQL**
- Make sure MySQL is running
- Check your password (default in script is `abcd`)
- Update the password in the command if different

---

## 📝 What Each Script Does

### `complete_database_setup.sql`
```sql
1. DROP DATABASE valorant_arena
2. CREATE DATABASE valorant_arena
3. Hibernate creates all tables on backend startup
```

### `add_missing_columns.sql`
```sql
1. Check if createdAt exists in users → Add if missing
2. Check if updatedAt exists in users → Add if missing
3. Repeat for teams, matches, scrims, matchmaking_queue
4. Update NULL values with current timestamp
```

---

## ✅ Expected Result

After running either script and restarting the backend:

- ✅ Discord login works
- ✅ Regular login works
- ✅ User registration works
- ✅ All API endpoints work
- ✅ No more "Unknown column" errors

---

## 🎯 Quick Reference

| Script | Data Loss | Speed | Use When |
|--------|-----------|-------|----------|
| `complete_database_setup.sql` | ⚠️ YES | ⚡ Fast | Development/Testing |
| `add_missing_columns.sql` | ✅ NO | 🐢 Slower | Production/Has Data |

---

**Last Updated:** October 12, 2025
