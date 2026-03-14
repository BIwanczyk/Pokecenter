package pokecenter.repository;

import pokecenter.model.Registration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    List<Registration> findByTrainerId(Long trainerId);
    List<Registration> findByPokecenterId(Long pokecenterId);

    // HISTORY/BAG
    List<Registration> findByTrainerIdAndPokecenterId(Long trainerId, Long pokecenterId);
    Optional<Registration> findTopByTrainerIdAndPokecenterIdOrderByRegistrationDateDesc(Long trainerId, Long pokecenterId);
    List<Registration> findByTrainerIdOrderByRegistrationDateDesc(Long trainerId);
    List<Registration> findByPokecenterIdOrderByRegistrationDateDesc(Long pokecenterId);
    List<Registration> findByTrainerIdAndPokecenterIdOrderByRegistrationDateDesc(Long trainerId, Long pokecenterId);
}
