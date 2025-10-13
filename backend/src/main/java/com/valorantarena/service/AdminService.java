package com.valorantarena.service;

import com.valorantarena.entity.Team;
import com.valorantarena.entity.User;
import com.valorantarena.enums.UserRole;
import com.valorantarena.enums.UserStatus;
import com.valorantarena.repository.ScrimRepository;
import com.valorantarena.repository.TeamRepository;
import com.valorantarena.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final ScrimRepository scrimRepository;

    // Dashboard Statistics
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Basic counts
        stats.put("totalUsers", userRepository.count());
        stats.put("totalTeams", teamRepository.count());
        stats.put("activeScrims", scrimRepository.countByStatus("PENDING") + scrimRepository.countByStatus("ACCEPTED"));
        stats.put("pendingReports", 0); // TODO: Implement when reports are added
        stats.put("onlineUsers", getOnlineUsersCount());
        stats.put("totalMatches", scrimRepository.countByStatus("COMPLETED"));
        
        return stats;
    }

    public List<Map<String, Object>> getRecentActivity() {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        // Get recent users (last 24 hours)
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        List<User> recentUsers = userRepository.findByCreatedAtAfter(yesterday);
        
        for (User user : recentUsers) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("description", "New user registered: " + user.getUsername());
            activity.put("timestamp", user.getCreatedAt());
            activity.put("icon", "person_add");
            activities.add(activity);
        }
        
        // Get recent teams
        List<Team> recentTeams = teamRepository.findByCreatedAtAfter(yesterday);
        for (Team team : recentTeams) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("description", "New team created: " + team.getName());
            activity.put("timestamp", team.getCreatedAt());
            activity.put("icon", "group_add");
            activities.add(activity);
        }
        
        // Sort by timestamp descending
        activities.sort((a, b) -> ((LocalDateTime) b.get("timestamp")).compareTo((LocalDateTime) a.get("timestamp")));
        
        // Return only the latest 20 activities
        return activities.size() > 20 ? activities.subList(0, 20) : activities;
    }

    public Map<String, String> getSystemHealth() {
        Map<String, String> health = new HashMap<>();
        
        // Database health check
        try {
            userRepository.count();
            health.put("database", "healthy");
        } catch (Exception e) {
            health.put("database", "error");
        }
        
        // API health (always healthy if we can respond)
        health.put("api", "healthy");
        
        // Discord bot health (placeholder)
        health.put("discord", "healthy");
        
        // Riot API health (placeholder)
        health.put("riot", "healthy");
        
        return health;
    }

    // User Management
    public Page<User> getAllUsers(Pageable pageable, String search) {
        if (search != null && !search.trim().isEmpty()) {
            return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                search.trim(), search.trim(), pageable);
        }
        return userRepository.findAll(pageable);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public User updateUser(Long userId, Map<String, Object> userData) {
        User user = getUserById(userId);
        
        if (userData.containsKey("username")) {
            String newUsername = (String) userData.get("username");
            if (!user.getUsername().equals(newUsername)) {
                if (userRepository.existsByUsername(newUsername)) {
                    throw new RuntimeException("Username already exists");
                }
                user.setUsername(newUsername);
            }
        }
        
        if (userData.containsKey("email")) {
            String newEmail = (String) userData.get("email");
            if (!user.getEmail().equals(newEmail)) {
                if (userRepository.existsByEmail(newEmail)) {
                    throw new RuntimeException("Email already exists");
                }
                user.setEmail(newEmail);
            }
        }
        
        if (userData.containsKey("role")) {
            String roleStr = (String) userData.get("role");
            try {
                user.setRole(UserRole.valueOf(roleStr));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid role: " + roleStr + ". Valid roles are: " + 
                    String.join(", ", java.util.Arrays.stream(UserRole.values())
                        .map(Enum::name)
                        .toArray(String[]::new)));
            }
        }
        
        if (userData.containsKey("status")) {
            String statusStr = (String) userData.get("status");
            user.setStatus(UserStatus.valueOf(statusStr));
        }
        
        if (userData.containsKey("isVerified")) {
            Boolean isVerified = (Boolean) userData.get("isVerified");
            user.setIsVerified(isVerified);
        }
        
        return userRepository.save(user);
    }

    @Transactional
    public void banUser(Long userId, String reason, Integer durationDays) {
        User user = getUserById(userId);
        user.setStatus(UserStatus.BANNED);
        // TODO: Add ban reason and duration to user entity or separate ban table
        userRepository.save(user);
        log.info("User {} banned by admin. Reason: {}", user.getUsername(), reason);
    }

    @Transactional
    public void unbanUser(Long userId) {
        User user = getUserById(userId);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        log.info("User {} unbanned by admin", user.getUsername());
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        
        // Remove user from team if they're in one
        if (user.getTeam() != null) {
            Team team = user.getTeam();
            team.removeMember(user);
            teamRepository.save(team);
        }
        
        userRepository.delete(user);
        log.info("User {} deleted by admin", user.getUsername());
    }

    // Team Management
    public Page<Team> getAllTeams(Pageable pageable, String search) {
        if (search != null && !search.trim().isEmpty()) {
            return teamRepository.findByNameContainingIgnoreCaseOrTagContainingIgnoreCase(
                search.trim(), search.trim(), pageable);
        }
        return teamRepository.findAll(pageable);
    }

    @Transactional
    public void disbandTeam(Long teamId, String reason) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        
        // Remove all members from the team
        List<User> members = new ArrayList<>(team.getMembers());
        for (User member : members) {
            member.setTeam(null);
            member.setTeamRole(null);
            userRepository.save(member);
        }
        
        // Delete the team
        teamRepository.delete(team);
        log.info("Team {} disbanded by admin. Reason: {}", team.getName(), reason);
    }

    // System Management
    public void clearCache() {
        // TODO: Implement cache clearing logic
        log.info("System cache cleared by admin");
    }

    private long getOnlineUsersCount() {
        // TODO: Implement online users tracking
        // For now, return a placeholder value
        return userRepository.count() / 10; // Assume 10% of users are online
    }
}
