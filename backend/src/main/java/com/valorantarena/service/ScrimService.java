package com.valorantarena.service;

import com.valorantarena.entity.Scrim;
import com.valorantarena.entity.Team;
import com.valorantarena.entity.User;
import com.valorantarena.repository.ScrimRepository;
import com.valorantarena.repository.TeamRepository;
import com.valorantarena.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScrimService {
    
    private final ScrimRepository scrimRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final DiscordNotificationService discordNotificationService;
    
    /**
     * Challenge another team to a scrim
     */
    @Transactional
    public Scrim challengeTeam(Long challengerTeamId, Long opponentTeamId, Long requesterId, 
                               LocalDateTime scheduledTime, String message, String map, Integer bestOf) {
        // Validate requester
        User requester = userRepository.findById(requesterId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Team challengerTeam = teamRepository.findById(challengerTeamId)
            .orElseThrow(() -> new RuntimeException("Challenger team not found"));
        
        Team opponentTeam = teamRepository.findById(opponentTeamId)
            .orElseThrow(() -> new RuntimeException("Opponent team not found"));
        
        // Check if requester can manage the challenger team
        if (!challengerTeam.canManageTeam(requester)) {
            throw new RuntimeException("Only captain or manager can challenge other teams");
        }
        
        // Cannot challenge own team
        if (challengerTeamId.equals(opponentTeamId)) {
            throw new RuntimeException("Cannot challenge your own team");
        }
        
        // Validate scheduled time is in the future
        if (scheduledTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Scheduled time must be in the future");
        }
        
        // Create scrim
        Scrim scrim = new Scrim();
        scrim.setChallengerTeam(challengerTeam);
        scrim.setOpponentTeam(opponentTeam);
        scrim.setScheduledTime(scheduledTime);
        scrim.setMessage(message);
        scrim.setMap(map != null ? map : "TBD");
        scrim.setBestOf(bestOf != null ? bestOf : 1);
        scrim.setStatus(Scrim.ScrimStatus.PENDING);
        
        scrim = scrimRepository.save(scrim);
        
        // Notify opponent team
        List<User> opponentMembers = userRepository.findByTeamId(opponentTeamId);
        try {
            String notification = String.format(
                "⚔️ **Scrim Challenge**\n\n" +
                "**%s** has challenged your team to a scrim!\n\n" +
                "**Scheduled:** %s\n" +
                "**Map:** %s\n" +
                "**Best of:** %d\n" +
                "**Message:** %s",
                challengerTeam.getName(),
                scheduledTime.toString(),
                scrim.getMap(),
                scrim.getBestOf(),
                message != null ? message : "Good luck!"
            );
            discordNotificationService.notifyTeamMembers(opponentMembers, notification);
        } catch (Exception e) {
            log.error("Failed to send Discord notifications: {}", e.getMessage());
        }
        
        log.info("Scrim challenge created: {} vs {}", challengerTeam.getName(), opponentTeam.getName());
        
        return scrim;
    }
    
    /**
     * Accept a scrim challenge
     */
    @Transactional
    public Scrim acceptScrim(Long scrimId, Long requesterId) {
        Scrim scrim = scrimRepository.findById(scrimId)
            .orElseThrow(() -> new RuntimeException("Scrim not found"));
        
        User requester = userRepository.findById(requesterId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if requester can manage the opponent team
        if (!scrim.getOpponentTeam().canManageTeam(requester)) {
            throw new RuntimeException("Only captain or manager of the challenged team can accept");
        }
        
        // Check if scrim is pending
        if (scrim.getStatus() != Scrim.ScrimStatus.PENDING) {
            throw new RuntimeException("Scrim is not pending");
        }
        
        scrim.setStatus(Scrim.ScrimStatus.ACCEPTED);
        scrim.setRespondedAt(LocalDateTime.now());
        scrim = scrimRepository.save(scrim);
        
        // Notify both teams
        List<User> challengerMembers = userRepository.findByTeamId(scrim.getChallengerTeam().getId());
        List<User> opponentMembers = userRepository.findByTeamId(scrim.getOpponentTeam().getId());
        
        try {
            String notification = String.format(
                "✅ **Scrim Accepted**\n\n" +
                "The scrim between **%s** and **%s** is confirmed!\n\n" +
                "**Scheduled:** %s\n" +
                "**Map:** %s\n" +
                "**Best of:** %d\n\n" +
                "See you on the battlefield!",
                scrim.getChallengerTeam().getName(),
                scrim.getOpponentTeam().getName(),
                scrim.getScheduledTime().toString(),
                scrim.getMap(),
                scrim.getBestOf()
            );
            discordNotificationService.notifyTeamMembers(challengerMembers, notification);
            discordNotificationService.notifyTeamMembers(opponentMembers, notification);
        } catch (Exception e) {
            log.error("Failed to send Discord notifications: {}", e.getMessage());
        }
        
        log.info("Scrim accepted: {} vs {}", scrim.getChallengerTeam().getName(), scrim.getOpponentTeam().getName());
        
        return scrim;
    }
    
    /**
     * Decline a scrim challenge
     */
    @Transactional
    public Scrim declineScrim(Long scrimId, Long requesterId, String reason) {
        Scrim scrim = scrimRepository.findById(scrimId)
            .orElseThrow(() -> new RuntimeException("Scrim not found"));
        
        User requester = userRepository.findById(requesterId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if requester can manage the opponent team
        if (!scrim.getOpponentTeam().canManageTeam(requester)) {
            throw new RuntimeException("Only captain or manager of the challenged team can decline");
        }
        
        // Check if scrim is pending
        if (scrim.getStatus() != Scrim.ScrimStatus.PENDING) {
            throw new RuntimeException("Scrim is not pending");
        }
        
        scrim.setStatus(Scrim.ScrimStatus.DECLINED);
        scrim.setRespondedAt(LocalDateTime.now());
        scrim = scrimRepository.save(scrim);
        
        // Notify challenger team
        List<User> challengerMembers = userRepository.findByTeamId(scrim.getChallengerTeam().getId());
        
        try {
            String notification = String.format(
                "❌ **Scrim Declined**\n\n" +
                "**%s** has declined your scrim challenge.\n\n" +
                "%s",
                scrim.getOpponentTeam().getName(),
                reason != null ? "**Reason:** " + reason : ""
            );
            discordNotificationService.notifyTeamMembers(challengerMembers, notification);
        } catch (Exception e) {
            log.error("Failed to send Discord notifications: {}", e.getMessage());
        }
        
        log.info("Scrim declined: {} vs {}", scrim.getChallengerTeam().getName(), scrim.getOpponentTeam().getName());
        
        return scrim;
    }
    
    /**
     * Cancel a scrim
     */
    @Transactional
    public Scrim cancelScrim(Long scrimId, Long requesterId, String reason) {
        Scrim scrim = scrimRepository.findById(scrimId)
            .orElseThrow(() -> new RuntimeException("Scrim not found"));
        
        User requester = userRepository.findById(requesterId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if requester can manage either team
        boolean canCancel = scrim.getChallengerTeam().canManageTeam(requester) || 
                           scrim.getOpponentTeam().canManageTeam(requester);
        
        if (!canCancel) {
            throw new RuntimeException("Only team captains or managers can cancel scrims");
        }
        
        // Can only cancel pending or accepted scrims
        if (scrim.getStatus() != Scrim.ScrimStatus.PENDING && 
            scrim.getStatus() != Scrim.ScrimStatus.ACCEPTED) {
            throw new RuntimeException("Cannot cancel this scrim");
        }
        
        scrim.setStatus(Scrim.ScrimStatus.CANCELLED);
        scrim = scrimRepository.save(scrim);
        
        // Notify both teams
        List<User> challengerMembers = userRepository.findByTeamId(scrim.getChallengerTeam().getId());
        List<User> opponentMembers = userRepository.findByTeamId(scrim.getOpponentTeam().getId());
        
        try {
            String notification = String.format(
                "🚫 **Scrim Cancelled**\n\n" +
                "The scrim between **%s** and **%s** has been cancelled.\n\n" +
                "%s",
                scrim.getChallengerTeam().getName(),
                scrim.getOpponentTeam().getName(),
                reason != null ? "**Reason:** " + reason : ""
            );
            discordNotificationService.notifyTeamMembers(challengerMembers, notification);
            discordNotificationService.notifyTeamMembers(opponentMembers, notification);
        } catch (Exception e) {
            log.error("Failed to send Discord notifications: {}", e.getMessage());
        }
        
        log.info("Scrim cancelled: {} vs {}", scrim.getChallengerTeam().getName(), scrim.getOpponentTeam().getName());
        
        return scrim;
    }
    
    /**
     * Get all scrims for a team
     */
    public List<Scrim> getTeamScrims(Long teamId) {
        return scrimRepository.findByTeamId(teamId);
    }
    
    /**
     * Get pending scrim requests for a team
     */
    public List<Scrim> getPendingScrims(Long teamId) {
        return scrimRepository.findPendingScrimsForTeam(teamId);
    }
    
    /**
     * Get upcoming scrims for a team
     */
    public List<Scrim> getUpcomingScrims(Long teamId) {
        return scrimRepository.findUpcomingScrimsForTeam(teamId, LocalDateTime.now());
    }
    
    /**
     * Get scrim by ID
     */
    public Scrim getScrimById(Long scrimId) {
        return scrimRepository.findById(scrimId)
            .orElseThrow(() -> new RuntimeException("Scrim not found"));
    }
}
