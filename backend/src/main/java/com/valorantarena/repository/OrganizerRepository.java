package com.valorantarena.repository;

import com.valorantarena.entity.User;
import com.valorantarena.model.Organizer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizerRepository extends JpaRepository<Organizer, Long> {
    
    Optional<Organizer> findByUser(User user);
    
    Optional<Organizer> findByUserId(Long userId);
    
    Page<Organizer> findByVerificationStatus(Organizer.VerificationStatus status, Pageable pageable);
    
    boolean existsByUserId(Long userId);
}
