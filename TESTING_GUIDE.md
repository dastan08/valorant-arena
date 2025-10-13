# 🧪 Tournament System - Testing Guide

**Version**: 3.0.0  
**Date**: October 12, 2025  
**Status**: Ready for Testing

---

## 📋 Pre-Testing Checklist

### **Backend Setup**
- [ ] Database is running (MySQL)
- [ ] All migrations have run successfully
- [ ] Backend server is running on port 8080
- [ ] Check logs for any startup errors

### **Frontend Setup**
- [ ] Node modules installed (`npm install`)
- [ ] Frontend is running on port 4200
- [ ] No compilation errors
- [ ] Browser console is clear

### **Test Data**
- [ ] At least 3 test users created
- [ ] At least 2 test teams created
- [ ] Admin user exists
- [ ] Test organizer application ready

---

## 🎯 Test Scenarios

## **Scenario 1: Organizer Application & Verification**

### **Test Case 1.1: Apply as Organizer**
**Steps:**
1. Login as regular user
2. Navigate to organizer application
3. Fill out application form:
   - Organization Name: "Test Gaming League"
   - Description: "Professional esports organization for testing"
   - Website: "https://testleague.com"
   - Discord: "https://discord.gg/test"
4. Submit application

**Expected Result:**
- ✅ Application submitted successfully
- ✅ Status shows "PENDING"
- ✅ Confirmation message displayed

**API Call:**
```bash
POST http://localhost:8080/api/organizers/apply
Authorization: Bearer <user-token>
Content-Type: application/json

{
  "organizationName": "Test Gaming League",
  "description": "Professional esports organization for testing",
  "websiteUrl": "https://testleague.com",
  "discordServer": "https://discord.gg/test"
}
```

### **Test Case 1.2: Admin Verifies Organizer**
**Steps:**
1. Login as admin
2. Navigate to admin panel → Pending Organizers
3. Review application
4. Click "Approve"
5. Add verification notes: "Verified for testing"

**Expected Result:**
- ✅ Organizer status changes to "VERIFIED"
- ✅ User can now create tournaments

**API Call:**
```bash
POST http://localhost:8080/api/organizers/{id}/verify?approved=true
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "notes": "Verified for testing"
}
```

---

## **Scenario 2: Tournament Creation**

### **Test Case 2.1: Create Tournament (Draft)**
**Steps:**
1. Login as verified organizer
2. Navigate to "Create Tournament"
3. **Step 1 - Basic Info:**
   - Name: "Summer Championship 2025"
   - Description: "Test tournament for system validation"
   - Banner URL: (optional)
   - Click "Next"

4. **Step 2 - Configuration:**
   - Game Mode: "TEAM_5V5"
   - Format: "SINGLE_ELIMINATION"
   - Max Participants: 8
   - Min Participants: 4
   - Team Size: 5
   - Region: "NA"
   - Click "Next"

5. **Step 3 - Schedule:**
   - Registration Start: Tomorrow 9:00 AM
   - Registration End: Tomorrow 11:00 AM
   - Check-in Start: Tomorrow 11:30 AM
   - Check-in End: Tomorrow 11:45 AM
   - Tournament Start: Tomorrow 12:00 PM
   - Click "Next"

6. **Step 4 - Rules & Prizes:**
   - Rules: "Standard Valorant competitive rules. No cheating. Respect opponents."
   - Prize Pool: 1000
   - Entry Fee: 0
   - Match Format: "BO3"
   - Select maps: Bind, Haven, Ascent
   - Allow Substitutes: Yes
   - Max Substitutes: 2
   - Click "Create Tournament"

**Expected Result:**
- ✅ Tournament created with status "DRAFT"
- ✅ Redirected to tournament detail page
- ✅ All information displayed correctly

