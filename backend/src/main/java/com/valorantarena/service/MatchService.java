package com.valorantarena.service;

import com.valorantarena.dto.ReportScoreDTO;
import com.valorantarena.entity.User;
import com.valorantarena.entity.Team;
import com.valorantarena.model.*;
import com.valorantarena.model.TournamentMatch.MatchStatus;
import com.valorantarena.model.TournamentEntry.EntryStatus;
import com.valorantarena.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private final TournamentMatchRepository matchRepository;
    private final TournamentEntryRepository entryRepository;
    private final BracketRepository bracketRepository;
    private final TournamentRepository tournamentRepository;
    private final UserRepository userRepository;

    @Transactional
    public TournamentMatch reportScore(Long matchId, ReportScoreDTO dto, Long userId) {
        TournamentMatch match = matchRepository.findById(matchId)
            .orElseThrow(() -> new RuntimeException("Match not found"));

        // Validate user is a participant
        if (!isParticipant(match, userId)) {
            throw new RuntimeException("You are not a participant in this match");
        }

        // Validate match status
        if (match.getStatus() != MatchStatus.READY && match.getStatus() != MatchStatus.IN_PROGRESS) {
            throw new RuntimeException("Match is not in a reportable state");
        }

        // Check if already reported by this user
        if (match.getReportedBy() != null && match.getReportedBy().getId().equals(userId)) {
            throw new RuntimeException("You have already reported this match");
        }

        User reporter = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // If first report
        if (match.getReportedBy() == null) {
            match.setParticipant1Score(dto.getParticipant1Score());
            match.setParticipant2Score(dto.getParticipant2Score());
            match.setScreenshotUrls(dto.getScreenshotUrls());
            match.setMatchReplayUrl(dto.getMatchReplayUrl());
            match.setReportedBy(reporter);
            match.setReportedAt(LocalDateTime.now());
            match.setStatus(MatchStatus.IN_PROGRESS);
            
            log.info("Match {} score reported by user {}", matchId, userId);
        } else {
            // Second report - confirm or dispute
            if (scoresMatch(match, dto)) {
                confirmMatch(match, reporter);
                log.info("Match {} confirmed by user {}", matchId, userId);
            } else {
                match.setIsDisputed(true);
                match.setDisputeReason("Score mismatch: First report (" + 
                    match.getParticipant1Score() + "-" + match.getParticipant2Score() + 
                    ") vs Second report (" + 
                    dto.getParticipant1Score() + "-" + dto.getParticipant2Score() + ")");
                match.setStatus(MatchStatus.DISPUTED);
                log.warn("Match {} disputed due to score mismatch", matchId);
            }
        }

        return matchRepository.save(match);
    }

    @Transactional
    public TournamentMatch confirmScore(Long matchId, Long userId) {
        TournamentMatch match = matchRepository.findById(matchId)
            .orElseThrow(() -> new RuntimeException("Match not found"));

        if (!isParticipant(match, userId)) {
            throw new RuntimeException("You are not a participant in this match");
        }

        if (match.getStatus() != MatchStatus.IN_PROGRESS) {
            throw new RuntimeException("Match cannot be confirmed in current state");
        }

        User confirmer = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        confirmMatch(match, confirmer);
        return matchRepository.save(match);
    }

    private void confirmMatch(TournamentMatch match, User confirmer) {
        match.setConfirmedBy(confirmer);
        match.setConfirmedAt(LocalDateTime.now());
        match.setStatus(MatchStatus.COMPLETED);
        match.setActualEndTime(LocalDateTime.now());

        // Determine winner
        TournamentEntry winner;
        TournamentEntry loser;
        
        if (match.getParticipant1Score() > match.getParticipant2Score()) {
            winner = match.getParticipant1();
            loser = match.getParticipant2();
        } else {
            winner = match.getParticipant2();
            loser = match.getParticipant1();
        }

        match.setWinner(winner);

        // Update entry statuses
        if (loser != null) {
            loser.setStatus(EntryStatus.ELIMINATED);
            entryRepository.save(loser);
        }

        // Progress bracket
        progressBracket(match);
    }

    @Transactional
    public void progressBracket(TournamentMatch match) {
        if (match.getWinner() == null) {
            return;
        }

        // Find the bracket for this match
        List<Bracket> brackets = bracketRepository.findByTournamentIdAndRoundNumber(
            match.getTournament().getId(), 
            match.getRoundNumber()
        );

        Bracket currentBracket = brackets.stream()
            .filter(b -> b.getMatch() != null && b.getMatch().getId().equals(match.getId()))
            .findFirst()
            .orElse(null);

        if (currentBracket == null) {
            log.warn("No bracket found for match {}", match.getId());
            return;
        }

        currentBracket.setStatus(Bracket.BracketStatus.COMPLETED);
        bracketRepository.save(currentBracket);

        // Progress winner to next match
        if (currentBracket.getNextMatchBracket() != null) {
            Bracket nextBracket = currentBracket.getNextMatchBracket();
            TournamentMatch nextMatch = nextBracket.getMatch();

            if (nextMatch != null) {
                if (nextMatch.getParticipant1() == null) {
                    nextMatch.setParticipant1(match.getWinner());
                } else if (nextMatch.getParticipant2() == null) {
                    nextMatch.setParticipant2(match.getWinner());
                }

                // If both participants are set, mark match as ready
                if (nextMatch.getParticipant1() != null && nextMatch.getParticipant2() != null) {
                    nextMatch.setStatus(MatchStatus.READY);
                    nextBracket.setStatus(Bracket.BracketStatus.READY);
                }

                matchRepository.save(nextMatch);
                bracketRepository.save(nextBracket);
            }
        }

        // For double elimination: progress loser to losers bracket
        if (currentBracket.getLoserBracket() != null) {
            Bracket loserBracket = currentBracket.getLoserBracket();
            TournamentMatch loserMatch = loserBracket.getMatch();

            if (loserMatch != null) {
                TournamentEntry loser = match.getParticipant1().equals(match.getWinner()) 
                    ? match.getParticipant2() 
                    : match.getParticipant1();

                if (loserMatch.getParticipant1() == null) {
                    loserMatch.setParticipant1(loser);
                } else if (loserMatch.getParticipant2() == null) {
                    loserMatch.setParticipant2(loser);
                }

                if (loserMatch.getParticipant1() != null && loserMatch.getParticipant2() != null) {
                    loserMatch.setStatus(MatchStatus.READY);
                    loserBracket.setStatus(Bracket.BracketStatus.READY);
                }

                matchRepository.save(loserMatch);
                bracketRepository.save(loserBracket);
            }
        }

        // Check if tournament is complete
        checkTournamentCompletion(match.getTournament());
    }

    private void checkTournamentCompletion(Tournament tournament) {
        List<TournamentMatch> allMatches = matchRepository.findByTournamentId(tournament.getId());
        
        boolean allComplete = allMatches.stream()
            .allMatch(m -> m.getStatus() == MatchStatus.COMPLETED || m.getStatus() == MatchStatus.CANCELLED);

        if (allComplete) {
            tournament.setStatus(Tournament.TournamentStatus.COMPLETED);
            tournament.setTournamentEnd(LocalDateTime.now());
            
            // Set winner
            TournamentMatch finalMatch = allMatches.stream()
                .filter(m -> m.getRoundNumber().equals(tournament.getTotalRounds()))
                .findFirst()
                .orElse(null);

            if (finalMatch != null && finalMatch.getWinner() != null) {
                finalMatch.getWinner().setStatus(EntryStatus.WINNER);
                entryRepository.save(finalMatch.getWinner());
            }

            tournamentRepository.save(tournament);
            log.info("Tournament {} completed", tournament.getId());
        }
    }

    @Transactional
    public TournamentMatch startMatch(Long matchId, Long userId) {
        TournamentMatch match = matchRepository.findById(matchId)
            .orElseThrow(() -> new RuntimeException("Match not found"));

        if (!isParticipant(match, userId)) {
            throw new RuntimeException("You are not a participant in this match");
        }

        if (match.getStatus() != MatchStatus.READY) {
            throw new RuntimeException("Match is not ready to start");
        }

        match.setStatus(MatchStatus.IN_PROGRESS);
        match.setActualStartTime(LocalDateTime.now());

        return matchRepository.save(match);
    }

    public List<TournamentMatch> getMatchesByTournament(Long tournamentId) {
        return matchRepository.findByTournamentId(tournamentId);
    }

    public List<TournamentMatch> getMatchesByRound(Long tournamentId, Integer roundNumber) {
        return matchRepository.findByTournamentIdAndRoundNumber(tournamentId, roundNumber);
    }

    public List<TournamentMatch> getUserMatches(Long userId) {
        return matchRepository.findByParticipantUserId(userId);
    }

    public TournamentMatch getMatchById(Long matchId) {
        return matchRepository.findById(matchId)
            .orElseThrow(() -> new RuntimeException("Match not found"));
    }

    private boolean isParticipant(TournamentMatch match, Long userId) {
        if (match.getParticipant1() != null) {
            if (match.getParticipant1().getUser() != null && 
                match.getParticipant1().getUser().getId().equals(userId)) {
                return true;
            }
            if (match.getParticipant1().getTeam() != null) {
                // Check if user is in the team
                return match.getParticipant1().getTeam().getMembers().stream()
                    .anyMatch(m -> m.getId().equals(userId));
            }
        }

        if (match.getParticipant2() != null) {
            if (match.getParticipant2().getUser() != null && 
                match.getParticipant2().getUser().getId().equals(userId)) {
                return true;
            }
            if (match.getParticipant2().getTeam() != null) {
                return match.getParticipant2().getTeam().getMembers().stream()
                    .anyMatch(m -> m.getId().equals(userId));
            }
        }

        return false;
    }

    private boolean scoresMatch(TournamentMatch match, ReportScoreDTO dto) {
        return match.getParticipant1Score().equals(dto.getParticipant1Score()) &&
               match.getParticipant2Score().equals(dto.getParticipant2Score());
    }
}
