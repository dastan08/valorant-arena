package com.valorantarena.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class DisputeDTO {
    
    @NotNull(message = "Match ID is required")
    private Long matchId;
    
    @NotBlank(message = "Dispute type is required")
    private String disputeType;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    private Long reportedAgainstUserId;
    
    private List<String> evidenceUrls;
}
