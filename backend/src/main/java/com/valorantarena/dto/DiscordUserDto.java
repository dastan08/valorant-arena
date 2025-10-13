package com.valorantarena.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscordUserDto {
    
    private String id;
    
    private String username;
    
    private String discriminator;
    
    private String avatar;
    
    private String email;
    
    @JsonProperty("verified")
    private Boolean verified;
    
    @JsonProperty("global_name")
    private String globalName;
}
