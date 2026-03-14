package pokecenter.repository;

import pokecenter.model.*;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventOrganizerRepository extends JpaRepository<EventOrganizer, Long> {
    Optional<EventOrganizer> findByEmail(String email);
}