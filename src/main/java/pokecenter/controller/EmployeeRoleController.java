
package pokecenter.controller;

import pokecenter.dto.*;
import pokecenter.model.*;
import pokecenter.repository.*;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employees")
public class EmployeeRoleController {

    private final EmployeeRepository employeeRepo;
    private final NurseRepository nurseRepo;
    private final ShopAssistantRepository shopAssistantRepo;
    private final PokeshopRepository pokeshopRepo;

    public EmployeeRoleController(EmployeeRepository employeeRepo,
                                  NurseRepository nurseRepo,
                                  ShopAssistantRepository shopAssistantRepo,
                                  PokeshopRepository pokeshopRepo) {
        this.employeeRepo = employeeRepo;
        this.nurseRepo = nurseRepo;
        this.shopAssistantRepo = shopAssistantRepo;
        this.pokeshopRepo = pokeshopRepo;
    }

    //Dynamic inheritance: change role to Nurse

    @PostMapping("/{id}/role/nurse")
    @Transactional
    public ResponseEntity<?> changeRoleToNurse(@PathVariable Long id) {
        var oldOpt = employeeRepo.findById(id);
        if (oldOpt.isEmpty()) return ResponseEntity.notFound().build();

        Employee old = oldOpt.get();
        if (old instanceof Nurse) {
            return ResponseEntity.ok(old); 
        }

        Nurse fresh = nurseRepo.save(new Nurse(old));
        employeeRepo.delete(old);
        return ResponseEntity.ok(fresh);
    }

    //Dynamic inheritance: change role to ShopAssistant

    @PostMapping("/{id}/role/shop-assistant")
    @Transactional
    public ResponseEntity<?> changeRoleToShopAssistant(@PathVariable Long id,
                                                       @Valid @RequestBody ChangeToShopAssistantRequest req) {
        var oldOpt = employeeRepo.findById(id);
        if (oldOpt.isEmpty()) return ResponseEntity.notFound().build();
        Employee old = oldOpt.get();

        var shopOpt = pokeshopRepo.findById(req.pokeshopId());
        if (shopOpt.isEmpty()) return ResponseEntity.badRequest().body("Invalid pokeshopId.");
        Pokeshop shop = shopOpt.get();

        if (old.getPokecenter() == null || shop.getPokecenter() == null ||
                !old.getPokecenter().getId().equals(shop.getPokecenter().getId())) {
            return ResponseEntity.badRequest().body("Pokeshop must belong to the same Pokecenter as employee.");
        }

        if (old instanceof ShopAssistant sa) {
            sa.setPokeshop(shop);
            return ResponseEntity.ok(shopAssistantRepo.save(sa));
        }

        ShopAssistant fresh = shopAssistantRepo.save(new ShopAssistant(old, shop));
        employeeRepo.delete(old);
        return ResponseEntity.ok(fresh);
    }
}