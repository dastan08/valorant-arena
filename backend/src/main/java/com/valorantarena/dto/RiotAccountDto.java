package com.valorantarena.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiotAccountDto {
    private String puuid;
    private String gameName;
    private String tagLine;
    private String region; // The region where the account was found (americas, europe, asia, sea)
}
