package com.valorantarena.service;

import com.valorantarena.dto.DiscordTokenDto;
import com.valorantarena.dto.DiscordUserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class DiscordOAuthService {
    
    @Value("${discord.client-id}")
    private String clientId;
    
    @Value("${discord.client-secret}")
    private String clientSecret;
    
    @Value("${discord.redirect-uri}")
    private String redirectUri;
    
    private final WebClient webClient;
    
    public DiscordOAuthService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://discord.com/api/v10")
                .build();
    }
    
    /**
     * Exchange authorization code for access token
     */
    public DiscordTokenDto exchangeCode(String code) {
        log.info("Exchanging Discord authorization code for access token");
        log.debug("Client ID: {}", clientId);
        log.debug("Redirect URI: {}", redirectUri);
        log.debug("Code (first 10 chars): {}", code.substring(0, Math.min(10, code.length())));
        
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("grant_type", "authorization_code");
        formData.add("code", code);
        formData.add("redirect_uri", redirectUri);
        
        try {
            DiscordTokenDto token = webClient.post()
                    .uri("/oauth2/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                            .map(body -> {
                                log.error("Discord API error response: {}", body);
                                return new RuntimeException("Discord API error: " + body);
                            })
                    )
                    .bodyToMono(DiscordTokenDto.class)
                    .block();
            
            log.info("Successfully exchanged code for Discord access token");
            return token;
        } catch (Exception e) {
            log.error("Error exchanging Discord code. Full error: ", e);
            throw new RuntimeException("Failed to exchange Discord authorization code: " + e.getMessage());
        }
    }
    
    /**
     * Get Discord user information using access token
     */
    public DiscordUserDto getDiscordUser(String accessToken) {
        log.info("Fetching Discord user information");
        
        try {
            DiscordUserDto user = webClient.get()
                    .uri("/users/@me")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(DiscordUserDto.class)
                    .block();
            
            log.info("Successfully fetched Discord user: {}", user.getUsername());
            return user;
        } catch (Exception e) {
            log.error("Error fetching Discord user", e);
            throw new RuntimeException("Failed to fetch Discord user information: " + e.getMessage());
        }
    }
    
    /**
     * Generate Discord OAuth authorization URL
     */
    public String getAuthorizationUrl() {
        return String.format(
                "https://discord.com/api/oauth2/authorize?client_id=%s&redirect_uri=%s&response_type=code&scope=identify%%20email",
                clientId,
                redirectUri
        );
    }
}
