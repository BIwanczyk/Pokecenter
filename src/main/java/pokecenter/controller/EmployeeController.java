
package pokecenter.controller;

import pokecenter.dto.*;
import pokecenter.model.*;
import pokecenter.repository.*;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeRepository employeeRepo;
    private final PokecenterRepository pokecenterRepo;

    public EmployeeController(EmployeeRepository employeeRepo, PokecenterRepository pokecenterRepo) {
        this.employeeRepo = employeeRepo;
        this.pokecenterRepo = pokecenterRepo;
    }

    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeRepo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        return employeeRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createEmployee(@Valid @RequestBody EmployeeRequest req) {
        return pokecenterRepo.findById(req.pokecenterId())
                .<ResponseEntity<?>>map(pokecenter -> {
                    Employee employee = new Employee(
                            req.name(),
                            req.surname(),
                            req.age(),
                            req.phoneNumber(),
                            pokecenter
                    );
                    return ResponseEntity.ok(employeeRepo.save(employee));
                })
                .orElseGet(() -> ResponseEntity.badRequest().body("Invalid pokecenterId."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequest req) {
        return employeeRepo.findById(id).flatMap(existing ->
                pokecenterRepo.findById(req.pokecenterId()).map(pokecenter -> {
                    existing.setName(req.name());
                    existing.setSurname(req.surname());
                    existing.setAge(req.age());
                    existing.setPhoneNumber(req.phoneNumber());
                    existing.setPokecenter(pokecenter);
                    return ResponseEntity.ok(employeeRepo.save(existing));
                })
        ).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        if (!employeeRepo.existsById(id)) return ResponseEntity.notFound().build();
        employeeRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}