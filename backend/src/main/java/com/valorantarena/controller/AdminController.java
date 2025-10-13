package com.valorantarena.controller;

import com.valorantarena.dto.ApiResponse;
import com.valorantarena.service.AdminService;
import com.valorantarena.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // Dashboard Statistics
    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Map<String, Object> stats = adminService.getDashboardStats();
            return ResponseEntity.ok(ApiResponse.success("Dashboard stats retrieved successfully", stats));
        } catch (Exception e) {
            log.error("Error retrieving dashboard stats", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve dashboard stats: " + e.getMessage()));
        }
    }

    @GetMapping("/dashboard/activity")
    public ResponseEntity<ApiResponse<Object>> getRecentActivity(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Object activity = adminService.getRecentActivity();
            return ResponseEntity.ok(ApiResponse.success("Recent activity retrieved successfully", activity));
        } catch (Exception e) {
            log.error("Error retrieving recent activity", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve recent activity: " + e.getMessage()));
        }
    }

    @GetMapping("/dashboard/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> getSystemHealth(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Map<String, String> health = adminService.getSystemHealth();
            return ResponseEntity.ok(ApiResponse.success("System health retrieved successfully", health));
        } catch (Exception e) {
            log.error("Error retrieving system health", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve system health: " + e.getMessage()));
        }
    }

    // User Management
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Object>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Object users = adminService.getAllUsers(pageable, search);
            return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
        } catch (Exception e) {
            log.error("Error retrieving users", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve users: " + e.getMessage()));
        }
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Object>> getUserById(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Object user = adminService.getUserById(userId);
            return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
        } catch (Exception e) {
            log.error("Error retrieving user", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve user: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Object>> updateUser(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> userData,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Object user = adminService.updateUser(userId, userData);
            return ResponseEntity.ok(ApiResponse.success("User updated successfully", user));
        } catch (Exception e) {
            log.error("Error updating user", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update user: " + e.getMessage()));
        }
    }

    @PostMapping("/users/{userId}/ban")
    public ResponseEntity<ApiResponse<String>> banUser(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> banData,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            String reason = (String) banData.get("reason");
            Integer duration = (Integer) banData.get("duration");
            adminService.banUser(userId, reason, duration);
            return ResponseEntity.ok(ApiResponse.success("User banned successfully", "User has been banned"));
        } catch (Exception e) {
            log.error("Error banning user", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to ban user: " + e.getMessage()));
        }
    }

    @PostMapping("/users/{userId}/unban")
    public ResponseEntity<ApiResponse<String>> unbanUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            adminService.unbanUser(userId);
            return ResponseEntity.ok(ApiResponse.success("User unbanned successfully", "User has been unbanned"));
        } catch (Exception e) {
            log.error("Error unbanning user", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to unban user: " + e.getMessage()));
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            adminService.deleteUser(userId);
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully", "User has been deleted"));
        } catch (Exception e) {
            log.error("Error deleting user", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete user: " + e.getMessage()));
        }
    }

    // Team Management
    @GetMapping("/teams")
    public ResponseEntity<ApiResponse<Object>> getAllTeams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Object teams = adminService.getAllTeams(pageable, search);
            return ResponseEntity.ok(ApiResponse.success("Teams retrieved successfully", teams));
        } catch (Exception e) {
            log.error("Error retrieving teams", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve teams: " + e.getMessage()));
        }
    }

    @PostMapping("/teams/{teamId}/disband")
    public ResponseEntity<ApiResponse<String>> disbandTeam(
            @PathVariable Long teamId,
            @RequestBody Map<String, String> data,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            String reason = data.get("reason");
            adminService.disbandTeam(teamId, reason);
            return ResponseEntity.ok(ApiResponse.success("Team disbanded successfully", "Team has been disbanded"));
        } catch (Exception e) {
            log.error("Error disbanding team", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to disband team: " + e.getMessage()));
        }
    }

    // System Management
    @PostMapping("/system/clear-cache")
    public ResponseEntity<ApiResponse<String>> clearCache(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            adminService.clearCache();
            return ResponseEntity.ok(ApiResponse.success("Cache cleared successfully", "System cache has been cleared"));
        } catch (Exception e) {
            log.error("Error clearing cache", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to clear cache: " + e.getMessage()));
        }
    }
}
