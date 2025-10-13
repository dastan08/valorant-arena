package com.valorantarena.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamInvitationResponse {
    private Long id;
    private Long teamId;
    private String teamName;
    private String teamTag;
    private String teamLogo;
    private Long invitedById;
    private String invitedByUsername;
    private Long invitedUserId;
    private String invitedUserUsername;
    private String invitedUserValorantId;
    private String invitedUserDiscordId;
    private String invitedUserDiscordAvatar;
    private String message;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
}
