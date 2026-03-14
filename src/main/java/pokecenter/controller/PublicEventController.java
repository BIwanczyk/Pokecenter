
package pokecenter.controller;

import pokecenter.dto.*;
import pokecenter.model.*;
import pokecenter.repository.*;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public-events")
public class PublicEventController {

    private final PublicEventRepository repo;
    private final PokecenterRepository pokecenterRepo;

    public PublicEventController(PublicEventRepository repo, PokecenterRepository pokecenterRepo) {
        this.repo = repo;
        this.pokecenterRepo = pokecenterRepo;
    }

    @GetMapping
    public List<PublicEvent> all() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicEvent> one(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody PublicEventRequest req) {
        int maxParticipants = req.maxParticipants() != null ? req.maxParticipants() : 20;
        int maxAudience = req.maxAudience() != null ? req.maxAudience() : 100;

        PublicEvent ev = new PublicEvent(req.eventName(), req.date(), maxParticipants, maxAudience);

        if (req.pokecenterId() != null) {
            return pokecenterRepo.findById(req.pokecenterId())
                    .<ResponseEntity<?>>map(pc -> {
                        ev.setPokecenter(pc);
                        return ResponseEntity.ok(repo.save(ev));
                    })
                    .orElseGet(() -> ResponseEntity.badRequest().body("Invalid pokecenterId."));
        }

        return ResponseEntity.ok(repo.save(ev));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody PublicEventRequest req) {
        return repo.findById(id).map(e -> {
            e.setEventName(req.eventName());
            e.setDate(req.date());
            if (req.maxParticipants() != null) e.setMaxParticipants(req.maxParticipants());
            if (req.maxAudience() != null) e.setMaxAudience(req.maxAudience());
            if (req.pokecenterId() != null) pokecenterRepo.findById(req.pokecenterId()).ifPresent(e::setPokecenter);
            return ResponseEntity.ok(repo.save(e));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}