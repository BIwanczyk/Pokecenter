package pokecenter.controller;

import pokecenter.dto.*;
import pokecenter.model.*;
import pokecenter.repository.*;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pokeshops")
public class PokeshopController {

    private final PokeshopRepository shopRepo;
    private final PokecenterRepository pokecenterRepo;

    public PokeshopController(PokeshopRepository shopRepo, PokecenterRepository pokecenterRepo) {
        this.shopRepo = shopRepo;
        this.pokecenterRepo = pokecenterRepo;
    }

    @GetMapping
    public List<Pokeshop> getAllShops() {
        return shopRepo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pokeshop> getShopById(@PathVariable Long id) {
        return shopRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createShop(@Valid @RequestBody PokeshopRequest request) {
        return pokecenterRepo.findById(request.pokecenterId()).<ResponseEntity<?>>map(pokecenter -> {
            try {
                Pokeshop shop = Pokeshop.create(
                        request.name(),
                        request.managerName(),
                        request.phoneNumber(),
                        pokecenter,
                        request.shopTypes(),
                        request.pokeballVarieties(),
                        request.medicalItemsCount()
                );
                return ResponseEntity.ok(shopRepo.save(shop));
            } catch (RuntimeException ex) {
                return ResponseEntity.badRequest().body(ex.getMessage());
            }
        }).orElseGet(() -> ResponseEntity.badRequest().body("Invalid pokecenterId."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateShop(@PathVariable Long id, @Valid @RequestBody PokeshopRequest request) {
        return shopRepo.findById(id).flatMap(existing ->
                pokecenterRepo.findById(request.pokecenterId()).map(pokecenter -> {
                    try {
                        existing.setPhoneNumber(request.phoneNumber());
                        existing.setManagerName(request.managerName());
                        existing.setName(request.name());

                        if (existing.getPokecenter() != null
                                && !existing.getPokecenter().getId().equals(request.pokecenterId())) {
                            return ResponseEntity.badRequest().body("Cannot change pokecenterId for existing shop.");
                        }

                        return ResponseEntity.ok(shopRepo.save(existing));
                    } catch (RuntimeException ex) {
                        return ResponseEntity.badRequest().body(ex.getMessage());
                    }
                })
        ).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShop(@PathVariable Long id) {
        if (!shopRepo.existsById(id)) return ResponseEntity.notFound().build();
        shopRepo.deleteById(id); 
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/actions/sell-pokeball")
    public ResponseEntity<?> sellPokeball(@PathVariable Long id, @RequestParam(defaultValue = "1") int count) {
        return shopRepo.findById(id).map(shop -> {
            try {
                for (int i = 0; i < count; i++) shop.sellPokeball();
                return ResponseEntity.ok(shopRepo.save(shop));
            } catch (RuntimeException ex) {
                return ResponseEntity.badRequest().body(ex.getMessage());
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/actions/sell-medical-item")
    public ResponseEntity<?> sellMedicalItem(@PathVariable Long id, @RequestParam(defaultValue = "1") int count) {
        return shopRepo.findById(id).map(shop -> {
            try {
                for (int i = 0; i < count; i++) shop.sellMedicalItem();
                return ResponseEntity.ok(shopRepo.save(shop));
            } catch (RuntimeException ex) {
                return ResponseEntity.badRequest().body(ex.getMessage());
            }
        }).orElse(ResponseEntity.notFound().build());
    }
}