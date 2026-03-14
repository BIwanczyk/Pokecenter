package pokecenter.controller;

import pokecenter.dto.*;
import pokecenter.model.*;
import pokecenter.repository.*;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/pokecenters")
public class PokecenterController {

    private final PokecenterRepository pokecenterRepo;
    private final EmployeeRepository employeeRepo;
    private final PokeshopRepository pokeshopRepo;
    private final EventRepository eventRepo;

    public PokecenterController(
            PokecenterRepository pokecenterRepo,
            EmployeeRepository employeeRepo,
            PokeshopRepository pokeshopRepo,
            EventRepository eventRepo
    ) {
        this.pokecenterRepo = pokecenterRepo;
        this.employeeRepo = employeeRepo;
        this.pokeshopRepo = pokeshopRepo;
        this.eventRepo = eventRepo;
    }

    @GetMapping
    public List<PokecenterListDto> getAllPokecenters() {
        return pokecenterRepo.findAll().stream()
                .map(pc -> new PokecenterListDto(pc.getId(), pc.getLocation(), pc.getPhoneNumber(), pc.getEmail()))
                .toList();
    }

    public record PokecenterListDto(Long id, String location, String phoneNumber, String email) {}

    @GetMapping("/{id}")
    public ResponseEntity<Pokecenter> getPokecenterById(@PathVariable Long id) {
        return pokecenterRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Pokecenter> createPokecenter(@Valid @RequestBody PokecenterRequest req) {
        Pokecenter pokecenter = new Pokecenter(req.location(), req.phoneNumber(), req.email());
        return ResponseEntity.ok(pokecenterRepo.save(pokecenter));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pokecenter> updatePokecenter(@PathVariable Long id, @Valid @RequestBody PokecenterRequest req) {
        return pokecenterRepo.findById(id).map(existing -> {
            existing.setLocation(req.location());
            existing.setPhoneNumber(req.phoneNumber());
            existing.setEmail(req.email());
            return ResponseEntity.ok(pokecenterRepo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePokecenter(@PathVariable Long id) {
        if (!pokecenterRepo.existsById(id)) return ResponseEntity.notFound().build();
        pokecenterRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/employees")
    public ResponseEntity<Employee> addEmployeeToPokecenter(@PathVariable Long id,
                                                            @Valid @RequestBody EmployeeInPokecenterRequest req) {
        return pokecenterRepo.findById(id).map(pokecenter -> {
            Employee employee = new Employee(
                    req.name(),
                    req.surname(),
                    req.age(),
                    req.phoneNumber(),
                    pokecenter
            );
            return ResponseEntity.ok(employeeRepo.save(employee));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/shops")
    public ResponseEntity<?> addShopToPokecenter(@PathVariable Long id,
                                                 @Valid @RequestBody PokeshopInPokecenterRequest req) {
        return pokecenterRepo.findById(id).map(pokecenter -> {
            try {
                Pokeshop shop = Pokeshop.create(
                        req.name(),
                        req.managerName(),
                        req.phoneNumber(),
                        pokecenter,
                        req.shopTypes(),
                        req.pokeballVarieties(),
                        req.medicalItemsCount()
                );
                return ResponseEntity.ok(pokeshopRepo.save(shop));
            } catch (RuntimeException ex) {
                return ResponseEntity.badRequest().body(ex.getMessage());
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/events")
    public ResponseEntity<?> getEventsByPokecenter(
            @PathVariable Long id,
            @RequestParam(value = "type", required = false) String type
    ) {
        if (!pokecenterRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        List<Event> events;
        if ("private".equalsIgnoreCase(type)) {
            events = eventRepo.findPrivateByPokecenter(id);
        } else if ("public".equalsIgnoreCase(type)) {
            events = eventRepo.findPublicByPokecenter(id);
        } else {
            events = eventRepo.findAllByPokecenter(id);
        }

        List<Map<String, Object>> out = new ArrayList<>();
        for (Event e : events) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", e.getId());
            m.put("eventName", e.getEventName());
            m.put("date", e.getDate());
            m.put("eventType", e instanceof PublicEvent ? "Public Event" : (e instanceof PrivateEvent ? "Private Event" : "Event"));
            m.put("pokecenterId", e.getPokecenter() != null ? e.getPokecenter().getId() : null);
            out.add(m);
        }
        out.sort(Comparator.comparing(x -> String.valueOf(x.get("date"))));
        return ResponseEntity.ok(out);
    }

    @DeleteMapping("/{pcId}/shops/{shopId}")
    public ResponseEntity<?> deleteShopFromPokecenter(@PathVariable Long pcId, @PathVariable Long shopId) {
        return pokecenterRepo.findById(pcId).map(pc ->
                pokeshopRepo.findById(shopId).map(shop -> {
                    if (shop.getPokecenter() == null || !pcId.equals(shop.getPokecenter().getId())) {
                        return ResponseEntity.badRequest().body("Shop does not belong to this Pokecenter.");
                    }

                    pc.removeShop(shop);
                    pokecenterRepo.save(pc); 

                    return ResponseEntity.noContent().build();
                }).orElse(ResponseEntity.notFound().build())
        ).orElse(ResponseEntity.notFound().build());
    }
}