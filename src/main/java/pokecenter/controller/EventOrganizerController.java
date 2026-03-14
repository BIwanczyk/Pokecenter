
package pokecenter.controller;

import pokecenter.dto.*;
import pokecenter.model.*;
import pokecenter.repository.*;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/event-organizers")
public class EventOrganizerController {

    private final EventOrganizerRepository repo;
    private final PokecenterRepository pokecenterRepo;
    private final TrainerRepository trainerRepo;

    public EventOrganizerController(EventOrganizerRepository repo, PokecenterRepository pokecenterRepo, TrainerRepository trainerRepo) {
        this.repo = repo;
        this.pokecenterRepo = pokecenterRepo;
        this.trainerRepo = trainerRepo;
    }

    @GetMapping
    public List<EventOrganizer> all() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventOrganizer> one(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody EventOrganizerRequest req) {
        String email = normalizeEmail(req.email());
        if (repo.findByEmail(email).isPresent() || trainerRepo.findByEmail(email).isPresent()) {
            return ResponseEntity.status(409).body("Email already in use.");
        }
        return pokecenterRepo.findById(req.pokecenterId()).<ResponseEntity<?>>map(pc -> {
            EventOrganizer e = new EventOrganizer(
                    req.name(), req.surname(), req.age(), req.phoneNumber(),
                    req.hasInsurance(), email, req.badgeCount(), pc
            );
            return ResponseEntity.ok(repo.save(e));
        }).orElseGet(() -> ResponseEntity.badRequest().body("Invalid pokecenterId."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody EventOrganizerRequest req) {
        return repo.findById(id).flatMap(existing ->
                pokecenterRepo.findById(req.pokecenterId()).map(pc -> {
                    String email = normalizeEmail(req.email());

                    boolean emailTakenByOtherOrganizer = repo.findByEmail(email)
                            .filter(o -> !o.getId().equals(id))
                            .isPresent();
                    boolean emailTakenByTrainer = trainerRepo.findByEmail(email).isPresent();
                    if (emailTakenByOtherOrganizer || emailTakenByTrainer) {
                        return ResponseEntity.status(409).body("Email already in use.");
                    }

                    existing.setName(req.name());
                    existing.setSurname(req.surname());
                    existing.setAge(req.age());
                    existing.setPhoneNumber(req.phoneNumber());
                    existing.setHasInsurance(req.hasInsurance());
                    existing.setEmail(email);
                    existing.setBadgeCount(req.badgeCount());
                    existing.setPokecenter(pc);
                    return ResponseEntity.ok(repo.save(existing));
                })
        ).orElse(ResponseEntity.notFound().build());
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}