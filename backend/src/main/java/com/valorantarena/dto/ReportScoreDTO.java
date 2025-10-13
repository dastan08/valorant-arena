package com.valorantarena.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReportScoreDTO {
    
    @NotNull(message = "Participant 1 score is required")
    @Min(value = 0, message = "Score must be non-negative")
    private Integer participant1Score;
    
    @NotNull(message = "Participant 2 score is required")
    @Min(value = 0, message = "Score must be non-negative")
    private Integer participant2Score;
    
    private List<String> screenshotUrls;
    
    private String matchReplayUrl;
    
    private String notes;
}
