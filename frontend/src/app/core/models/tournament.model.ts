import { Team } from './user.model';

export interface Tournament {
  id: number;
  name: string;
  description: string;
  format: string;
  status: string;
  organizer?: any;
  organizerId?: number;
  organizerName?: string;
  organizationName?: string;
  maxTeams?: number;
  maxParticipants?: number;
  minParticipants?: number;
  teamSize?: number;
  prizePool: number;
  entryFee: number;
  prizeDistribution?: any;
  startDate?: Date;
  tournamentStart?: Date;
  tournamentEnd?: Date;
  endDate?: Date;
  registrationStart?: Date;
  registrationEnd?: Date;
  registrationDeadline?: Date;
  checkInStart?: Date;
  checkInEnd?: Date;
  registeredTeams?: Team[];
  matches?: Match[];
  winner?: Team;
  rules?: string;
  banner?: string;
  bannerUrl?: string;
  gameMode?: string;
  matchFormat?: string;
  mapPool?: string[];
  allowSubstitutes?: boolean;
  maxSubstitutes?: number;
  region?: string;
  minRank?: string;
  maxRank?: string;
  currentRound?: number;
  totalRounds?: number;
  isFeatured?: boolean;
  isVerified?: boolean;
  createdAt: Date;
  updatedAt: Date;
}

export enum TournamentFormat {
  SINGLE_ELIMINATION = 'SINGLE_ELIMINATION',
  DOUBLE_ELIMINATION = 'DOUBLE_ELIMINATION',
  ROUND_ROBIN = 'ROUND_ROBIN',
  SWISS = 'SWISS'
}

export enum TournamentStatus {
  DRAFT = 'DRAFT',
  OPEN = 'OPEN',
  REGISTRATION_OPEN = 'REGISTRATION_OPEN',
  REGISTRATION_CLOSED = 'REGISTRATION_CLOSED',
  CHECK_IN = 'CHECK_IN',
  ONGOING = 'ONGOING',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

export interface Match {
  id: number;
  tournament: Tournament;
  team1: Team;
  team2: Team;
  status: MatchStatus;
  team1Score: number;
  team2Score: number;
  winner?: Team;
  scheduledTime: Date;
  startTime?: Date;
  endTime?: Date;
  round: number;
  bestOf: number;
  notes?: string;
  serverInfo?: string;
  createdAt: Date;
  updatedAt: Date;
}

export enum MatchStatus {
  SCHEDULED = 'SCHEDULED',
  READY = 'READY',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
  DISPUTED = 'DISPUTED'
}
