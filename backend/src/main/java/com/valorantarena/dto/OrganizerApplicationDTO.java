package com.valorantarena.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrganizerApplicationDTO {
    
    @NotBlank(message = "Organization name is required")
    @Size(min = 3, max = 255, message = "Organization name must be between 3 and 255 characters")
    private String organizationName;
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
    
    @Size(max = 500, message = "Website URL cannot exceed 500 characters")
    private String websiteUrl;
    
    @Size(max = 255, message = "Discord server cannot exceed 255 characters")
    private String discordServer;
}
