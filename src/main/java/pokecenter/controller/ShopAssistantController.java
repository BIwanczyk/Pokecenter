
package pokecenter.controller;

import pokecenter.dto.*;
import pokecenter.model.*;
import pokecenter.repository.*;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shop-assistants")
public class ShopAssistantController {

    private final ShopAssistantRepository repo;
    private final PokecenterRepository pokecenterRepo;
    private final PokeshopRepository pokeshopRepo;

    public ShopAssistantController(ShopAssistantRepository repo,
                                   PokecenterRepository pokecenterRepo,
                                   PokeshopRepository pokeshopRepo) {
        this.repo = repo;
        this.pokecenterRepo = pokecenterRepo;
        this.pokeshopRepo = pokeshopRepo;
    }

    @GetMapping
    public List<ShopAssistant> all() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShopAssistant> one(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ShopAssistantRequest req) {
        var pcOpt = pokecenterRepo.findById(req.pokecenterId());
        if (pcOpt.isEmpty()) return ResponseEntity.badRequest().body("Invalid pokecenterId.");

        var shopOpt = pokeshopRepo.findById(req.pokeshopId());
        if (shopOpt.isEmpty()) return ResponseEntity.badRequest().body("Invalid pokeshopId.");

        Pokecenter pc = pcOpt.get();
        Pokeshop shop = shopOpt.get();

        if (shop.getPokecenter() == null || !shop.getPokecenter().getId().equals(pc.getId())) {
            return ResponseEntity.badRequest().body("Pokeshop must belong to the given Pokecenter.");
        }

        ShopAssistant e = new ShopAssistant(req.name(), req.surname(), req.age(), req.phoneNumber(), pc, shop);
        return ResponseEntity.ok(repo.save(e));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody ShopAssistantRequest req) {
        var pcOpt = pokecenterRepo.findById(req.pokecenterId());
        if (pcOpt.isEmpty()) return ResponseEntity.badRequest().body("Invalid pokecenterId.");

        var shopOpt = pokeshopRepo.findById(req.pokeshopId());
        if (shopOpt.isEmpty()) return ResponseEntity.badRequest().body("Invalid pokeshopId.");

        Pokecenter pc = pcOpt.get();
        Pokeshop shop = shopOpt.get();

        if (shop.getPokecenter() == null || !shop.getPokecenter().getId().equals(pc.getId())) {
            return ResponseEntity.badRequest().body("Pokeshop must belong to the given Pokecenter.");
        }

        return repo.findById(id).map(existing -> {
            existing.setName(req.name());
            existing.setSurname(req.surname());
            existing.setAge(req.age());
            existing.setPhoneNumber(req.phoneNumber());
            existing.setPokecenter(pc);
            existing.setPokeshop(shop);
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}