**API Call:**
```bash
POST http://localhost:8080/api/tournaments
Authorization: Bearer <organizer-token>
Content-Type: application/json

{
  "name": "Summer Championship 2025",
  "description": "Test tournament for system validation",
  "gameMode": "TEAM_5V5",
  "format": "SINGLE_ELIMINATION",
  "maxParticipants": 8,
  "minParticipants": 4,
  "teamSize": 5,
  "region": "NA",
  "prizePool": 1000,
  "entryFee": 0,
  "registrationStart": "2025-10-13T09:00:00",
  "registrationEnd": "2025-10-13T11:00:00",
  "checkInStart": "2025-10-13T11:30:00",
  "checkInEnd": "2025-10-13T11:45:00",
  "tournamentStart": "2025-10-13T12:00:00",
  "rules": "Standard Valorant competitive rules. No cheating. Respect opponents.",
  "matchFormat": "BO3",
  "mapPool": ["Bind", "Haven", "Ascent"],
  "allowSubstitutes": true,
  "maxSubstitutes": 2
}
```

### **Test Case 2.2: Publish Tournament**
**Steps:**
1. From organizer dashboard
2. Find the draft tournament
3. Click "Publish"
4. Confirm action

**Expected Result:**
- ✅ Tournament status changes to "OPEN"
- ✅ Tournament appears in public listing
- ✅ Registration is now open

**API Call:**
```bash
POST http://localhost:8080/api/tournaments/{id}/publish
Authorization: Bearer <organizer-token>
```

---

## **Scenario 3: Player Registration**

### **Test Case 3.1: Register Team for Tournament**
**Steps:**
1. Login as team captain
2. Browse tournaments
3. Click on "Summer Championship 2025"
4. Click "Register Now"
5. Select team from dropdown
6. Confirm registration

**Expected Result:**
- ✅ Team registered successfully
- ✅ Team appears in participants list
- ✅ Registration count increases
- ✅ "Unregister" button now visible

**API Call:**
```bash
POST http://localhost:8080/api/tournaments/{id}/register
Authorization: Bearer <user-token>
Content-Type: application/json

{
  "entryType": "TEAM",
  "teamId": 123
}
```

### **Test Case 3.2: Multiple Team Registrations**
**Steps:**
1. Repeat Test Case 3.1 with 7 more teams
2. Verify participant count reaches 8/8

**Expected Result:**
- ✅ All 8 teams registered
- ✅ Tournament shows as "FULL"
- ✅ Registration button disabled for others

---

## **Scenario 4: Check-in Process**

### **Test Case 4.1: Team Check-in**
**Steps:**
1. Wait until check-in window opens (or manually change tournament status)
2. Login as team captain
3. Navigate to tournament page
4. Click "Check In"
5. Confirm check-in

**Expected Result:**
- ✅ Team marked as checked in
- ✅ Check-in timestamp recorded
- ✅ Check-in count increases

**API Call:**
```bash
POST http://localhost:8080/api/tournaments/{id}/check-in
Authorization: Bearer <user-token>
```

### **Test Case 4.2: All Teams Check In**
**Steps:**
1. Have all 8 teams check in
2. Verify check-in count: 8/8

**Expected Result:**
- ✅ All teams checked in
- ✅ Tournament ready for bracket generation

---

## **Scenario 5: Bracket Generation**

### **Test Case 5.1: Generate Single Elimination Bracket**
**Steps:**
1. Login as organizer
2. Navigate to tournament management page
3. Click "Generate Bracket"
4. Confirm action
5. Wait for generation to complete

**Expected Result:**
- ✅ Bracket generated successfully
- ✅ Tournament status changes to "ONGOING"
- ✅ 7 matches created (for 8 teams)
- ✅ Round 1: 4 matches
- ✅ Round 2: 2 matches (Semi-finals)
- ✅ Round 3: 1 match (Finals)
- ✅ All first-round matches have status "READY"

**API Call:**
```bash
POST http://localhost:8080/api/brackets/tournaments/{id}/generate
Authorization: Bearer <organizer-token>
```

### **Test Case 5.2: View Bracket**
**Steps:**
1. Click "View Bracket"
2. Verify bracket visualization

**Expected Result:**
- ✅ Bracket displays all rounds
- ✅ Teams assigned to matches
- ✅ Match cards show team logos
- ✅ Status indicators visible
- ✅ Horizontal scroll works

**API Call:**
```bash
GET http://localhost:8080/api/brackets/tournaments/{id}
```

---

## **Scenario 6: Match Reporting**

### **Test Case 6.1: Start Match**
**Steps:**
1. Login as player from Team 1
2. Navigate to match detail page
3. Click "Start Match"

