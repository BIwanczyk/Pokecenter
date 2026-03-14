package pokecenter.repository;

import pokecenter.model.*;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PokecenterRepository extends JpaRepository<Pokecenter, Long> {

    @EntityGraph(attributePaths = {"employees", "shops"})
    Optional<Pokecenter> findById(Long id);
}