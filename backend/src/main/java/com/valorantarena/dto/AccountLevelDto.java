package com.valorantarena.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountLevelDto {
    
    private String status;
    
    @JsonProperty("data")
    private AccountData data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountData {
        private String puuid;
        private String region;
        private Integer accountLevel;
        private String name;
        private String tag;
        private CardInfo card;
        private String lastUpdate;
        
        @JsonProperty("last_update_raw")
        private Long lastUpdateRaw;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardInfo {
        private String small;
        private String large;
        private String wide;
        private String id;
    }
}