**Expected Result:**
- ✅ Match status changes to "IN_PROGRESS"
- ✅ Start time recorded
- ✅ Score reporting form appears

**API Call:**
```bash
POST http://localhost:8080/api/matches/{matchId}/start
Authorization: Bearer <player-token>
```

### **Test Case 6.2: Report Score (First Report)**
**Steps:**
1. As Team 1 player, fill score form:
   - Team 1 Score: 2
   - Team 2 Score: 1
   - Add screenshot URL: "https://example.com/screenshot1.png"
   - Match Replay: "https://example.com/replay1"
2. Click "Report Score"

**Expected Result:**
- ✅ Score reported successfully
- ✅ Match shows reported scores
- ✅ Waiting for confirmation from Team 2

**API Call:**
```bash
POST http://localhost:8080/api/matches/{matchId}/report-score
Authorization: Bearer <team1-player-token>
Content-Type: application/json

{
  "participant1Score": 2,
  "participant2Score": 1,
  "screenshotUrls": ["https://example.com/screenshot1.png"],
  "matchReplayUrl": "https://example.com/replay1"
}
```

### **Test Case 6.3: Confirm Score (Second Report)**
**Steps:**
1. Login as Team 2 player
2. Navigate to same match
3. Click "Confirm Score"

**Expected Result:**
- ✅ Match status changes to "COMPLETED"
- ✅ Winner determined (Team 1)
- ✅ Team 1 progresses to next round
- ✅ Next match becomes "READY"
- ✅ Team 2 marked as "ELIMINATED"

**API Call:**
```bash
POST http://localhost:8080/api/matches/{matchId}/confirm-score
Authorization: Bearer <team2-player-token>
```

### **Test Case 6.4: Score Mismatch (Dispute)**
**Steps:**
1. Team 1 reports: 2-1
2. Team 2 reports: 1-2 (different scores)

**Expected Result:**
- ✅ Match status changes to "DISPUTED"
- ✅ Dispute reason recorded
- ✅ Match does not progress
- ✅ Admin notification triggered (if implemented)

---

## **Scenario 7: Tournament Progression**

### **Test Case 7.1: Complete All Round 1 Matches**
**Steps:**
1. Complete all 4 first-round matches
2. Verify winners progress to semi-finals

**Expected Result:**
- ✅ All Round 1 matches completed
- ✅ 4 winners determined
- ✅ Semi-final matches populated with winners
- ✅ Semi-final matches status: "READY"

### **Test Case 7.2: Complete Semi-Finals**
**Steps:**
1. Complete both semi-final matches
2. Verify winners progress to finals

**Expected Result:**
- ✅ Both semi-finals completed
- ✅ 2 finalists determined
- ✅ Finals match populated
- ✅ Finals match status: "READY"

### **Test Case 7.3: Complete Finals**
**Steps:**
1. Complete finals match
2. Verify tournament completion

**Expected Result:**
- ✅ Finals completed
- ✅ Champion determined
- ✅ Tournament status changes to "COMPLETED"
- ✅ Tournament end time recorded
- ✅ Winner's entry status: "WINNER"

---

## **Scenario 8: Double Elimination**

### **Test Case 8.1: Create Double Elimination Tournament**
**Steps:**
1. Create new tournament with format "DOUBLE_ELIMINATION"
2. Register 8 teams
3. Generate bracket

**Expected Result:**
- ✅ Winners bracket created
- ✅ Losers bracket created
- ✅ Proper bracket linking
- ✅ More matches than single elimination

### **Test Case 8.2: Loser Progression**
**Steps:**
1. Complete a winners bracket match
2. Verify loser moves to losers bracket

**Expected Result:**
- ✅ Winner progresses in winners bracket
- ✅ Loser moves to losers bracket
- ✅ Losers bracket match becomes "READY"

---

## **Scenario 9: Round Robin**

### **Test Case 9.1: Create Round Robin Tournament**
**Steps:**
1. Create tournament with format "ROUND_ROBIN"
2. Register 4 teams
3. Generate bracket

**Expected Result:**
- ✅ All teams play each other once
- ✅ 6 matches created (4 choose 2)
- ✅ 3 rounds created

