
package pokecenter.controller;

import pokecenter.dto.*;
import pokecenter.model.*;
import pokecenter.repository.*;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teams")
public class TeamController {

    private final TeamRepository repo;
    private final TrainerRepository trainerRepo;
    private final PokemonRepository pokemonRepo;

    public TeamController(TeamRepository repo, TrainerRepository trainerRepo, PokemonRepository pokemonRepo) {
        this.repo = repo;
        this.trainerRepo = trainerRepo;
        this.pokemonRepo = pokemonRepo;
    }

    @GetMapping
    public List<Team> all() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Team> one(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody TeamRequest req) {
        return trainerRepo.findById(req.trainerId()).<ResponseEntity<?>>map(tr -> {
            Team t = new Team(tr);
            if (req.pokemonIds() != null) {
                t.getPokemons().addAll(pokemonRepo.findAllById(req.pokemonIds()));
            }
            return ResponseEntity.ok(repo.save(t));
        }).orElseGet(() -> ResponseEntity.badRequest().body("Invalid trainerId."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody TeamRequest req) {
        return repo.findById(id).flatMap(existing ->
                trainerRepo.findById(req.trainerId()).map(tr -> {
                    existing.setTrainer(tr);
                    existing.getPokemons().clear();
                    if (req.pokemonIds() != null) {
                        existing.getPokemons().addAll(pokemonRepo.findAllById(req.pokemonIds()));
                    }
                    return ResponseEntity.ok(repo.save(existing));
                })
        ).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}