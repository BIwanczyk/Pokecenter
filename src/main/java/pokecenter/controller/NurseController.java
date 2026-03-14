
package pokecenter.controller;

import pokecenter.dto.*;
import pokecenter.model.*;
import pokecenter.repository.*;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nurses")
public class NurseController {

    private final NurseRepository nurseRepo;
    private final PokecenterRepository pokecenterRepo;

    public NurseController(NurseRepository nurseRepo, PokecenterRepository pokecenterRepo) {
        this.nurseRepo = nurseRepo;
        this.pokecenterRepo = pokecenterRepo;
    }

    @GetMapping
    public List<Nurse> all() {
        return nurseRepo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Nurse> one(@PathVariable Long id) {
        return nurseRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody NurseRequest req) {
        return pokecenterRepo.findById(req.pokecenterId()).<ResponseEntity<?>>map(pc -> {
            Nurse n = new Nurse(req.name(), req.surname(), req.age(), req.phoneNumber(), pc);
            return ResponseEntity.ok(nurseRepo.save(n));
        }).orElseGet(() -> ResponseEntity.badRequest().body("Invalid pokecenterId."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody NurseRequest req) {
        return nurseRepo.findById(id).flatMap(existing ->
                pokecenterRepo.findById(req.pokecenterId()).map(pc -> {
                    existing.setName(req.name());
                    existing.setSurname(req.surname());
                    existing.setAge(req.age());
                    existing.setPhoneNumber(req.phoneNumber());
                    existing.setPokecenter(pc);
                    return ResponseEntity.ok(nurseRepo.save(existing));
                })
        ).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!nurseRepo.existsById(id)) return ResponseEntity.notFound().build();
        nurseRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}