---

## **Scenario 10: Tournament Management**

### **Test Case 10.1: View Participants**
**Steps:**
1. Login as organizer
2. Navigate to tournament management
3. Click "Participants" tab

**Expected Result:**
- ✅ All registered teams listed
- ✅ Check-in status visible
- ✅ Registration timestamp shown
- ✅ Disqualify button available

### **Test Case 10.2: Disqualify Participant**
**Steps:**
1. Click "Disqualify" on a participant
2. Confirm action

**Expected Result:**
- ✅ Participant marked as disqualified
- ✅ Removed from bracket (if not started)
- ✅ Matches updated

### **Test Case 10.3: View Matches**
**Steps:**
1. Click "Matches" tab
2. Review all matches

**Expected Result:**
- ✅ All matches listed by round
- ✅ Scores displayed
- ✅ Status indicators shown
- ✅ Cancel button available

---

## 🐛 Bug Testing Checklist

### **Edge Cases**
- [ ] Register with full tournament
- [ ] Check-in after deadline
- [ ] Report score for completed match
- [ ] Generate bracket with insufficient participants
- [ ] Create tournament with past dates
- [ ] Register same team twice
- [ ] Non-participant tries to report score
- [ ] Negative scores
- [ ] Empty form submissions

### **Validation Testing**
- [ ] Tournament name too short (< 3 chars)
- [ ] Description too short (< 10 chars)
- [ ] Max participants < min participants
- [ ] Registration end before start
- [ ] Tournament start before registration end
- [ ] Invalid URLs
- [ ] Invalid date formats

### **Permission Testing**
- [ ] Non-organizer tries to create tournament
- [ ] Non-participant tries to check in
- [ ] User tries to report score for others' match
- [ ] Regular user tries to generate bracket
- [ ] Non-admin tries to verify organizer

---

## 📊 Performance Testing

### **Load Testing**
- [ ] Create tournament with 128 participants
- [ ] Generate bracket with 64 teams
- [ ] Concurrent registrations (10+ users)
- [ ] Concurrent score reporting
- [ ] View bracket with 100+ matches

### **Response Time Targets**
- Tournament creation: < 500ms
- Bracket generation (16 teams): < 2s
- Bracket generation (64 teams): < 5s
- Match reporting: < 300ms
- Tournament listing: < 200ms

---

## ✅ Test Results Template

```markdown
## Test Execution Report

**Date**: ___________
**Tester**: ___________
**Environment**: Development/Staging/Production

### Summary
- Total Test Cases: ___
- Passed: ___
- Failed: ___
- Blocked: ___
- Pass Rate: ___%

### Failed Tests
1. Test Case ID: ___
   - Expected: ___
   - Actual: ___
   - Severity: Critical/High/Medium/Low
   - Screenshot: ___

### Bugs Found
1. Bug ID: ___
   - Description: ___
   - Steps to Reproduce: ___
   - Severity: ___
   - Status: Open/In Progress/Fixed

### Notes
___________
```

---

## 🚀 Quick Test Commands

### **Backend Health Check**
```bash
curl http://localhost:8080/actuator/health
```

### **Get All Tournaments**
```bash
curl http://localhost:8080/api/tournaments
```

### **Create Test Tournament (Quick)**
```bash
curl -X POST http://localhost:8080/api/tournaments \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d @test-tournament.json
```

### **Generate Bracket**
```bash
curl -X POST http://localhost:8080/api/brackets/tournaments/1/generate \
  -H "Authorization: Bearer <token>"
```

---

## 📝 Testing Notes

### **Important Considerations**
1. Test with realistic data (team names, descriptions)
2. Test timezone handling
3. Test with different user roles
4. Clear browser cache between tests
5. Check browser console for errors
6. Monitor backend logs during testing
7. Test on different browsers (Chrome, Firefox, Safari)
8. Test on mobile devices

### **Common Issues to Watch For**
- CORS errors
- Authentication token expiration
- Date/time format mismatches
- Null pointer exceptions
- Race conditions in concurrent operations
- Memory leaks in long-running tests

---

**Testing Status**: Ready to Begin  
**Estimated Testing Time**: 4-6 hours  
**Recommended Team Size**: 2-3 testers
