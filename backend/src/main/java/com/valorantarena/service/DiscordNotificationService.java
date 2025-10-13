package com.valorantarena.service;

import com.valorantarena.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DiscordNotificationService {

    @Value("${discord.bot.token:}")
    private String botToken;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String DISCORD_API_BASE = "https://discord.com/api/v10";

    /**
     * Send a DM to a user via Discord
     */
    public boolean sendDirectMessage(String discordUserId, String message) {
        if (botToken == null || botToken.isEmpty()) {
            log.warn("Discord bot token not configured. Skipping notification.");
            return false;
        }

        try {
            // Step 1: Create DM channel
            String dmChannelId = createDMChannel(discordUserId);
            
            if (dmChannelId == null) {
                log.error("Failed to create DM channel for user: {}", discordUserId);
                return false;
            }

            // Step 2: Send message to DM channel
            boolean sent = sendMessageToChannel(dmChannelId, message);
            
            if (sent) {
                log.info("✅ Discord notification sent successfully to user: {}", discordUserId);
                return true;
            } else {
                log.warn("❌ Failed to send Discord notification to user: {}", discordUserId);
                return false;
            }
        } catch (Exception e) {
            log.error("❌ Error sending Discord notification: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Send team invitation notification
     */
    public void sendTeamInvitationNotification(User invitedUser, String teamName, String inviterName, String message) {
        if (invitedUser.getDiscordId() == null || invitedUser.getDiscordId().isEmpty()) {
            log.debug("User {} does not have Discord linked. Skipping notification.", invitedUser.getUsername());
            return;
        }

        String notificationMessage = buildInvitationMessage(teamName, inviterName, message);
        sendDirectMessage(invitedUser.getDiscordId(), notificationMessage);
    }

    /**
     * Send invitation accepted notification
     */
    public void sendInvitationAcceptedNotification(User teamCaptain, String playerName, String teamName) {
        if (teamCaptain.getDiscordId() == null || teamCaptain.getDiscordId().isEmpty()) {
            return;
        }

        String notificationMessage = String.format(
            "🎉 **Team Update**\n\n" +
            "**%s** has accepted your invitation and joined **%s**!",
            playerName, teamName
        );
        
        sendDirectMessage(teamCaptain.getDiscordId(), notificationMessage);
    }

    /**
     * Send invitation declined notification
     */
    public void sendInvitationDeclinedNotification(User teamCaptain, String playerName, String teamName) {
        if (teamCaptain.getDiscordId() == null || teamCaptain.getDiscordId().isEmpty()) {
            return;
        }

        String notificationMessage = String.format(
            "❌ **Team Update**\n\n" +
            "**%s** has declined your invitation to join **%s**.",
            playerName, teamName
        );
        
        sendDirectMessage(teamCaptain.getDiscordId(), notificationMessage);
    }

    /**
     * Notify all team members about an activity
     */
    public void notifyTeamMembers(List<User> members, String message) {
        for (User member : members) {
            if (member.getDiscordId() != null && !member.getDiscordId().isEmpty()) {
                sendDirectMessage(member.getDiscordId(), message);
            }
        }
    }

    /**
     * Member left team notification
     */
    public void sendMemberLeftNotification(List<User> teamMembers, String playerName, String teamName) {
        String message = String.format(
            "👋 **Team Update**\n\n" +
            "**%s** has left **%s**.",
            playerName, teamName
        );
        notifyTeamMembers(teamMembers, message);
    }

    /**
     * Member removed notification
     */
    public void sendMemberRemovedNotification(List<User> teamMembers, String removedPlayer, String removedBy, String teamName) {
        String message = String.format(
            "🚫 **Team Update**\n\n" +
            "**%s** has been removed from **%s** by **%s**.",
            removedPlayer, teamName, removedBy
        );
        notifyTeamMembers(teamMembers, message);
    }

    /**
     * Captaincy transferred notification
     */
    public void sendCaptaincyTransferredNotification(List<User> teamMembers, String newCaptain, String oldCaptain, String teamName) {
        String message = String.format(
            "👑 **Team Update**\n\n" +
            "**%s** has transferred captaincy of **%s** to **%s**.",
            oldCaptain, teamName, newCaptain
        );
        notifyTeamMembers(teamMembers, message);
    }

    /**
     * Manager assigned notification
     */
    public void sendManagerAssignedNotification(List<User> teamMembers, String managerName, String teamName) {
        String message = String.format(
            "⭐ **Team Update**\n\n" +
            "**%s** has been assigned as manager of **%s**.",
            managerName, teamName
        );
        notifyTeamMembers(teamMembers, message);
    }

    /**
     * Manager removed notification
     */
    public void sendManagerRemovedNotification(List<User> teamMembers, String managerName, String teamName) {
        String message = String.format(
            "📋 **Team Update**\n\n" +
            "**%s** is no longer the manager of **%s**.",
            managerName, teamName
        );
        notifyTeamMembers(teamMembers, message);
    }

    /**
     * New member joined notification
     */
    public void sendNewMemberJoinedNotification(List<User> teamMembers, String playerName, String teamName) {
        String message = String.format(
            "🎉 **Team Update**\n\n" +
            "**%s** has joined **%s**! Welcome to the team!",
            playerName, teamName
        );
        notifyTeamMembers(teamMembers, message);
    }

    private String buildInvitationMessage(String teamName, String inviterName, String customMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append("🎮 **Team Invitation**\n\n");
        sb.append(String.format("You've been invited to join **%s**!\n\n", teamName));
        sb.append(String.format("**Invited by:** %s\n", inviterName));
        
        if (customMessage != null && !customMessage.isEmpty()) {
            sb.append(String.format("**Message:** \"%s\"\n\n", customMessage));
        }
        
        sb.append("Visit Valorant Arena to accept or decline this invitation.");
        
        return sb.toString();
    }

    private String createDMChannel(String userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bot " + botToken);

            Map<String, String> body = new HashMap<>();
            body.put("recipient_id", userId);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                DISCORD_API_BASE + "/users/@me/channels",
                request,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("id");
            }
        } catch (Exception e) {
            log.error("Error creating DM channel: {}", e.getMessage());
        }
        
        return null;
    }

    private boolean sendMessageToChannel(String channelId, String message) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bot " + botToken);

            Map<String, String> body = new HashMap<>();
            body.put("content", message);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                DISCORD_API_BASE + "/channels/" + channelId + "/messages",
                request,
                Map.class
            );
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg.contains("50007")) {
                log.error("❌ Cannot send DM: User has DMs disabled or doesn't share a server with the bot");
            } else if (errorMsg.contains("50001")) {
                log.error("❌ Cannot send DM: Missing access to user");
            } else {
                log.error("❌ Error sending message to channel: {}", errorMsg);
            }
            return false;
        }
    }
}
