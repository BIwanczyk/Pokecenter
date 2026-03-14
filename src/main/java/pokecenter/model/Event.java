package pokecenter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String eventName;

    @NotNull
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private EventStatus status = EventStatus.CREATED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pokecenter_id")
    @JsonIgnore
    private Pokecenter pokecenter;

    @ManyToMany
    @JoinTable(
            name = "event_participants",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "trainer_id"),
            uniqueConstraints = {
                    @UniqueConstraint(columnNames = {"event_id", "registration_number"}),
                    @UniqueConstraint(columnNames = {"event_id", "trainer_id"})
            }
    )
    @MapKeyColumn(name = "registration_number", nullable = false)
    @JsonIgnore
    private Map<Integer, Trainer> participantsByRegistrationNumber = new HashMap<>();

    public Event() {}

    public Event(String eventName, LocalDate date) {
        this.eventName = eventName;
        this.date = date;
        this.status = EventStatus.CREATED;
    }

    public abstract String getEventType();

    public Long getId() { return id; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public EventStatus getStatus() {
        return status == null ? EventStatus.CREATED : status;
    }

    private void setStatus(EventStatus newStatus) {
        this.status = newStatus;
    }

    @JsonIgnore
    public Pokecenter getPokecenter() { return pokecenter; }
    public void setPokecenter(Pokecenter pokecenter) { this.pokecenter = pokecenter; }

    @JsonProperty("pokecenterId")
    @Transient
    public Long getPokecenterId() {
        return pokecenter != null ? pokecenter.getId() : null;
    }

    public void openRegistrations() {
        requireStatus(EventStatus.CREATED, "open registrations");
        setStatus(EventStatus.OPEN_FOR_REGISTRATION);
    }

    public void closeRegistrations() {
        requireStatus(EventStatus.OPEN_FOR_REGISTRATION, "close registrations");
        setStatus(EventStatus.REGISTRATION_CLOSED);
    }

    public void startEvent() {
        requireStatus(EventStatus.REGISTRATION_CLOSED, "start event");
        setStatus(EventStatus.IN_PROGRESS);
    }

    public void finishEvent() {
        requireStatus(EventStatus.IN_PROGRESS, "finish event");
        setStatus(EventStatus.FINISHED);
    }

    public void cancelEvent() {
        requireStatus(EventStatus.CREATED, "cancel event");
        setStatus(EventStatus.CANCELLED);
    }

    public boolean isRegistrationOpen() {
        return getStatus() == EventStatus.OPEN_FOR_REGISTRATION;
    }


    //strażnik stanów
    private void requireStatus(EventStatus expected, String action) {
        EventStatus current = getStatus();
        if (current != expected) {
            throw new IllegalStateException(
                    "Cannot " + action + " when event status is " + current + " (expected " + expected + ")"
            );
        }
    }

    private void requireRegistrationOpen(String action) {
        if (!isRegistrationOpen()) {
            throw new IllegalStateException(
                    "Cannot " + action + " when registrations are not open. Current status: " + getStatus()
            );
        }
    }

    //zabezpieczenie przed zapisaniem eventu bez statusu
    @PrePersist
    private void ensureStatusInitialized() {
        if (status == null) status = EventStatus.CREATED;
    }

   //readonly
    public Map<Integer, Trainer> getParticipantsByRegistrationNumber() {
        return Collections.unmodifiableMap(participantsByRegistrationNumber);
    }

    public Trainer getParticipantByRegistrationNumber(int registrationNumber) {
        return participantsByRegistrationNumber.get(registrationNumber);
    }

    public boolean isTrainerRegistered(Trainer trainer) {
        return getRegistrationNumberForTrainer(trainer) != null;
    }

    //kwalifikowana
    public int registerTrainer(Trainer trainer) {
        if (trainer == null) throw new IllegalArgumentException("Trainer cannot be null.");

        Integer existing = getRegistrationNumberForTrainer(trainer);
        if (existing != null) {
            return existing;
        }

        requireRegistrationOpen("register trainer");
        requireCapacityAvailable();

        int regNo = nextRegistrationNumber();
        addTrainerWithRegistrationNumber(regNo, trainer);
        return regNo;
    }

    public void addTrainerWithRegistrationNumber(int registrationNumber, Trainer trainer) {
        if (trainer == null) throw new IllegalArgumentException("Trainer cannot be null.");
        if (registrationNumber <= 0) throw new IllegalArgumentException("registrationNumber must be > 0");

        requireRegistrationOpen("register trainer");

        if (participantsByRegistrationNumber.containsKey(registrationNumber)) {
            throw new IllegalStateException("Registration number already used in this event: " + registrationNumber);
        }

        if (getRegistrationNumberForTrainer(trainer) != null) {
            throw new IllegalStateException("Trainer is already registered for this event.");
        }

        requireCapacityAvailable();

        participantsByRegistrationNumber.put(registrationNumber, trainer);
        trainer._addRegisteredEvent(this);
    }

    private void requireCapacityAvailable() {
        int limit = getMaxParticipantsLimit();
        if (limit > 0 && participantsByRegistrationNumber.size() >= limit) {
            throw new IllegalStateException("Event is full (max participants: " + limit + ").");
        }
    }

    private int getMaxParticipantsLimit() {
        if (this instanceof PrivateEvent pe) {
            return pe.getMaxParticipants();
        }
        if (this instanceof PublicEvent pu) {
            return pu.getMaxParticipants();
        }
        return 0;
    }



    public void unregisterTrainer(Trainer trainer) {
        if (trainer == null) return;

        requireRegistrationOpen("unregister trainer");

        Integer regNo = getRegistrationNumberForTrainer(trainer);
        if (regNo != null) {
            participantsByRegistrationNumber.remove(regNo);
            trainer._removeRegisteredEvent(this);
        }
    }


    public void unregisterTrainerByRegistrationNumber(int registrationNumber) {
        requireRegistrationOpen("unregister trainer");
        Trainer trainer = participantsByRegistrationNumber.remove(registrationNumber);
        if (trainer != null) {
            trainer._removeRegisteredEvent(this);
        }
    }


    public void changeRegistrationNumber(int oldRegistrationNumber, int newRegistrationNumber) {
        requireRegistrationOpen("change registration number");
        if (oldRegistrationNumber == newRegistrationNumber) return;
        if (newRegistrationNumber <= 0) throw new IllegalArgumentException("registrationNumber must be > 0");

        Trainer trainer = participantsByRegistrationNumber.get(oldRegistrationNumber);
        if (trainer == null) {
            throw new IllegalStateException("No participant under registration number: " + oldRegistrationNumber);
        }
        if (participantsByRegistrationNumber.containsKey(newRegistrationNumber)) {
            throw new IllegalStateException("Registration number already used in this event: " + newRegistrationNumber);
        }

        participantsByRegistrationNumber.remove(oldRegistrationNumber);
        participantsByRegistrationNumber.put(newRegistrationNumber, trainer);
    }


    public Integer getRegistrationNumberForTrainer(Trainer trainer) {
        if (trainer == null) return null;

        Long trainerId = trainer.getId();

        for (Map.Entry<Integer, Trainer> e : participantsByRegistrationNumber.entrySet()) {
            Trainer val = e.getValue();
            if (val == null) continue;

            if (val == trainer) {
                return e.getKey();
            }

            Long valId = val.getId();
            if (trainerId != null && valId != null && trainerId.equals(valId)) {
                return e.getKey();
            }
        }
        return null;
    }

    private int nextRegistrationNumber() {
        int max = 0;
        for (Integer k : participantsByRegistrationNumber.keySet()) {
            if (k != null && k > max) max = k;
        }
        int next = max + 1;
        while (participantsByRegistrationNumber.containsKey(next)) next++;
        return next;
    }

    @Override
    public String toString() {
        return getEventType() + ": " + eventName + " - " + date;
    }
}
