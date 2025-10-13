package com.valorantarena.service;

import com.valorantarena.dto.LinkRiotAccountRequest;
import com.valorantarena.dto.RiotAccountDto;
import com.valorantarena.entity.User;
import com.valorantarena.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final RiotApiService riotApiService;
    
    @Transactional
    public User linkRiotAccount(Long userId, LinkRiotAccountRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if Valorant ID is already taken by another user
        if (userRepository.existsByValorantId(request.getValorantId())) {
            User existingUser = userRepository.findByValorantId(request.getValorantId())
                    .orElse(null);
            if (existingUser != null && !existingUser.getId().equals(userId)) {
                throw new RuntimeException("This Valorant ID is already linked to another account");
            }
        }
        
        // Parse and verify Riot account
        String[] riotIdParts = request.getValorantId().split("#");
        if (riotIdParts.length != 2) {
            throw new RuntimeException("Invalid Valorant ID format. Use: GameName#TAG");
        }
        
        try {
            log.info("Verifying Riot account for user {}: {}", userId, request.getValorantId());
            RiotAccountDto riotAccount = riotApiService.verifyAccount(riotIdParts[0], riotIdParts[1]);
            
            if (riotAccount == null) {
                throw new RuntimeException("Riot account not found. Please check your Valorant ID.");
            }
            
            log.info("Riot account verified: PUUID={}", riotAccount.getPuuid());
            
            // Update user's Valorant ID
            user.setValorantId(request.getValorantId());
            user.setIsVerified(true);
            
            return userRepository.save(user);
            
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error verifying Riot account", e);
            throw new RuntimeException("Failed to verify Riot account: " + e.getMessage());
        }
    }
    
    @Transactional
    public User unlinkRiotAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Set to NOT_LINKED instead of null (to satisfy NOT NULL constraint)
        if (user.getDiscordId() != null) {
            user.setValorantId("NOT_LINKED_" + user.getDiscordId());
        } else {
            user.setValorantId("NOT_LINKED_" + userId);
        }
        user.setIsVerified(false);
        
        log.info("Unlinked Riot account for user {}", userId);
        
        return userRepository.save(user);
    }
    
    public User getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // Set profile picture URL
        user.setProfilePictureUrl(user.getProfilePictureUrl());
        return user;
    }
    
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
