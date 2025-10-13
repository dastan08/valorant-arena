package com.valorantarena.service;

import com.valorantarena.dto.LoginRequest;
import com.valorantarena.dto.RegisterRequest;
import com.valorantarena.dto.AuthResponse;
import com.valorantarena.dto.RiotAccountDto;
import com.valorantarena.dto.DiscordUserDto;
import com.valorantarena.entity.User;
import com.valorantarena.enums.UserRole;
import com.valorantarena.repository.UserRepository;
import com.valorantarena.security.JwtUtils;
import com.valorantarena.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RiotApiService riotApiService;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }
        
        if (userRepository.existsByValorantId(request.getValorantId())) {
            throw new RuntimeException("Error: Valorant ID is already registered!");
        }
        
        // Verify Riot account (optional - can be enabled/disabled)
        // Parse Valorant ID (format: GameName#TAG)
        String[] riotIdParts = request.getValorantId().split("#");
        if (riotIdParts.length == 2) {
            try {
                log.info("Verifying Riot account for: {}", request.getValorantId());
                RiotAccountDto riotAccount = riotApiService.verifyAccount(riotIdParts[0], riotIdParts[1]);
                
                if (riotAccount == null) {
                    log.warn("Riot account not found: {}", request.getValorantId());
                    // Uncomment to enforce Riot account verification
                    throw new RuntimeException("Error: Riot account not found. Please check your Valorant ID.");
                } else {
                    log.info("Riot account verified: PUUID={}", riotAccount.getPuuid());
                }
            } catch (Exception e) {
                log.error("Error verifying Riot account", e);
                // Uncomment to enforce Riot account verification
                throw new RuntimeException("Error: Could not verify Riot account. " + e.getMessage());
            }
        }
        
        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setValorantId(request.getValorantId());
        user.setRank(request.getRank());
        
        userRepository.save(user);
        
        // Generate JWT token
        String jwt = jwtUtils.generateTokenFromUsername(user.getUsername());
        
        return new AuthResponse(jwt, user.getId(), user.getUsername(), 
                user.getEmail(), user.getRole().name());
    }
    
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        getUsernameFromEmail(request.getEmail()),
                        request.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        return new AuthResponse(jwt, userDetails.getId(), userDetails.getUsername(),
                userDetails.getEmail(), userDetails.getAuthorities().iterator().next().getAuthority());
    }
    
    private String getUsernameFromEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return user.getUsername();
    }
    
    @Transactional
    public AuthResponse loginWithRiot(String valorantId, com.valorantarena.enums.ValorantRank rank) {
        // Parse Valorant ID
        String[] riotIdParts = valorantId.split("#");
        if (riotIdParts.length != 2) {
            throw new RuntimeException("Invalid Valorant ID format. Use: GameName#TAG");
        }
        
        // Verify Riot account exists
        log.info("Verifying Riot account for login: {}", valorantId);
        RiotAccountDto riotAccount = riotApiService.verifyAccount(riotIdParts[0], riotIdParts[1]);
        
        if (riotAccount == null) {
            throw new RuntimeException("Riot account not found. Please check your Valorant ID.");
        }
        
        log.info("Riot account verified: PUUID={}", riotAccount.getPuuid());
        
        // Try to fetch player's current rank from Valorant API (optional)
        com.valorantarena.enums.ValorantRank fetchedRank = null;
        try {
            fetchedRank = riotApiService.getPlayerRank(riotIdParts[0], riotIdParts[1]);
            if (fetchedRank != null && fetchedRank != com.valorantarena.enums.ValorantRank.IRON) {
                log.info("Fetched rank from API: {}", fetchedRank);
            } else {
                log.info("Could not fetch rank from API, will use provided or default rank");
                fetchedRank = null;
            }
        } catch (Exception e) {
            log.info("Could not fetch rank from API, will use provided or default rank");
            fetchedRank = null;
        }
        
        // Check if user already exists with this Valorant ID
        User user = userRepository.findByValorantId(valorantId).orElse(null);
        
        if (user == null) {
            // Create new user automatically
            log.info("Creating new user for Riot account: {}", valorantId);
            user = new User();
            user.setUsername(riotAccount.getGameName() + "_" + riotAccount.getTagLine());
            
            // Generate a simple, valid email using username and timestamp
            String emailUsername = riotAccount.getGameName().toLowerCase().replaceAll("[^a-z0-9]", "");
            String generatedEmail = emailUsername + "." + riotAccount.getTagLine().toLowerCase() + "@valorant-arena.com";
            log.info("Generated email: {}", generatedEmail);
            
            // Check if email already exists, if so, add timestamp
            if (userRepository.existsByEmail(generatedEmail)) {
                generatedEmail = emailUsername + "." + riotAccount.getTagLine().toLowerCase() + "." + System.currentTimeMillis() + "@valorant-arena.com";
                log.info("Email collision detected, using: {}", generatedEmail);
            }
            
            user.setEmail(generatedEmail); // Temporary email with valid format
            
            user.setPassword(passwordEncoder.encode(riotAccount.getPuuid())); // Use PUUID as password
            user.setValorantId(valorantId);
            user.setRegion(riotAccount.getRegion()); // Store the account region
            
            // Use fetched rank if available, otherwise use provided rank or default
            if (fetchedRank != null) {
                user.setRank(fetchedRank);
            } else {
                user.setRank(rank != null ? rank : com.valorantarena.enums.ValorantRank.IRON);
            }
            
            user.setIsVerified(true);
            
            // Check if username already exists, if so, add random suffix
            String baseUsername = user.getUsername();
            int suffix = 1;
            while (userRepository.existsByUsername(user.getUsername())) {
                user.setUsername(baseUsername + suffix);
                suffix++;
            }
            
            userRepository.save(user);
            log.info("New user created: {}", user.getUsername());
        } else {
            log.info("Existing user found: {}", user.getUsername());
        }
        
        // Generate JWT token
        String jwt = jwtUtils.generateTokenFromUsername(user.getUsername());
        
        return new AuthResponse(jwt, user.getId(), user.getUsername(), 
                user.getEmail(), user.getRole().name());
    }
    
    /**
     * Login or register user with Discord OAuth
     */
    @Transactional
    public AuthResponse loginWithDiscord(DiscordUserDto discordUser) {
        log.info("Processing Discord login for user: {}", discordUser.getUsername());
        
        // Check if user already exists with this Discord ID
        User user = userRepository.findByDiscordId(discordUser.getId()).orElse(null);
        
        if (user == null) {
            // Check if email already exists (link to existing account)
            if (discordUser.getEmail() != null) {
                User existingEmailUser = userRepository.findByEmail(discordUser.getEmail()).orElse(null);
                if (existingEmailUser != null) {
                    // Link Discord to existing account
                    existingEmailUser.setDiscordId(discordUser.getId());
                    existingEmailUser.setDiscordUsername(discordUser.getUsername());
                    existingEmailUser.setDiscordAvatar(discordUser.getAvatar());
                    userRepository.save(existingEmailUser);
                    log.info("Linked Discord to existing account: {}", existingEmailUser.getUsername());
                    user = existingEmailUser;
                }
            }
            
            if (user == null) {
                // Create new user
                log.info("Creating new Discord user: {}", discordUser.getUsername());
                user = new User();
                user.setUsername(discordUser.getUsername());
                user.setEmail(discordUser.getEmail() != null ? discordUser.getEmail() : discordUser.getId() + "@discord.local");
                user.setPassword(passwordEncoder.encode(discordUser.getId()));
                user.setDiscordId(discordUser.getId());
                user.setDiscordUsername(discordUser.getUsername());
                user.setDiscordAvatar(discordUser.getAvatar());
                user.setValorantId("NOT_LINKED_" + discordUser.getId());
                user.setRank(com.valorantarena.enums.ValorantRank.IRON);
                user.setIsVerified(discordUser.getVerified() != null ? discordUser.getVerified() : false);
                user.setRole(UserRole.USER);
                
                // Handle username collision
                String baseUsername = user.getUsername();
                int suffix = 1;
                while (userRepository.existsByUsername(user.getUsername())) {
                    user.setUsername(baseUsername + suffix);
                    suffix++;
                }
                
                userRepository.save(user);
                log.info("New Discord user created: {}", user.getUsername());
            }
        } else {
            // Update existing user
            user.setDiscordUsername(discordUser.getUsername());
            user.setDiscordAvatar(discordUser.getAvatar());
            if (discordUser.getEmail() != null && !discordUser.getEmail().endsWith("@discord.local")) {
                user.setEmail(discordUser.getEmail());
            }
            userRepository.save(user);
            log.info("Discord user updated: {}", user.getUsername());
        }
        
        // Generate JWT token
        String jwt = jwtUtils.generateTokenFromUsername(user.getUsername());
        
        return new AuthResponse(jwt, user.getId(), user.getUsername(), 
                user.getEmail(), user.getRole().name());
    }
}

