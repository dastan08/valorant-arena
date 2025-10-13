export interface User {
  id: number;
  username: string;
  email: string;
  valorantId: string;
  region?: string;
  rank: ValorantRank;
  role: UserRole;
  avatar: string;
  isVerified: boolean;
  team?: Team;
  teamRole?: string; // CAPTAIN, MANAGER, or PLAYER
  stats: UserStats;
  elo: number;
  createdAt: Date;
  updatedAt: Date;
  // Discord fields
  discordId?: string;
  discordUsername?: string;
  discordAvatar?: string;
  profilePictureUrl?: string;
}

export interface UserStats {
  matchesPlayed: number;
  wins: number;
  losses: number;
  kills: number;
  deaths: number;
  assists: number;
  headshotPercentage: number;
  averageCombatScore: number;
}

export enum ValorantRank {
  IRON = 'IRON',
  BRONZE = 'BRONZE',
  SILVER = 'SILVER',
  GOLD = 'GOLD',
  PLATINUM = 'PLATINUM',
  DIAMOND = 'DIAMOND',
  IMMORTAL = 'IMMORTAL',
  RADIANT = 'RADIANT'
}

export enum UserRole {
  USER = 'USER',
  MODERATOR = 'MODERATOR',
  ADMIN = 'ADMIN',
  ORGANIZER = 'ORGANIZER',
  TOURNAMENT_MODERATOR = 'TOURNAMENT_MODERATOR'
}

export interface Team {
  id: number;
  name: string;
  tag: string;
  description: string;
  logo: string;
  captain: User;
  manager?: User;
  members: User[];
  maxMembers: number;
  stats: TeamStats;
  elo: number;
  isActive: boolean;
  createdAt: Date;
  updatedAt: Date;
}

export enum TeamRole {
  CAPTAIN = 'CAPTAIN',
  MANAGER = 'MANAGER',
  PLAYER = 'PLAYER'
}

export interface TeamStats {
  matchesPlayed: number;
  wins: number;
  losses: number;
  winRate: number;
  tournamentsWon: number;
}
