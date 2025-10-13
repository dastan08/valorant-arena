# MySQL Conversion - Remaining Fixes for V6

## ✅ Already Fixed
- ✅ `BIGSERIAL` → `BIGINT AUTO_INCREMENT` (done via PowerShell)
- ✅ `JSONB` → `JSON` (done via PowerShell)
- ✅ Organizers table - fully converted
- ✅ Tournaments table - fully converted

## 🔧 Still Need to Fix

### For Each Remaining Table:

1. **Remove inline `REFERENCES`** - Move to separate FOREIGN KEY constraints
2. **Add `ON UPDATE CURRENT_TIMESTAMP`** to `updated_at` columns
3. **Add `ENGINE=InnoDB`** and charset at end of each CREATE TABLE
4. **Remove PostgreSQL `COMMENT ON TABLE`** statements

### Tables That Need Conversion:

1. ✅ `organizers` - DONE
2. ✅ `tournaments` - DONE
3. ❌ `tournament_entries` - NEEDS FIXING
4. ❌ `tournament_matches` - NEEDS FIXING
5. ❌ `brackets` - NEEDS FIXING
6. ❌ `tournament_disputes` - NEEDS FIXING
7. ❌ `tournament_statistics` - NEEDS FIXING

## 📝 Pattern to Follow

### Before (PostgreSQL):
```sql
CREATE TABLE tournament_entries (
    id BIGSERIAL PRIMARY KEY,
    tournament_id BIGINT NOT NULL REFERENCES tournaments(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    ...
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_entry_type CHECK (entry_type IN ('SOLO', 'TEAM'))
);
```

### After (MySQL):
```sql
CREATE TABLE tournament_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id BIGINT NOT NULL,
    user_id BIGINT,
    ...
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_entry_type CHECK (entry_type IN ('SOLO', 'TEAM')),
    CONSTRAINT fk_entries_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments(id) ON DELETE CASCADE,
    CONSTRAINT fk_entries_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

## 🚀 Quick Fix Commands

You can manually edit the V6 file or I can complete the conversion. The key changes for each table:

1. Find all lines with `REFERENCES`
2. Remove the `REFERENCES ...` part
3. Add FOREIGN KEY constraints before the closing `)`
4. Add `ON UPDATE CURRENT_TIMESTAMP` to `updated_at`
5. Add `ENGINE=InnoDB...` after the closing `)`

## ⚠️ Current Status

The V6 file will have SQL errors until all inline REFERENCES are converted to separate FOREIGN KEY constraints. This is the main blocker.

Would you like me to complete the full conversion now?
