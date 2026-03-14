package pokecenter.repository;

import pokecenter.model.*;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("select distinct e from Event e left join fetch e.participantsByRegistrationNumber where e.id = :id")
    Optional<Event> findByIdWithParticipants(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select distinct e from Event e left join fetch e.participantsByRegistrationNumber where e.id = :id")
    Optional<Event> findByIdWithParticipantsForUpdate(@Param("id") Long id);

    @Query("select e from Event e where e.pokecenter.id = :pcId")
    List<Event> findAllByPokecenter(@Param("pcId") Long pcId);

    @Query("select e from Event e where e.pokecenter.id = :pcId and type(e) = PrivateEvent")
    List<Event> findPrivateByPokecenter(@Param("pcId") Long pcId);

    @Query("select e from Event e where e.pokecenter.id = :pcId and type(e) = PublicEvent")
    List<Event> findPublicByPokecenter(@Param("pcId") Long pcId);
}
