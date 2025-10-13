const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');

const userSchema = new mongoose.Schema(
  {
    username: {
      type: String,
      required: true,
      unique: true,
      trim: true,
      minlength: 3,
      maxlength: 30,
    },
    email: {
      type: String,
      required: true,
      unique: true,
      trim: true,
      lowercase: true,
    },
    password: {
      type: String,
      required: true,
      minlength: 6,
      select: false,
    },
    valorantId: {
      type: String,
      required: true,
      unique: true,
      trim: true,
    },
    rank: {
      type: String,
      enum: [
        'Iron', 'Bronze', 'Silver', 'Gold',
        'Platinum', 'Diamond', 'Immortal', 'Radiant'
      ],
      required: true,
    },
    role: {
      type: String,
      enum: ['user', 'admin', 'moderator'],
      default: 'user',
    },
    avatar: {
      type: String,
      default: 'default-avatar.png',
    },
    isVerified: {
      type: Boolean,
      default: false,
    },
    team: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Team',
    },
    stats: {
      matchesPlayed: { type: Number, default: 0 },
      wins: { type: Number, default: 0 },
      losses: { type: Number, default: 0 },
      kills: { type: Number, default: 0 },
      deaths: { type: Number, default: 0 },
      assists: { type: Number, default: 0 },
      headshotPercentage: { type: Number, default: 0 },
      averageCombatScore: { type: Number, default: 0 },
    },
    elo: {
      type: Number,
      default: 1000,
    },
  },
  {
    timestamps: true,
    toJSON: {
      transform: function (doc, ret) {
        delete ret.password;
        delete ret.__v;
        return ret;
      },
    },
  }
);

// Hash password before saving
userSchema.pre('save', async function (next) {
  if (!this.isModified('password')) return next();
  
  try {
    const salt = await bcrypt.genSalt(10);
    this.password = await bcrypt.hash(this.password, salt);
    next();
  } catch (error) {
    next(error);
  }
});

// Method to compare passwords
userSchema.methods.matchPassword = async function (enteredPassword) {
  return await bcrypt.compare(enteredPassword, this.password);
};

// Update user stats after a match
userSchema.methods.updateStats = function (isWin, matchStats) {
  this.stats.matchesPlayed += 1;
  
  if (isWin) {
    this.stats.wins += 1;
    this.elo += 25; // Base ELO gain for win
  } else {
    this.stats.losses += 1;
    this.elo = Math.max(0, this.elo - 15); // Base ELO loss, never go below 0
  }
  
  // Update combat stats
  this.stats.kills += matchStats.kills || 0;
  this.stats.deaths += matchStats.deaths || 0;
  this.stats.assists += matchStats.assists || 0;
  
  // Calculate new averages
  const totalKills = this.stats.kills;
  const totalDeaths = this.stats.deaths;
  
  if (totalDeaths > 0) {
    this.stats.averageCombatScore = Math.round(
      (this.stats.kills * 150 + this.stats.assists * 50) / this.stats.matchesPlayed
    );
    
    if (matchStats.headshots && matchStats.shots) {
      const matchHSRate = (matchStats.headshots / matchStats.shots) * 100;
      this.stats.headshotPercentage = Math.round(
        (this.stats.headshotPercentage * (this.stats.matchesPlayed - 1) + matchHSRate) / 
        this.stats.matchesPlayed
      );
    }
  }
  
  return this.save();
};

const User = mongoose.model('User', userSchema);

module.exports = User;
