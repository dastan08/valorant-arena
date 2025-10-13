package com.valorantarena.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvitePlayerRequest {
    private Long userId; // User to invite
    private String message; // Optional invitation message
}
