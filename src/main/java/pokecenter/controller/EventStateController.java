package pokecenter.controller;

import pokecenter.dto.*;
import pokecenter.model.*;
import pokecenter.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/events")
public class EventStateController {

    private final EventRepository eventRepo;

    public EventStateController(EventRepository eventRepo) {
        this.eventRepo = eventRepo;
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<?> status(@PathVariable Long id) {
        Event ev = eventRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        return ResponseEntity.ok(java.util.Map.of(
                "eventId", ev.getId(),
                "status", ev.getStatus().name()
        ));
    }

    @PutMapping("/{id}/open-registrations")
    public ResponseEntity<?> openRegistrations(@PathVariable Long id) {
        Event ev = get(id);
        try {
            ev.openRegistrations();
            return ResponseEntity.ok(eventRepo.save(ev));
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @PutMapping("/{id}/close-registrations")
    public ResponseEntity<?> closeRegistrations(@PathVariable Long id) {
        Event ev = get(id);
        try {
            ev.closeRegistrations();
            return ResponseEntity.ok(eventRepo.save(ev));
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @PutMapping("/{id}/start")
    public ResponseEntity<?> start(@PathVariable Long id) {
        Event ev = get(id);
        try {
            ev.startEvent();
            return ResponseEntity.ok(eventRepo.save(ev));
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @PutMapping("/{id}/finish")
    public ResponseEntity<?> finish(@PathVariable Long id) {
        Event ev = get(id);
        try {
            ev.finishEvent();
            return ResponseEntity.ok(eventRepo.save(ev));
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        Event ev = get(id);
        try {
            ev.cancelEvent();
            return ResponseEntity.ok(eventRepo.save(ev));
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    private Event get(Long id) {
        return eventRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
    }
}