package pokecenter.controller;

import pokecenter.dto.*;
import pokecenter.model.*;
import pokecenter.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/trainers")
public class TrainerInsuranceController {

    private final TrainerRepository trainerRepo;
    private final RegistrationRepository registrationRepo;
    private final PokecenterRepository pokecenterRepo;

    public TrainerInsuranceController(TrainerRepository trainerRepo,
                                      RegistrationRepository registrationRepo,
                                      PokecenterRepository pokecenterRepo) {
        this.trainerRepo = trainerRepo;
        this.registrationRepo = registrationRepo;
        this.pokecenterRepo = pokecenterRepo;
    }

    private static RegistrationDto toDto(Registration r) {
        Long trainerId = r.getTrainer() != null ? r.getTrainer().getId() : null;
        Long pokecenterId = r.getPokecenter() != null ? r.getPokecenter().getId() : null;
        String trainerName = r.getTrainer() != null ? r.getTrainer().getFullName() : null;
        String pokecenterName = r.getPokecenter() != null ? r.getPokecenter().getLocation() : null;

        return new RegistrationDto(
                r.getId(),
                trainerId,
                pokecenterId,
                trainerName,
                pokecenterName,
                r.getRegistrationDate()
        );
    }

    @PostMapping("/{id}/insurance/purchase")
    @Transactional
    public ResponseEntity<RegistrationDto> buyInsurance(@PathVariable Long id,
                                                        @RequestParam(value = "pokecenterId") Long pokecenterId) {
        if (pokecenterId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "You must select a Pokecenter before purchasing insurance.");
        }

        Trainer t = trainerRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trainer not found"));

        Pokecenter pc = pokecenterRepo.findById(pokecenterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pokecenter not found"));

        if (!Boolean.TRUE.equals(t.getHasInsurance())) {
            t.setHasInsurance(true);
            trainerRepo.save(t);
        }

        Registration reg = registrationRepo
                .findTopByTrainerIdAndPokecenterIdOrderByRegistrationDateDesc(t.getId(), pc.getId())
                .orElseGet(() -> registrationRepo.save(new Registration(t, pc)));

        return ResponseEntity.ok(toDto(reg));
    }
}
