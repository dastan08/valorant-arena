package com.valorantarena.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkRiotAccountRequest {
    
    @NotBlank(message = "Valorant ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9 ]+#[a-zA-Z0-9]+$", 
             message = "Valorant ID must be in format: GameName#TAG")
    private String valorantId;
}
