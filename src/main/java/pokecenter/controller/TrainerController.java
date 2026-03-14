
package pokecenter.controller;

import pokecenter.dto.*;
import pokecenter.model.*;
import pokecenter.repository.*;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trainers")
public class TrainerController {

    private final TrainerRepository trainerRepo;
    private final EventOrganizerRepository organizerRepo;

    public TrainerController(TrainerRepository trainerRepo, EventOrganizerRepository organizerRepo) {
        this.trainerRepo = trainerRepo;
        this.organizerRepo = organizerRepo;
    }

    @GetMapping
    public List<Trainer> getAllTrainers() {
        return trainerRepo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trainer> getTrainerById(@PathVariable Long id) {
        return trainerRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createTrainer(@Valid @RequestBody TrainerRequest request) {
        String email = normalizeEmail(request.email());
        if (trainerRepo.findByEmail(email).isPresent() || organizerRepo.findByEmail(email).isPresent()) {
            return ResponseEntity.status(409).body("Email already in use.");
        }
        Trainer trainer = new Trainer(
                request.name(),
                request.surname(),
                request.age(),
                request.phoneNumber(),
                request.hasInsurance(),
                email,
                request.badgeCount()
        );
        return ResponseEntity.ok(trainerRepo.save(trainer));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTrainer(@PathVariable Long id, @Valid @RequestBody TrainerRequest request) {
        return trainerRepo.findById(id).map(trainer -> {
            String email = normalizeEmail(request.email());

            // blokada na maile
            boolean emailTakenByOtherTrainer = trainerRepo.findByEmail(email)
                    .filter(t -> !t.getId().equals(id))
                    .isPresent();
            boolean emailTakenByOrganizer = organizerRepo.findByEmail(email).isPresent();
            if (emailTakenByOtherTrainer || emailTakenByOrganizer) {
                return ResponseEntity.status(409).body("Email already in use.");
            }

            trainer.setName(request.name());
            trainer.setSurname(request.surname());
            trainer.setAge(request.age());
            trainer.setPhoneNumber(request.phoneNumber());
            trainer.setHasInsurance(request.hasInsurance());
            trainer.setEmail(email);
            trainer.setBadgeCount(request.badgeCount());
            return ResponseEntity.ok(trainerRepo.save(trainer));
        }).orElse(ResponseEntity.notFound().build());
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}