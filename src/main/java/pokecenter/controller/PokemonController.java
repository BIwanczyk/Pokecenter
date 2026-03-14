
package pokecenter.controller;

import pokecenter.dto.*;
import pokecenter.model.*;
import pokecenter.repository.*;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pokemons")
public class PokemonController {

    private final PokemonRepository repo;

    public PokemonController(PokemonRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Pokemon> all() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pokemon> one(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Pokemon> create(@Valid @RequestBody PokemonRequest req) {
        return ResponseEntity.ok(repo.save(new Pokemon(req.name(), req.baseHP(), req.level(), req.types())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pokemon> update(@PathVariable Long id, @Valid @RequestBody PokemonRequest req) {
        return repo.findById(id).map(p -> {
            p.setName(req.name());
            p.setBaseHP(req.baseHP());
            p.setLevel(req.level());
            p.setTypes(req.types());
            return ResponseEntity.ok(repo.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}