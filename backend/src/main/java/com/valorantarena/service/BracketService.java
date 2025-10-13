package com.valorantarena.service;

import com.valorantarena.model.*;
import com.valorantarena.model.Bracket.BracketStatus;
import com.valorantarena.model.Bracket.BracketType;
import com.valorantarena.model.Tournament.TournamentFormat;
import com.valorantarena.model.Tournament.TournamentStatus;
import com.valorantarena.model.TournamentMatch.MatchStatus;
import com.valorantarena.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BracketService {

    private final BracketRepository bracketRepository;
    private final TournamentRepository tournamentRepository;
    private final TournamentEntryRepository entryRepository;
    private final TournamentMatchRepository matchRepository;

    @Transactional
    public void generateBracket(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
            .orElseThrow(() -> new RuntimeException("Tournament not found"));

        // Validate tournament status
        if (tournament.getStatus() != TournamentStatus.CHECK_IN) {
            throw new RuntimeException("Tournament must be in CHECK_IN status to generate bracket");
        }

        // Get checked-in participants
        List<TournamentEntry> participants = entryRepository
            .findByTournamentIdAndCheckedIn(tournamentId, true);

        if (participants.size() < tournament.getMinParticipants()) {
            throw new RuntimeException("Not enough participants checked in");
        }

        // Clear existing brackets if any
        bracketRepository.deleteByTournamentId(tournamentId);

        // Generate bracket based on format
        switch (tournament.getFormat()) {
            case SINGLE_ELIMINATION:
                generateSingleEliminationBracket(tournament, participants);
                break;
            case DOUBLE_ELIMINATION:
                generateDoubleEliminationBracket(tournament, participants);
                break;
            case ROUND_ROBIN:
                generateRoundRobinBracket(tournament, participants);
                break;
            case SWISS:
                generateSwissBracket(tournament, participants);
                break;
            default:
                throw new RuntimeException("Unsupported tournament format");
        }

        // Update tournament status
        tournament.setStatus(TournamentStatus.ONGOING);
        tournament.setCurrentRound(1);
        tournamentRepository.save(tournament);

        log.info("Generated bracket for tournament {} with {} participants", tournamentId, participants.size());
    }

    private void generateSingleEliminationBracket(Tournament tournament, List<TournamentEntry> participants) {
        // Shuffle for random seeding (or implement rank-based seeding)
        List<TournamentEntry> seededParticipants = seedParticipants(participants);
        
        int participantCount = seededParticipants.size();
        int totalRounds = (int) Math.ceil(Math.log(participantCount) / Math.log(2));
        tournament.setTotalRounds(totalRounds);

        // Calculate first round matches
        int firstRoundMatches = (int) Math.pow(2, totalRounds - 1);
        int byes = firstRoundMatches * 2 - participantCount;

        List<Bracket> allBrackets = new ArrayList<>();
        List<TournamentMatch> allMatches = new ArrayList<>();
        Map<String, Bracket> bracketMap = new HashMap<>();

        // Generate all bracket positions first
        for (int round = 1; round <= totalRounds; round++) {
            int matchesInRound = (int) Math.pow(2, totalRounds - round);
            for (int matchNum = 1; matchNum <= matchesInRound; matchNum++) {
                Bracket bracket = new Bracket();
                bracket.setTournament(tournament);
                bracket.setBracketType(BracketType.WINNERS);
                bracket.setRoundNumber(round);
                bracket.setMatchNumber(matchNum);
                bracket.setStatus(BracketStatus.PENDING);
                
                String key = round + "-" + matchNum;
                bracketMap.put(key, bracket);
                allBrackets.add(bracket);
            }
        }

        // Link brackets (next match progression)
        for (int round = 1; round < totalRounds; round++) {
            int matchesInRound = (int) Math.pow(2, totalRounds - round);
            for (int matchNum = 1; matchNum <= matchesInRound; matchNum++) {
                String currentKey = round + "-" + matchNum;
                int nextMatchNum = (matchNum + 1) / 2;
                String nextKey = (round + 1) + "-" + nextMatchNum;
                
                Bracket currentBracket = bracketMap.get(currentKey);
                Bracket nextBracket = bracketMap.get(nextKey);
                currentBracket.setNextMatchBracket(nextBracket);
            }
        }

        // Create first round matches
        int participantIndex = 0;
        int matchesInFirstRound = (int) Math.pow(2, totalRounds - 1);
        
        for (int matchNum = 1; matchNum <= matchesInFirstRound; matchNum++) {
            TournamentMatch match = new TournamentMatch();
            match.setTournament(tournament);
            match.setRoundNumber(1);
            match.setMatchNumber(matchNum);
            match.setStatus(MatchStatus.SCHEDULED);
            match.setBestOf(parseBestOf(tournament.getMatchFormat()));

            // Assign participants (handle byes)
            if (participantIndex < seededParticipants.size()) {
                match.setParticipant1(seededParticipants.get(participantIndex++));
            }
            if (participantIndex < seededParticipants.size()) {
                match.setParticipant2(seededParticipants.get(participantIndex++));
            }

            // If only one participant, it's a bye
            if (match.getParticipant2() == null && match.getParticipant1() != null) {
                match.setStatus(MatchStatus.COMPLETED);
                match.setWinner(match.getParticipant1());
            }

            allMatches.add(match);
            
            String bracketKey = "1-" + matchNum;
            Bracket bracket = bracketMap.get(bracketKey);
            bracket.setMatch(match);
            bracket.setStatus(match.getParticipant1() != null && match.getParticipant2() != null 
                ? BracketStatus.READY : BracketStatus.COMPLETED);
        }

        // Save all
        matchRepository.saveAll(allMatches);
        bracketRepository.saveAll(allBrackets);
    }

    private void generateDoubleEliminationBracket(Tournament tournament, List<TournamentEntry> participants) {
        List<TournamentEntry> seededParticipants = seedParticipants(participants);
        
        int participantCount = seededParticipants.size();
        int winnersRounds = (int) Math.ceil(Math.log(participantCount) / Math.log(2));
        int losersRounds = (winnersRounds - 1) * 2;
        int totalRounds = winnersRounds + losersRounds + 1; // +1 for grand finals
        
        tournament.setTotalRounds(totalRounds);

        List<Bracket> allBrackets = new ArrayList<>();
        List<TournamentMatch> allMatches = new ArrayList<>();

        // Generate winners bracket (similar to single elimination)
        generateWinnersBracket(tournament, seededParticipants, winnersRounds, allBrackets, allMatches);
        
        // Generate losers bracket
        generateLosersBracket(tournament, losersRounds, allBrackets, allMatches);

        // Save all
        matchRepository.saveAll(allMatches);
        bracketRepository.saveAll(allBrackets);
    }

    private void generateWinnersBracket(Tournament tournament, List<TournamentEntry> participants, 
                                       int rounds, List<Bracket> allBrackets, List<TournamentMatch> allMatches) {
        int participantIndex = 0;
        
        for (int round = 1; round <= rounds; round++) {
            int matchesInRound = (int) Math.pow(2, rounds - round);
            
            for (int matchNum = 1; matchNum <= matchesInRound; matchNum++) {
                TournamentMatch match = new TournamentMatch();
                match.setTournament(tournament);
                match.setRoundNumber(round);
                match.setMatchNumber(matchNum);
                match.setStatus(MatchStatus.SCHEDULED);
                match.setBestOf(parseBestOf(tournament.getMatchFormat()));

                if (round == 1 && participantIndex < participants.size()) {
                    match.setParticipant1(participants.get(participantIndex++));
                    if (participantIndex < participants.size()) {
                        match.setParticipant2(participants.get(participantIndex++));
                    }
                }

                allMatches.add(match);

                Bracket bracket = new Bracket();
                bracket.setTournament(tournament);
                bracket.setBracketType(BracketType.WINNERS);
                bracket.setRoundNumber(round);
                bracket.setMatchNumber(matchNum);
                bracket.setMatch(match);
                bracket.setStatus(round == 1 ? BracketStatus.READY : BracketStatus.PENDING);
                
                allBrackets.add(bracket);
            }
        }
    }

    private void generateLosersBracket(Tournament tournament, int rounds, 
                                      List<Bracket> allBrackets, List<TournamentMatch> allMatches) {
        // Losers bracket generation logic
        for (int round = 1; round <= rounds; round++) {
            int matchesInRound = calculateLosersBracketMatches(round, rounds);
            
            for (int matchNum = 1; matchNum <= matchesInRound; matchNum++) {
                TournamentMatch match = new TournamentMatch();
                match.setTournament(tournament);
                match.setRoundNumber(round);
                match.setMatchNumber(matchNum);
                match.setStatus(MatchStatus.SCHEDULED);
                match.setBestOf(parseBestOf(tournament.getMatchFormat()));

                allMatches.add(match);

                Bracket bracket = new Bracket();
                bracket.setTournament(tournament);
                bracket.setBracketType(BracketType.LOSERS);
                bracket.setRoundNumber(round);
                bracket.setMatchNumber(matchNum);
                bracket.setMatch(match);
                bracket.setStatus(BracketStatus.PENDING);
                
                allBrackets.add(bracket);
            }
        }
    }

    private void generateRoundRobinBracket(Tournament tournament, List<TournamentEntry> participants) {
        int n = participants.size();
        int totalRounds = n % 2 == 0 ? n - 1 : n;
        int matchesPerRound = n / 2;
        
        tournament.setTotalRounds(totalRounds);

        List<TournamentMatch> allMatches = new ArrayList<>();
        List<Bracket> allBrackets = new ArrayList<>();

        // Round-robin algorithm
        List<TournamentEntry> rotatingList = new ArrayList<>(participants);
        if (n % 2 != 0) {
            rotatingList.add(null); // Add bye
        }

        for (int round = 1; round <= totalRounds; round++) {
            for (int matchNum = 1; matchNum <= matchesPerRound; matchNum++) {
                int home = matchNum - 1;
                int away = rotatingList.size() - matchNum;

                TournamentEntry p1 = rotatingList.get(home);
                TournamentEntry p2 = rotatingList.get(away);

                if (p1 != null && p2 != null) {
                    TournamentMatch match = new TournamentMatch();
                    match.setTournament(tournament);
                    match.setRoundNumber(round);
                    match.setMatchNumber(matchNum);
                    match.setParticipant1(p1);
                    match.setParticipant2(p2);
                    match.setStatus(MatchStatus.SCHEDULED);
                    match.setBestOf(parseBestOf(tournament.getMatchFormat()));

                    allMatches.add(match);

                    Bracket bracket = new Bracket();
                    bracket.setTournament(tournament);
                    bracket.setBracketType(BracketType.WINNERS);
                    bracket.setRoundNumber(round);
                    bracket.setMatchNumber(matchNum);
                    bracket.setMatch(match);
                    bracket.setStatus(BracketStatus.READY);
                    
                    allBrackets.add(bracket);
                }
            }

            // Rotate for next round (keep first fixed, rotate rest)
            if (round < totalRounds) {
                TournamentEntry first = rotatingList.get(0);
                TournamentEntry last = rotatingList.remove(rotatingList.size() - 1);
                rotatingList.add(1, last);
            }
        }

        matchRepository.saveAll(allMatches);
        bracketRepository.saveAll(allBrackets);
    }

    private void generateSwissBracket(Tournament tournament, List<TournamentEntry> participants) {
        int rounds = (int) Math.ceil(Math.log(participants.size()) / Math.log(2));
        tournament.setTotalRounds(rounds);

        // First round: random pairing
        List<TournamentEntry> shuffled = new ArrayList<>(participants);
        Collections.shuffle(shuffled);

        List<TournamentMatch> matches = new ArrayList<>();
        List<Bracket> brackets = new ArrayList<>();

        for (int i = 0; i < shuffled.size(); i += 2) {
            if (i + 1 < shuffled.size()) {
                TournamentMatch match = new TournamentMatch();
                match.setTournament(tournament);
                match.setRoundNumber(1);
                match.setMatchNumber((i / 2) + 1);
                match.setParticipant1(shuffled.get(i));
                match.setParticipant2(shuffled.get(i + 1));
                match.setStatus(MatchStatus.SCHEDULED);
                match.setBestOf(parseBestOf(tournament.getMatchFormat()));

                matches.add(match);

                Bracket bracket = new Bracket();
                bracket.setTournament(tournament);
                bracket.setBracketType(BracketType.WINNERS);
                bracket.setRoundNumber(1);
                bracket.setMatchNumber(match.getMatchNumber());
                bracket.setMatch(match);
                bracket.setStatus(BracketStatus.READY);
                
                brackets.add(bracket);
            }
        }

        matchRepository.saveAll(matches);
        bracketRepository.saveAll(brackets);
    }

    public List<Bracket> getTournamentBracket(Long tournamentId) {
        return bracketRepository.findByTournamentIdOrderByRoundNumberAscMatchNumberAsc(tournamentId);
    }

    public List<Bracket> getBracketByType(Long tournamentId, BracketType bracketType) {
        return bracketRepository.findByTournamentIdAndBracketType(tournamentId, bracketType);
    }

    private List<TournamentEntry> seedParticipants(List<TournamentEntry> participants) {
        // Simple random seeding for now
        // TODO: Implement rank-based seeding
        List<TournamentEntry> seeded = new ArrayList<>(participants);
        Collections.shuffle(seeded);
        return seeded;
    }

    private int parseBestOf(String matchFormat) {
        if (matchFormat == null) return 1;
        if (matchFormat.startsWith("BO")) {
            try {
                return Integer.parseInt(matchFormat.substring(2));
            } catch (NumberFormatException e) {
                return 1;
            }
        }
        return 1;
    }

    private int calculateLosersBracketMatches(int round, int totalRounds) {
        // Simplified calculation for losers bracket
        return (int) Math.pow(2, (totalRounds - round) / 2);
    }
}
