package pokecenter.repository;

import pokecenter.model.*;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PokemonRepository extends JpaRepository<Pokemon, Long> {}