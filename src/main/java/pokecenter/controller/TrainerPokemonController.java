
package pokecenter.controller;

import pokecenter.dto.*;
import pokecenter.model.*;
import pokecenter.repository.*;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/trainers")
public class TrainerPokemonController {

    private final TrainerRepository trainerRepo;
    private final PokemonRepository pokemonRepo;

    public TrainerPokemonController(TrainerRepository trainerRepo, PokemonRepository pokemonRepo) {
        this.trainerRepo = trainerRepo;
        this.pokemonRepo = pokemonRepo;
    }

    private ResponseEntity<?> withTrainer(Long trainerId, java.util.function.Function<Trainer, ResponseEntity<?>> fn) {
        return trainerRepo.findById(trainerId).<ResponseEntity<?>>map(fn).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{trainerId}/pokemons/caught")
    public ResponseEntity<?> getCaught(@PathVariable Long trainerId) {
        return withTrainer(trainerId, t -> ResponseEntity.ok(t.getCaughtPokemons()));
    }

    @GetMapping("/{trainerId}/pokemons/active")
    public ResponseEntity<?> getActive(@PathVariable Long trainerId) {
        return withTrainer(trainerId, t -> ResponseEntity.ok(t.getActiveTeamPokemons()));
    }

    @PostMapping("/{trainerId}/pokemons/caught/{pokemonId}")
    @Transactional
    public ResponseEntity<?> catchPokemon(@PathVariable Long trainerId, @PathVariable Long pokemonId) {
        return withTrainer(trainerId, t ->
                pokemonRepo.findById(pokemonId)
                        .<ResponseEntity<?>>map(p -> {
                            t.catchPokemon(p);
                            trainerRepo.save(t);
                            return ResponseEntity.ok(t.getCaughtPokemons());
                        })
                        .orElseGet(() -> ResponseEntity.badRequest().body("Invalid pokemonId."))
        );
    }

    @DeleteMapping("/{trainerId}/pokemons/caught/{pokemonId}")
    @Transactional
    public ResponseEntity<?> releasePokemon(@PathVariable Long trainerId, @PathVariable Long pokemonId) {
        return withTrainer(trainerId, t ->
                pokemonRepo.findById(pokemonId)
                        .<ResponseEntity<?>>map(p -> {
                            t.releasePokemon(p);
                            trainerRepo.save(t);
                            return ResponseEntity.noContent().build();
                        })
                        .orElseGet(() -> ResponseEntity.badRequest().body("Invalid pokemonId."))
        );
    }

    @PostMapping("/{trainerId}/pokemons/active/{pokemonId}")
    @Transactional
    public ResponseEntity<?> addToActive(@PathVariable Long trainerId, @PathVariable Long pokemonId) {
        return withTrainer(trainerId, t ->
                pokemonRepo.findById(pokemonId)
                        .<ResponseEntity<?>>map(p -> {
                            try {
                                t.addToActiveTeam(p);
                                trainerRepo.save(t);
                                return ResponseEntity.ok(t.getActiveTeamPokemons());
                            } catch (RuntimeException ex) {
                                return ResponseEntity.badRequest().body(ex.getMessage());
                            }
                        })
                        .orElseGet(() -> ResponseEntity.badRequest().body("Invalid pokemonId."))
        );
    }

    @DeleteMapping("/{trainerId}/pokemons/active/{pokemonId}")
    @Transactional
    public ResponseEntity<?> removeFromActive(@PathVariable Long trainerId, @PathVariable Long pokemonId) {
        return withTrainer(trainerId, t ->
                pokemonRepo.findById(pokemonId)
                        .<ResponseEntity<?>>map(p -> {
                            t.removeFromActiveTeam(p);
                            trainerRepo.save(t);
                            return ResponseEntity.noContent().build();
                        })
                        .orElseGet(() -> ResponseEntity.badRequest().body("Invalid pokemonId."))
        );
    }
}