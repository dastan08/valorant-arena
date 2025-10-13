package com.valorantarena.repository;

import com.valorantarena.entity.Team;
import com.valorantarena.entity.TeamInvitation;
import com.valorantarena.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeamInvitationRepository extends JpaRepository<TeamInvitation, Long> {
    
    List<TeamInvitation> findByInvitedUserAndStatus(User invitedUser, TeamInvitation.InvitationStatus status);
    
    List<TeamInvitation> findByTeamAndStatus(Team team, TeamInvitation.InvitationStatus status);
    
    Optional<TeamInvitation> findByTeamAndInvitedUserAndStatus(
        Team team, 
        User invitedUser, 
        TeamInvitation.InvitationStatus status
    );
    
    List<TeamInvitation> findByStatusAndCreatedAtBefore(
        TeamInvitation.InvitationStatus status,
        LocalDateTime expiryTime
    );
    
    long countByTeamAndStatus(Team team, TeamInvitation.InvitationStatus status);
}
