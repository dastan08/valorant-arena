# Matchmaking Page - Quick Access Guide

## 🎮 Where to Find Matchmaking

Players can search for matches in **3 easy ways**:

### **1. Navbar - Desktop** 
Click **"Find Match"** in the top navigation bar (visible when logged in)

### **2. Navbar - Mobile**
Open mobile menu → Click **"Find Match"** (highlighted in red)

### **3. Direct URL**
Navigate to: `http://localhost:4200/matchmaking`

## 📍 Page Location

```
URL: /matchmaking
Route: Protected by AuthGuard (login required)
Component: MatchmakingComponent
```

## 🎯 Features

### **Queue Stats Display**
- Players currently in queue
- Estimated wait time
- Updates every 5 seconds

### **Region Selection**
- North America (NA)
- Europe (EU)
- Asia (ASIA)
- Southeast Asia (SEA)

### **Live Queue Status**
- Real-time wait time counter
- Animated search indicator
- Progress bar
- ELO and rank display

### **Match Found Notification**
- Automatic detection
- Alert notification
- Ready for match acceptance

## 🎨 UI Features

### **Before Joining Queue:**
- Queue statistics
- Region selector
- Large "Find Match" button
- Matchmaking tips

### **While in Queue:**
- Pulsing search animation
- Current ELO and rank badge
- Time in queue counter
- Estimated wait time
- Region display
- Progress bar
- Cancel button

### **Visual Design:**
- VALORANT-themed colors (red/pink gradient)
- Dark background
- Animated elements
- Responsive (mobile + desktop)
- Modern UI with icons

## 🔄 How It Works

1. **Player clicks "Find Match"**
2. **Selects region** (default: NA)
3. **Joins queue** with current ELO
4. **System searches** for 10 players
5. **Match found** → Alert shown
6. **Teams created** → Redirected to match

## 📊 Matchmaking Logic

- **Initial Range:** ±100 ELO
- **Expansion:** +50 ELO per minute
- **Max Range:** ±500 ELO
- **Players Needed:** 10 (5v5)
- **Processing:** Every 5 seconds
- **Timeout:** 10 minutes

## 🛠️ Technical Details

### **Frontend Files:**
```
src/app/
├── pages/matchmaking/
│   ├── matchmaking.component.ts
│   ├── matchmaking.component.html
│   └── matchmaking.component.css
├── services/
│   └── matchmaking.service.ts
└── components/navbar/
    └── navbar.component.html (updated)
```

### **Backend Endpoints:**
```
POST /api/matchmaking/join    - Join queue
POST /api/matchmaking/leave   - Leave queue
GET  /api/matchmaking/status  - Get queue status
GET  /api/matchmaking/stats   - Get queue statistics
```

### **Polling:**
- Queue status checked every 2 seconds
- Stats refreshed every 5 seconds
- Auto-cleanup on component destroy

## 🎮 User Flow

```
Login → Navbar → "Find Match" → Select Region → Join Queue → 
Wait for Match → Match Found → Accept → Play!
```

## 📱 Mobile Experience

- Fully responsive design
- Touch-friendly buttons
- Mobile menu integration
- Same features as desktop

## 🔐 Security

- **Auth Required:** Must be logged in
- **AuthGuard:** Protects route
- **JWT Token:** Sent with all requests
- **User Validation:** Backend verifies user

## 🎨 Styling

- **Primary Color:** VALORANT Red (#FF4655)
- **Gradient:** Red to Pink
- **Background:** Dark (#0f1923)
- **Animations:** Pulse, spin, fade
- **Icons:** Heroicons (search, clock, etc.)

## 📝 Next Steps

1. **Start Backend** - Ensure matchmaking service is running
2. **Start Frontend** - `npm start`
3. **Login** - Create account or login
4. **Navigate** - Click "Find Match" in navbar
5. **Test** - Join queue and wait for match

---

**Matchmaking is now fully accessible and ready to use!** 🎮✨

Players can easily find and join matches from anywhere in the application!
