package com.valorantarena.service;

import com.valorantarena.dto.OrganizerApplicationDTO;
import com.valorantarena.entity.User;
import com.valorantarena.model.Organizer;
import com.valorantarena.model.Organizer.VerificationStatus;
import com.valorantarena.repository.OrganizerRepository;
import com.valorantarena.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrganizerService {
    
    private final OrganizerRepository organizerRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public Organizer applyAsOrganizer(OrganizerApplicationDTO dto, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user already has an organizer profile
        if (organizerRepository.existsByUserId(userId)) {
            throw new RuntimeException("You have already applied as an organizer");
        }
        
        Organizer organizer = new Organizer();
        organizer.setUser(user);
        organizer.setOrganizationName(dto.getOrganizationName());
        organizer.setDescription(dto.getDescription());
        organizer.setWebsiteUrl(dto.getWebsiteUrl());
        organizer.setDiscordServer(dto.getDiscordServer());
        organizer.setVerificationStatus(VerificationStatus.PENDING);
        
        return organizerRepository.save(organizer);
    }
    
    @Transactional
    public Organizer updateOrganizerProfile(OrganizerApplicationDTO dto, Long userId) {
        Organizer organizer = organizerRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Organizer profile not found"));
        
        // Can only update if not verified yet
        if (organizer.getVerificationStatus() == VerificationStatus.VERIFIED) {
            throw new RuntimeException("Cannot update verified organizer profile. Contact support for changes.");
        }
        
        organizer.setOrganizationName(dto.getOrganizationName());
        organizer.setDescription(dto.getDescription());
        organizer.setWebsiteUrl(dto.getWebsiteUrl());
        organizer.setDiscordServer(dto.getDiscordServer());
        
        return organizerRepository.save(organizer);
    }
    
    @Transactional
    public Organizer verifyOrganizer(Long organizerId, Long adminId, boolean approved, String notes) {
        Organizer organizer = organizerRepository.findById(organizerId)
            .orElseThrow(() -> new RuntimeException("Organizer not found"));
        
        User admin = userRepository.findById(adminId)
            .orElseThrow(() -> new RuntimeException("Admin not found"));
        
        if (approved) {
            organizer.setVerificationStatus(VerificationStatus.VERIFIED);
            organizer.setVerifiedBy(admin);
            organizer.setVerifiedAt(LocalDateTime.now());
        } else {
            organizer.setVerificationStatus(VerificationStatus.REJECTED);
            organizer.setRejectionReason(notes);
        }
        
        return organizerRepository.save(organizer);
    }
    
    @Transactional
    public Organizer suspendOrganizer(Long organizerId, String reason) {
        Organizer organizer = organizerRepository.findById(organizerId)
            .orElseThrow(() -> new RuntimeException("Organizer not found"));
        
        organizer.setVerificationStatus(VerificationStatus.SUSPENDED);
        organizer.setRejectionReason(reason);
        
        return organizerRepository.save(organizer);
    }
    
    public Organizer getOrganizerByUserId(Long userId) {
        return organizerRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Organizer profile not found"));
    }
    
    public Organizer getOrganizerById(Long id) {
        return organizerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Organizer not found"));
    }
    
    public Page<Organizer> getPendingOrganizers(Pageable pageable) {
        return organizerRepository.findByVerificationStatus(VerificationStatus.PENDING, pageable);
    }
    
    public Page<Organizer> getAllOrganizers(Pageable pageable) {
        return organizerRepository.findAll(pageable);
    }
}
