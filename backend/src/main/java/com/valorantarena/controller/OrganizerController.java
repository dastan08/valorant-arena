package com.valorantarena.controller;

import com.valorantarena.dto.ApiResponse;
import com.valorantarena.dto.OrganizerApplicationDTO;
import com.valorantarena.entity.User;
import com.valorantarena.model.Organizer;
import com.valorantarena.service.OrganizerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizers")
@RequiredArgsConstructor
public class OrganizerController {
    
    private final OrganizerService organizerService;
    
    @PostMapping("/apply")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Organizer>> applyAsOrganizer(
            @Valid @RequestBody OrganizerApplicationDTO dto,
            @AuthenticationPrincipal User user) {
        Organizer organizer = organizerService.applyAsOrganizer(dto, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Application submitted successfully", organizer));
    }
    
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Organizer>> getMyOrganizerProfile(@AuthenticationPrincipal User user) {
        Organizer organizer = organizerService.getOrganizerByUserId(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Organizer profile retrieved successfully", organizer));
    }
    
    @PutMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Organizer>> updateOrganizerProfile(
            @Valid @RequestBody OrganizerApplicationDTO dto,
            @AuthenticationPrincipal User user) {
        Organizer organizer = organizerService.updateOrganizerProfile(dto, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Organizer profile updated successfully", organizer));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Organizer>> getOrganizerById(@PathVariable Long id) {
        Organizer organizer = organizerService.getOrganizerById(id);
        return ResponseEntity.ok(ApiResponse.success("Organizer retrieved successfully", organizer));
    }
    
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<Organizer>>> getPendingOrganizers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Organizer> organizers = organizerService.getPendingOrganizers(pageable);
        return ResponseEntity.ok(ApiResponse.success("Pending organizers retrieved successfully", organizers));
    }
    
    @PostMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Organizer>> verifyOrganizer(
            @PathVariable Long id,
            @RequestParam boolean approved,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal User admin) {
        Organizer organizer = organizerService.verifyOrganizer(id, admin.getId(), approved, notes);
        return ResponseEntity.ok(ApiResponse.success("Organizer verification updated", organizer));
    }
    
    @PostMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Organizer>> suspendOrganizer(
            @PathVariable Long id,
            @RequestParam String reason) {
        Organizer organizer = organizerService.suspendOrganizer(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Organizer suspended", organizer));
    }
}
