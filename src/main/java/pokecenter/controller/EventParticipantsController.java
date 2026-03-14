package pokecenter.controller;

import pokecenter.dto.EventParticipantDto;
import pokecenter.model.*;
import pokecenter.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Comparator;

@RestController
@RequestMapping("/events")
public class EventParticipantsController {

    private final EventRepository eventRepo;
    private final TrainerRepository trainerRepo;

    public EventParticipantsController(EventRepository eventRepo, TrainerRepository trainerRepo) {
        this.eventRepo = eventRepo;
        this.trainerRepo = trainerRepo;
    }

    @GetMapping({"/{id}/participants", "/private/{id}/participants", "/public/{id}/participants"})
    public List<EventParticipantDto> list(@PathVariable Long id) {
        Event ev = eventRepo.findByIdWithParticipants(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        return ev.getParticipantsByRegistrationNumber().entrySet().stream()
                .map(e -> EventParticipantDto.from(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(EventParticipantDto::getSurname)
                        .thenComparing(EventParticipantDto::getName)
                        .thenComparingInt(EventParticipantDto::getRegistrationNumber))
                .collect(Collectors.toList());
    }

    @GetMapping({"/{id}/participants/by-number/{registrationNumber}",
            "/private/{id}/participants/by-number/{registrationNumber}",
            "/public/{id}/participants/by-number/{registrationNumber}"})
    public EventParticipantDto getByNumber(@PathVariable Long id, @PathVariable int registrationNumber) {
        Event ev = eventRepo.findByIdWithParticipants(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        Trainer t = ev.getParticipantByRegistrationNumber(registrationNumber);
        if (t == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Participant not found for registration number: " + registrationNumber);
        }
        return EventParticipantDto.from(registrationNumber, t);
    }

    @PostMapping({"/{id}/register", "/private/{id}/register", "/public/{id}/register"})
    @Transactional
    public ResponseEntity<?> register(
            @PathVariable Long id,
            @RequestParam(required = false) Long trainerId,
            @RequestBody(required = false) Map<String, Object> body
    ) {
        //pobieranie trainerid z body jeżeli nie przyszedł jako query
        if (trainerId == null && body != null && body.get("trainerId") != null) {
            Object v = body.get("trainerId");
            if (v instanceof Number n) trainerId = n.longValue();
            else trainerId = Long.valueOf(v.toString());
        }
        if (trainerId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("trainerId required");
        }

        // pobieranie eventu z blokadą na bazie
        Event ev = eventRepo.findByIdWithParticipantsForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        //pobieranie trenera po trainerId
        Trainer tr = trainerRepo.findById(trainerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trainer not found"));

        if (tr.hasInsurance() == null || !tr.hasInsurance()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Trainer must have active insurance to register.");
        }

        //sprawdza czy jest zapisany
        Integer existingRegNo = ev.getRegistrationNumberForTrainer(tr);
        if (existingRegNo != null) {
            return ResponseEntity.ok(Map.of(
                    "status", "ok",
                    "info", "already-registered",
                    "eventId", id,
                    "trainerId", trainerId,
                    "registrationNumber", existingRegNo
            ));
        }

        if (!ev.isRegistrationOpen()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Registrations are not open for this event. Current status: " + ev.getStatus());
        }

        final int regNo;
        try {
            regNo = ev.registerTrainer(tr); // metoda z modeli
            eventRepo.save(ev); //jpa utrwala zmiany
            //obsługa błędów
        } catch (IllegalStateException ex) {
            String raw = String.valueOf(ex.getMessage());
            String low = raw.toLowerCase();
            if (low.contains("full") || low.contains("max") || low.contains("limit") || low.contains("uczest")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Limit uczestników osiągnięty");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(raw);
        }

        //sukces
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "eventId", id,
                "trainerId", trainerId,
                "registrationNumber", regNo
        ));
    }

    @DeleteMapping("/{id}/participants/{trainerId}")
    @Transactional
    public ResponseEntity<?> unregister(@PathVariable Long id, @PathVariable Long trainerId) {
        Event ev = eventRepo.findByIdWithParticipants(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        if (!ev.isRegistrationOpen()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot unregister when registrations are not open. Current status: " + ev.getStatus());
        }

        Trainer tr = trainerRepo.findById(trainerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trainer not found"));

        ev.unregisterTrainer(tr);
        eventRepo.save(ev);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping({"/{id}/participants/by-number/{registrationNumber}",
            "/private/{id}/participants/by-number/{registrationNumber}",
            "/public/{id}/participants/by-number/{registrationNumber}"})
    @Transactional
    public ResponseEntity<?> unregisterByNumber(@PathVariable Long id, @PathVariable int registrationNumber) {
        Event ev = eventRepo.findByIdWithParticipants(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        if (!ev.isRegistrationOpen()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot unregister when registrations are not open. Current status: " + ev.getStatus());
        }

        if (ev.getParticipantByRegistrationNumber(registrationNumber) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Participant not found for registration number: " + registrationNumber);
        }

        ev.unregisterTrainerByRegistrationNumber(registrationNumber);
        eventRepo.save(ev);
        return ResponseEntity.noContent().build();
    }

    @GetMapping({"", "/all"})
    public List<Map<String, Object>> listAll(@RequestParam(required = false) String type) {
        List<Event> list;
        if ("private".equalsIgnoreCase(type)) {
            list = eventRepo.findAll().stream().filter(e -> e instanceof PrivateEvent).toList();
        } else if ("public".equalsIgnoreCase(type)) {
            list = eventRepo.findAll().stream().filter(e -> e instanceof PublicEvent).toList();
        } else {
            list = eventRepo.findAll();
        }

        List<Map<String, Object>> out = new ArrayList<>();
        for (Event e : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", e.getId());
            m.put("eventName", e.getEventName());
            m.put("date", e.getDate());
            m.put("eventType", e instanceof PublicEvent ? "Public Event" : (e instanceof PrivateEvent ? "Private Event" : "Event"));
            m.put("status", e.getStatus().name());
            m.put("pokecenter", e.getPokecenter() != null ? e.getPokecenter().getId() : null);
            out.add(m);
        }
        out.sort((a, b) -> String.valueOf(a.get("date")).compareTo(String.valueOf(b.get("date"))));
        return out;
    }
}
