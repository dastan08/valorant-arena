package com.valorantarena.service;

import com.valorantarena.dto.CreateTeamRequest;
import com.valorantarena.dto.InvitePlayerRequest;
import com.valorantarena.dto.TeamInvitationResponse;
import com.valorantarena.entity.Team;
import com.valorantarena.entity.TeamInvitation;
import com.valorantarena.entity.User;
import com.valorantarena.repository.TeamInvitationRepository;
import com.valorantarena.repository.TeamRepository;
import com.valorantarena.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamManagementService {
    
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamInvitationRepository invitationRepository;
    private final DiscordNotificationService discordNotificationService;
    
    private static final int INVITATION_EXPIRY_DAYS = 7;
    
    /**
     * Create a new team
     */
    @Transactional
    public Team createTeam(Long captainId, CreateTeamRequest request) {
        User captain = userRepository.findById(captainId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user already has a team
        if (captain.getTeam() != null) {
            throw new RuntimeException("You are already in a team. Leave your current team first.");
        }
        
        // Check if team name or tag already exists
        if (teamRepository.existsByName(request.getName())) {
            throw new RuntimeException("Team name already taken");
        }
        
        if (teamRepository.existsByTag(request.getTag())) {
            throw new RuntimeException("Team tag already taken");
        }
        
        // Create team
        Team team = new Team();
        team.setName(request.getName());
        team.setTag(request.getTag());
        team.setDescription(request.getDescription());
        team.setLogo(request.getLogo() != null ? request.getLogo() : "default-team-logo.png");
        team.setCaptain(captain);
        team.setManager(captain); // Captain is also the manager initially
        team.setMaxMembers(request.getMaxMembers());
        team.setCreatedAt(LocalDateTime.now());
        
        team = teamRepository.save(team);
        
        // Add captain to team with CAPTAIN role
        captain.setTeam(team);
        captain.setTeamRole(com.valorantarena.enums.TeamRole.CAPTAIN);
        userRepository.save(captain);
        
        log.info("Team created: {} by captain {}", team.getName(), captain.getUsername());
        
        return team;
    }
    
    /**
     * Invite a player to the team
     */
    @Transactional
    public TeamInvitation invitePlayer(Long teamId, Long inviterId, InvitePlayerRequest request) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new RuntimeException("Team not found"));
        
        User inviter = userRepository.findById(inviterId)
            .orElseThrow(() -> new RuntimeException("Inviter not found"));
        
        User invitedUser = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("User to invite not found"));
        
        // Verify inviter is captain or member of the team
        if (!team.getCaptain().getId().equals(inviterId) && 
            (inviter.getTeam() == null || !inviter.getTeam().getId().equals(teamId))) {
            throw new RuntimeException("Only team captain or members can send invitations");
        }
        
        // Check if user is already in a team
        if (invitedUser.getTeam() != null) {
            throw new RuntimeException("User is already in a team");
        }
        
        // Check if team is full
        long currentMembers = userRepository.findByTeamId(teamId).size();
        if (currentMembers >= team.getMaxMembers()) {
            throw new RuntimeException("Team is full");
        }
        
        // Check if invitation already exists
        if (invitationRepository.findByTeamAndInvitedUserAndStatus(
                team, invitedUser, TeamInvitation.InvitationStatus.PENDING).isPresent()) {
            throw new RuntimeException("Invitation already sent to this user");
        }
        
        // Create invitation
        TeamInvitation invitation = new TeamInvitation();
        invitation.setTeam(team);
        invitation.setInvitedUser(invitedUser);
        invitation.setInvitedBy(inviter);
        invitation.setMessage(request.getMessage());
        invitation.setStatus(TeamInvitation.InvitationStatus.PENDING);
        invitation.setCreatedAt(LocalDateTime.now());
        
        invitation = invitationRepository.save(invitation);
        
        // Send Discord notification
        try {
            discordNotificationService.sendTeamInvitationNotification(
                invitedUser, team.getName(), inviter.getUsername(), request.getMessage());
        } catch (Exception e) {
            log.error("Failed to send Discord notification: {}", e.getMessage());
        }
        
        log.info("Team invitation sent: {} invited {} to team {}", 
            inviter.getUsername(), invitedUser.getUsername(), team.getName());
        
        return invitation;
    }
    
    /**
     * Accept team invitation
     */
    @Transactional
    public Team acceptInvitation(Long invitationId, Long userId) {
        TeamInvitation invitation = invitationRepository.findById(invitationId)
            .orElseThrow(() -> new RuntimeException("Invitation not found"));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Verify invitation is for this user
        if (!invitation.getInvitedUser().getId().equals(userId)) {
            throw new RuntimeException("This invitation is not for you");
        }
        
        // Verify invitation is still pending
        if (invitation.getStatus() != TeamInvitation.InvitationStatus.PENDING) {
            throw new RuntimeException("Invitation is no longer valid");
        }
        
        // Check if user is already in a team
        if (user.getTeam() != null) {
            throw new RuntimeException("You are already in a team");
        }
        
        Team team = invitation.getTeam();
        
        // Check if team is full
        long currentMembers = userRepository.findByTeamId(team.getId()).size();
        if (currentMembers >= team.getMaxMembers()) {
            throw new RuntimeException("Team is full");
        }
        
        // Accept invitation
        invitation.setStatus(TeamInvitation.InvitationStatus.ACCEPTED);
        invitation.setRespondedAt(LocalDateTime.now());
        invitationRepository.save(invitation);
        
        // Add user to team
        user.setTeam(team);
        user.setTeamRole(com.valorantarena.enums.TeamRole.PLAYER);
        userRepository.save(user);
        
        // Get all team members for notification
        List<User> teamMembers = userRepository.findByTeamId(team.getId());
        
        // Send Discord notifications
        try {
            // Notify captain
            discordNotificationService.sendInvitationAcceptedNotification(
                team.getCaptain(), user.getUsername(), team.getName());
            // Notify all team members
            discordNotificationService.sendNewMemberJoinedNotification(
                teamMembers, user.getUsername(), team.getName());
        } catch (Exception e) {
            log.error("Failed to send Discord notification: {}", e.getMessage());
        }
        
        log.info("User {} joined team {}", user.getUsername(), team.getName());
        
        return team;
    }
    
    /**
     * Decline team invitation
     */
    @Transactional
    public void declineInvitation(Long invitationId, Long userId) {
        TeamInvitation invitation = invitationRepository.findById(invitationId)
            .orElseThrow(() -> new RuntimeException("Invitation not found"));
        
        // Verify invitation is for this user
        if (!invitation.getInvitedUser().getId().equals(userId)) {
            throw new RuntimeException("This invitation is not for you");
        }
        
        // Verify invitation is still pending
        if (invitation.getStatus() != TeamInvitation.InvitationStatus.PENDING) {
            throw new RuntimeException("Invitation is no longer valid");
        }
        
        // Decline invitation
        invitation.setStatus(TeamInvitation.InvitationStatus.DECLINED);
        invitation.setRespondedAt(LocalDateTime.now());
        invitationRepository.save(invitation);
        
        // Send Discord notification to team captain
        try {
            discordNotificationService.sendInvitationDeclinedNotification(
                invitation.getTeam().getCaptain(), 
                invitation.getInvitedUser().getUsername(), 
                invitation.getTeam().getName());
        } catch (Exception e) {
            log.error("Failed to send Discord notification: {}", e.getMessage());
        }
        
        log.info("User {} declined invitation to team {}", 
            invitation.getInvitedUser().getUsername(), invitation.getTeam().getName());
    }
    
    /**
     * Leave team
     * Captain must transfer captaincy before leaving
     */
    @Transactional
    public void leaveTeam(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Team team = user.getTeam();
        if (team == null) {
            throw new RuntimeException("You are not in a team");
        }
        
        String teamName = team.getName();
        String username = user.getUsername();
        
        // Get remaining members before user leaves
        List<User> allMembers = userRepository.findByTeamId(team.getId());
        
        // If user is captain, they must transfer captaincy first
        if (team.getCaptain().getId().equals(userId)) {
            if (allMembers.size() > 1) {
                throw new RuntimeException("Captain must transfer captaincy to another member before leaving the team");
            } else {
                // Captain is the only member - disband team
                teamRepository.delete(team);
                user.setTeam(null);
                user.setTeamRole(null);
                userRepository.save(user);
                log.info("Team {} disbanded as captain {} was the only member", teamName, username);
                return;
            }
        }
        
        // Remove user from team
        user.setTeam(null);
        user.setTeamRole(null);
        userRepository.save(user);
        
        // Notify remaining team members
        List<User> remainingMembers = userRepository.findByTeamId(team.getId());
        try {
            discordNotificationService.sendMemberLeftNotification(remainingMembers, username, teamName);
        } catch (Exception e) {
            log.error("Failed to send Discord notifications: {}", e.getMessage());
        }
        
        log.info("User {} left team {}", username, teamName);
    }
    
    /**
     * Get pending invitations for a user
     */
    public List<TeamInvitationResponse> getPendingInvitations(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<TeamInvitation> invitations = invitationRepository.findByInvitedUserAndStatus(
            user, TeamInvitation.InvitationStatus.PENDING
        );
        
        return invitations.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get all teams
     */
    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }
    
    /**
     * Get team details with members
     */
    public Team getTeamWithMembers(Long teamId) {
        return teamRepository.findById(teamId)
            .orElseThrow(() -> new RuntimeException("Team not found"));
    }
    
    /**
     * Search users by username (for inviting)
     */
    public List<User> searchUsers(String searchTerm) {
        return userRepository.searchByUsername(searchTerm);
    }
    
    /**
     * Remove a member from the team (Captain or Manager only)
     */
    @Transactional
    public void removeMemberFromTeam(Long teamId, Long memberId, Long requesterId) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new RuntimeException("Team not found"));
        
        User requester = userRepository.findById(requesterId)
            .orElseThrow(() -> new RuntimeException("Requester not found"));
        
        User memberToRemove = userRepository.findById(memberId)
            .orElseThrow(() -> new RuntimeException("Member not found"));
        
        // Check if requester can manage the team
        if (!team.canManageTeam(requester)) {
            throw new RuntimeException("Only captain or manager can remove members");
        }
        
        // Cannot remove captain
        if (team.isCaptain(memberToRemove)) {
            throw new RuntimeException("Cannot remove team captain. Transfer captaincy first.");
        }
        
        // Cannot remove manager (must unassign first)
        if (team.getManager() != null && team.getManager().getId().equals(memberId)) {
            throw new RuntimeException("Cannot remove manager. Unassign manager role first.");
        }
        
        // Remove member from team
        memberToRemove.setTeam(null);
        memberToRemove.setTeamRole(null);
        userRepository.save(memberToRemove);
        
        // Remove from team's members list
        team.getMembers().remove(memberToRemove);
        teamRepository.save(team);
        
        // Notify remaining team members
        List<User> remainingMembers = userRepository.findByTeamId(team.getId());
        try {
            discordNotificationService.sendMemberRemovedNotification(
                remainingMembers, memberToRemove.getUsername(), 
                requester.getUsername(), team.getName());
        } catch (Exception e) {
            log.error("Failed to send Discord notifications: {}", e.getMessage());
        }
        
        log.info("Member {} removed from team {} by {}", 
            memberToRemove.getUsername(), team.getName(), requester.getUsername());
    }
    
    /**
     * Assign manager role to a team member (Captain only)
     */
    @Transactional
    public Team assignManager(Long teamId, Long memberId, Long captainId) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new RuntimeException("Team not found"));
        
        User captain = userRepository.findById(captainId)
            .orElseThrow(() -> new RuntimeException("Captain not found"));
        
        User newManager = userRepository.findById(memberId)
            .orElseThrow(() -> new RuntimeException("Member not found"));
        
        // Only captain can assign manager
        if (!team.isCaptain(captain)) {
            throw new RuntimeException("Only team captain can assign manager");
        }
        
        // Member must be in the team
        if (newManager.getTeam() == null || !newManager.getTeam().getId().equals(teamId)) {
            throw new RuntimeException("User is not a member of this team");
        }
        
        // Update old manager if exists and is not the captain
        if (team.getManager() != null && !team.getManager().getId().equals(team.getCaptain().getId())) {
            User oldManager = team.getManager();
            oldManager.setTeamRole(com.valorantarena.enums.TeamRole.PLAYER);
            userRepository.save(oldManager);
        }
        
        // Set new manager
        team.setManager(newManager);
        newManager.setTeamRole(com.valorantarena.enums.TeamRole.MANAGER);
        
        teamRepository.save(team);
        userRepository.save(newManager);
        
        // Notify all team members
        List<User> teamMembers = userRepository.findByTeamId(team.getId());
        try {
            discordNotificationService.sendManagerAssignedNotification(
                teamMembers, newManager.getUsername(), team.getName());
        } catch (Exception e) {
            log.error("Failed to send Discord notifications: {}", e.getMessage());
        }
        
        log.info("Manager role assigned to {} in team {}", newManager.getUsername(), team.getName());
        
        return team;
    }
    
    /**
     * Remove manager role (Captain only)
     */
    @Transactional
    public Team removeManager(Long teamId, Long captainId) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new RuntimeException("Team not found"));
        
        User captain = userRepository.findById(captainId)
            .orElseThrow(() -> new RuntimeException("Captain not found"));
        
        // Only captain can remove manager
        if (!team.isCaptain(captain)) {
            throw new RuntimeException("Only team captain can remove manager");
        }
        
        if (team.getManager() == null) {
            throw new RuntimeException("No manager assigned");
        }
        
        // Can't remove manager if they are the captain
        if (team.getManager().getId().equals(team.getCaptain().getId())) {
            throw new RuntimeException("Cannot remove captain from manager role. Captain is always a manager.");
        }
        
        // Demote manager to player
        User oldManager = team.getManager();
        oldManager.setTeamRole(com.valorantarena.enums.TeamRole.PLAYER);
        userRepository.save(oldManager);
        
        // Set captain back as manager
        team.setManager(team.getCaptain());
        teamRepository.save(team);
        
        // Notify all team members
        List<User> teamMembers = userRepository.findByTeamId(team.getId());
        try {
            discordNotificationService.sendManagerRemovedNotification(
                teamMembers, oldManager.getUsername(), team.getName());
        } catch (Exception e) {
            log.error("Failed to send Discord notifications: {}", e.getMessage());
        }
        
        log.info("Manager role removed from {} in team {}", oldManager.getUsername(), team.getName());
        
        return team;
    }
    
    /**
     * Transfer captaincy to another member (Captain only)
     */
    @Transactional
    public Team transferCaptaincy(Long teamId, Long newCaptainId, Long currentCaptainId) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new RuntimeException("Team not found"));
        
        User currentCaptain = userRepository.findById(currentCaptainId)
            .orElseThrow(() -> new RuntimeException("Current captain not found"));
        
        User newCaptain = userRepository.findById(newCaptainId)
            .orElseThrow(() -> new RuntimeException("New captain not found"));
        
        // Only current captain can transfer
        if (!team.isCaptain(currentCaptain)) {
            throw new RuntimeException("Only current captain can transfer captaincy");
        }
        
        // New captain must be in the team
        if (newCaptain.getTeam() == null || !newCaptain.getTeam().getId().equals(teamId)) {
            throw new RuntimeException("New captain must be a member of the team");
        }
        
        // Update roles
        currentCaptain.setTeamRole(com.valorantarena.enums.TeamRole.PLAYER);
        newCaptain.setTeamRole(com.valorantarena.enums.TeamRole.CAPTAIN);
        
        team.setCaptain(newCaptain);
        
        // If new captain was the manager, remove manager role
        if (team.getManager() != null && team.getManager().getId().equals(newCaptainId)) {
            team.setManager(null);
        }
        
        teamRepository.save(team);
        userRepository.save(currentCaptain);
        userRepository.save(newCaptain);
        
        // Notify all team members
        List<User> teamMembers = userRepository.findByTeamId(team.getId());
        try {
            discordNotificationService.sendCaptaincyTransferredNotification(
                teamMembers, newCaptain.getUsername(), 
                currentCaptain.getUsername(), team.getName());
        } catch (Exception e) {
            log.error("Failed to send Discord notifications: {}", e.getMessage());
        }
        
        log.info("Captaincy transferred from {} to {} in team {}", 
            currentCaptain.getUsername(), newCaptain.getUsername(), team.getName());
        
        return team;
    }
    
    /**
     * Update team logo (Captain or Manager only)
     */
    @Transactional
    public Team updateTeamLogo(Long teamId, String logoUrl, Long requesterId) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new RuntimeException("Team not found"));
        
        User requester = userRepository.findById(requesterId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if requester can manage the team
        if (!team.canManageTeam(requester)) {
            throw new RuntimeException("Only captain or manager can update team logo");
        }
        
        team.setLogo(logoUrl);
        teamRepository.save(team);
        
        // Notify all team members
        List<User> teamMembers = userRepository.findByTeamId(team.getId());
        try {
            String message = String.format(
                "🎨 **Team Update**\n\n" +
                "Team logo has been updated for **%s**!",
                team.getName()
            );
            discordNotificationService.notifyTeamMembers(teamMembers, message);
        } catch (Exception e) {
            log.error("Failed to send Discord notifications: {}", e.getMessage());
        }
        
        log.info("Team logo updated for {} by {}", team.getName(), requester.getUsername());
        
        return team;
    }
    
    /**
     * Change a member's role (Captain or Manager only)
     */
    @Transactional
    public void changeMemberRole(Long teamId, Long memberId, String newRole, Long requesterId) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new RuntimeException("Team not found"));
        
        User requester = userRepository.findById(requesterId)
            .orElseThrow(() -> new RuntimeException("Requester not found"));
        
        User member = userRepository.findById(memberId)
            .orElseThrow(() -> new RuntimeException("Member not found"));
        
        // Check if requester can manage the team
        if (!team.canManageTeam(requester)) {
            throw new RuntimeException("Only captain or manager can change member roles");
        }
        
        // Cannot change captain's role
        if (team.isCaptain(member)) {
            throw new RuntimeException("Cannot change captain's role. Transfer captaincy first.");
        }
        
        // Cannot change manager's role
        if (team.getManager() != null && team.getManager().getId().equals(memberId)) {
            throw new RuntimeException("Cannot change manager's role. Remove manager first.");
        }
        
        // Set new role
        com.valorantarena.enums.TeamRole teamRole = com.valorantarena.enums.TeamRole.valueOf(newRole);
        member.setTeamRole(teamRole);
        userRepository.save(member);
        
        log.info("Member {} role changed to {} in team {}", 
            member.getUsername(), newRole, team.getName());
    }
    
    /**
     * Get team's sent invitations (Captain/Manager only)
     */
    public List<TeamInvitationResponse> getTeamInvitations(Long teamId, Long requesterId) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new RuntimeException("Team not found"));
        
        User requester = userRepository.findById(requesterId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if requester can manage the team
        if (!team.canManageTeam(requester)) {
            throw new RuntimeException("Only captain or manager can view team invitations");
        }
        
        List<TeamInvitation> invitations = invitationRepository.findByTeamAndStatus(
            team, TeamInvitation.InvitationStatus.PENDING);
        
        return invitations.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Cancel team invitation (Captain/Manager only)
     */
    @Transactional
    public void cancelInvitation(Long invitationId, Long requesterId) {
        TeamInvitation invitation = invitationRepository.findById(invitationId)
            .orElseThrow(() -> new RuntimeException("Invitation not found"));
        
        User requester = userRepository.findById(requesterId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if requester can manage the team
        if (!invitation.getTeam().canManageTeam(requester)) {
            throw new RuntimeException("Only captain or manager can cancel invitations");
        }
        
        // Can only cancel pending invitations
        if (invitation.getStatus() != TeamInvitation.InvitationStatus.PENDING) {
            throw new RuntimeException("Can only cancel pending invitations");
        }
        
        invitation.setStatus(TeamInvitation.InvitationStatus.CANCELLED);
        invitationRepository.save(invitation);
        
        log.info("Invitation {} cancelled by {}", invitationId, requester.getUsername());
    }
    
    /**
     * Scheduled task to expire old invitations
     */
    @Scheduled(cron = "0 0 0 * * *") // Run daily at midnight
    @Transactional
    public void expireOldInvitations() {
        LocalDateTime expiryTime = LocalDateTime.now().minusDays(INVITATION_EXPIRY_DAYS);
        List<TeamInvitation> expiredInvitations = invitationRepository
            .findByStatusAndCreatedAtBefore(TeamInvitation.InvitationStatus.PENDING, expiryTime);
        
        if (!expiredInvitations.isEmpty()) {
            expiredInvitations.forEach(inv -> {
                inv.setStatus(TeamInvitation.InvitationStatus.EXPIRED);
                invitationRepository.save(inv);
            });
            log.info("Expired {} old team invitations", expiredInvitations.size());
        }
    }
    
    private TeamInvitationResponse mapToResponse(TeamInvitation invitation) {
        TeamInvitationResponse response = new TeamInvitationResponse();
        response.setId(invitation.getId());
        response.setTeamId(invitation.getTeam().getId());
        response.setTeamName(invitation.getTeam().getName());
        response.setTeamTag(invitation.getTeam().getTag());
        response.setTeamLogo(invitation.getTeam().getLogo());
        response.setInvitedById(invitation.getInvitedBy().getId());
        response.setInvitedByUsername(invitation.getInvitedBy().getUsername());
        
        // Add invited user details
        User invitedUser = invitation.getInvitedUser();
        response.setInvitedUserId(invitedUser.getId());
        response.setInvitedUserUsername(invitedUser.getUsername());
        response.setInvitedUserValorantId(invitedUser.getValorantId());
        response.setInvitedUserDiscordId(invitedUser.getDiscordId());
        response.setInvitedUserDiscordAvatar(invitedUser.getDiscordAvatar());
        
        response.setMessage(invitation.getMessage());
        response.setStatus(invitation.getStatus().name());
        response.setCreatedAt(invitation.getCreatedAt());
        response.setRespondedAt(invitation.getRespondedAt());
        return response;
    }
}
