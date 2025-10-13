package com.valorantarena.controller;

import com.valorantarena.dto.ApiResponse;
import com.valorantarena.dto.CreateTeamRequest;
import com.valorantarena.dto.InvitePlayerRequest;
import com.valorantarena.dto.TeamInvitationResponse;
import com.valorantarena.entity.Team;
import com.valorantarena.entity.User;
import com.valorantarena.security.UserDetailsImpl;
import com.valorantarena.service.TeamManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class TeamManagementController {
    
    private final TeamManagementService teamManagementService;
    
    /**
     * Create a new team
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Team>> createTeam(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody CreateTeamRequest request) {
        try {
            Team team = teamManagementService.createTeam(userDetails.getId(), request);
            return ResponseEntity.ok(ApiResponse.success("Team created successfully", team));
        } catch (Exception e) {
            log.error("Error creating team", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Invite a player to the team
     */
    @PostMapping("/{teamId}/invite")
    public ResponseEntity<ApiResponse<Void>> invitePlayer(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody InvitePlayerRequest request) {
        try {
            teamManagementService.invitePlayer(teamId, userDetails.getId(), request);
            return ResponseEntity.ok(ApiResponse.success("Invitation sent successfully", null));
        } catch (Exception e) {
            log.error("Error sending invitation", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Accept team invitation
     */
    @PostMapping("/invitations/{invitationId}/accept")
    public ResponseEntity<ApiResponse<Team>> acceptInvitation(
            @PathVariable Long invitationId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Team team = teamManagementService.acceptInvitation(invitationId, userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success("Invitation accepted. Welcome to the team!", team));
        } catch (Exception e) {
            log.error("Error accepting invitation", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Decline team invitation
     */
    @PostMapping("/invitations/{invitationId}/decline")
    public ResponseEntity<ApiResponse<Void>> declineInvitation(
            @PathVariable Long invitationId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            teamManagementService.declineInvitation(invitationId, userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success("Invitation declined", null));
        } catch (Exception e) {
            log.error("Error declining invitation", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Get pending invitations for current user
     */
    @GetMapping("/invitations/pending")
    public ResponseEntity<ApiResponse<List<TeamInvitationResponse>>> getPendingInvitations(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            List<TeamInvitationResponse> invitations = 
                teamManagementService.getPendingInvitations(userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success("Pending invitations retrieved", invitations));
        } catch (Exception e) {
            log.error("Error getting pending invitations", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Get team's sent invitations (Captain/Manager only)
     */
    @GetMapping("/{teamId}/invitations")
    public ResponseEntity<ApiResponse<List<TeamInvitationResponse>>> getTeamInvitations(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            List<TeamInvitationResponse> invitations = 
                teamManagementService.getTeamInvitations(teamId, userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success("Team invitations retrieved", invitations));
        } catch (Exception e) {
            log.error("Error getting team invitations", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Cancel team invitation (Captain/Manager only)
     */
    @DeleteMapping("/invitations/{invitationId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelInvitation(
            @PathVariable Long invitationId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            teamManagementService.cancelInvitation(invitationId, userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success("Invitation cancelled", null));
        } catch (Exception e) {
            log.error("Error cancelling invitation", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Leave team
     */
    @PostMapping("/leave")
    public ResponseEntity<ApiResponse<Void>> leaveTeam(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            teamManagementService.leaveTeam(userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success("You have left the team", null));
        } catch (Exception e) {
            log.error("Error leaving team", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Search users for inviting
     */
    @GetMapping("/search-users")
    public ResponseEntity<ApiResponse<List<User>>> searchUsers(
            @RequestParam String query) {
        try {
            List<User> users = teamManagementService.searchUsers(query);
            return ResponseEntity.ok(ApiResponse.success("Users found", users));
        } catch (Exception e) {
            log.error("Error searching users", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Get all teams
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Team>>> getAllTeams() {
        try {
            List<Team> teams = teamManagementService.getAllTeams();
            return ResponseEntity.ok(ApiResponse.success("Teams retrieved", teams));
        } catch (Exception e) {
            log.error("Error getting teams", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Get team details with members
     */
    @GetMapping("/{teamId}")
    public ResponseEntity<ApiResponse<Team>> getTeam(@PathVariable Long teamId) {
        try {
            Team team = teamManagementService.getTeamWithMembers(teamId);
            return ResponseEntity.ok(ApiResponse.success("Team retrieved", team));
        } catch (Exception e) {
            log.error("Error getting team", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Remove a member from the team
     */
    @DeleteMapping("/{teamId}/members/{memberId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long teamId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            teamManagementService.removeMemberFromTeam(teamId, memberId, userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success("Member removed successfully", null));
        } catch (Exception e) {
            log.error("Error removing member", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Assign manager role to a team member
     */
    @PostMapping("/{teamId}/assign-manager/{memberId}")
    public ResponseEntity<ApiResponse<Team>> assignManager(
            @PathVariable Long teamId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Team team = teamManagementService.assignManager(teamId, memberId, userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success("Manager assigned successfully", team));
        } catch (Exception e) {
            log.error("Error assigning manager", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Remove manager role
     */
    @DeleteMapping("/{teamId}/remove-manager")
    public ResponseEntity<ApiResponse<Team>> removeManager(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Team team = teamManagementService.removeManager(teamId, userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success("Manager removed successfully", team));
        } catch (Exception e) {
            log.error("Error removing manager", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Transfer captaincy to another member
     */
    @PostMapping("/{teamId}/transfer-captaincy/{newCaptainId}")
    public ResponseEntity<ApiResponse<Team>> transferCaptaincy(
            @PathVariable Long teamId,
            @PathVariable Long newCaptainId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Team team = teamManagementService.transferCaptaincy(teamId, newCaptainId, userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success("Captaincy transferred successfully", team));
        } catch (Exception e) {
            log.error("Error transferring captaincy", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Update team logo
     */
    @PutMapping("/{teamId}/logo")
    public ResponseEntity<ApiResponse<Team>> updateTeamLogo(
            @PathVariable Long teamId,
            @RequestParam String logoUrl,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Team team = teamManagementService.updateTeamLogo(teamId, logoUrl, userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success("Team logo updated successfully", team));
        } catch (Exception e) {
            log.error("Error updating team logo", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Change a member's role
     */
    @PutMapping("/{teamId}/members/{memberId}/role")
    public ResponseEntity<ApiResponse<Void>> changeMemberRole(
            @PathVariable Long teamId,
            @PathVariable Long memberId,
            @RequestParam String role,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            teamManagementService.changeMemberRole(teamId, memberId, role, userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success("Member role updated successfully", null));
        } catch (Exception e) {
            log.error("Error changing member role", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}
