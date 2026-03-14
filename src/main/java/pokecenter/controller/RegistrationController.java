package pokecenter.controller;

import pokecenter.dto.*;
import pokecenter.model.*;
import pokecenter.repository.*;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/registrations")
public class RegistrationController {

    private final RegistrationRepository registrationRepo;
    private final TrainerRepository trainerRepo;
    private final PokecenterRepository pokecenterRepo;

    public RegistrationController(RegistrationRepository registrationRepo, TrainerRepository trainerRepo, PokecenterRepository pokecenterRepo) {
        this.registrationRepo = registrationRepo;
        this.trainerRepo = trainerRepo;
        this.pokecenterRepo = pokecenterRepo;
    }

    private static RegistrationDto toDto(Registration r) {
        Long trainerId = r.getTrainer() != null ? r.getTrainer().getId() : null;
        Long pokecenterId = r.getPokecenter() != null ? r.getPokecenter().getId() : null;
        String trainerName = r.getTrainer() != null ? r.getTrainer().getFullName() : null;
        String pokecenterLabel = r.getPokecenter() != null ? r.getPokecenter().getLocation() : null;

        return new RegistrationDto(
                r.getId(),
                trainerId,
                pokecenterId,
                trainerName,
                pokecenterLabel,
                r.getRegistrationDate()
        );
    }

    @GetMapping
    public List<RegistrationDto> list(@RequestParam(required = false) Long trainerId,
                                      @RequestParam(required = false) Long pokecenterId) {

        if (trainerId != null && pokecenterId != null) {
            return registrationRepo.findByTrainerIdAndPokecenterIdOrderByRegistrationDateDesc(trainerId, pokecenterId)
                    .stream().map(RegistrationController::toDto).toList();
        }
        if (trainerId != null) {
            return registrationRepo.findByTrainerIdOrderByRegistrationDateDesc(trainerId)
                    .stream().map(RegistrationController::toDto).toList();
        }
        if (pokecenterId != null) {
            return registrationRepo.findByPokecenterIdOrderByRegistrationDateDesc(pokecenterId)
                    .stream().map(RegistrationController::toDto).toList();
        }

        return registrationRepo.findAll().stream().map(RegistrationController::toDto).toList();
    }

    @PostMapping
    @Transactional
    public RegistrationDto create(@Valid @RequestBody RegistrationRequest req) {
        var t = trainerRepo.findById(req.trainerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trainer not found"));

        if (!Boolean.TRUE.equals(t.getHasInsurance())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trainer must have active insurance");
        }

        var pc = pokecenterRepo.findById(req.pokecenterId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pokecenter not found"));

        // BAG/HISTORY brak blokady na duplikatach
        var r = registrationRepo.save(new Registration(t, pc));
        return toDto(r);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!registrationRepo.existsById(id)) return ResponseEntity.notFound().build();
        registrationRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
