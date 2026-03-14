package pokecenter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Trainer extends Person implements ITrainer {

    @NotNull
    private Boolean hasInsurance;

    @Column(nullable = false, unique = true)
    @Email
    @NotBlank
    private String email;

    @Min(0)
    @Max(109)
    private Integer badgeCount;

    @ManyToMany(mappedBy = "participantsByRegistrationNumber")
    @JsonIgnore
    private Set<Event> registeredEvents = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "trainer_caught_pokemon",
            joinColumns = @JoinColumn(name = "trainer_id"),
            inverseJoinColumns = @JoinColumn(name = "pokemon_id")
    )
    @JsonIgnore
    private Set<Pokemon> caughtPokemons = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "trainer_active_pokemon",
            joinColumns = @JoinColumn(name = "trainer_id"),
            inverseJoinColumns = @JoinColumn(name = "pokemon_id")
    )
    @JsonIgnore
    private Set<Pokemon> activeTeamPokemons = new HashSet<>();

    @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Registration> registrations = new HashSet<>();

    public Trainer() {}

    public Trainer(String name, String surname, int age, String phoneNumber,
                   Boolean hasInsurance, String email, Integer badgeCount) {
        super(name, surname, age, phoneNumber);
        this.hasInsurance = hasInsurance;
        setEmail(email);
        this.badgeCount = badgeCount;
    }

    public Boolean hasInsurance() { return hasInsurance; }
    public Boolean getHasInsurance() { return hasInsurance; }

    public String getEmail() { return email; }
    public Integer getBadgeCount() { return badgeCount; }

    public Set<Event> getRegisteredEvents() {
        return Collections.unmodifiableSet(registeredEvents);
    }

    public Set<Pokemon> getCaughtPokemons() { return Collections.unmodifiableSet(caughtPokemons); }
    public Set<Pokemon> getActiveTeamPokemons() { return Collections.unmodifiableSet(activeTeamPokemons); }

    public Set<Registration> getRegistrations() {
        return Collections.unmodifiableSet(registrations);
    }

    public void setEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null.");
        }
        String normalized = email.trim().toLowerCase();
        if (!normalized.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        this.email = normalized;
    }

    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setHasInsurance(Boolean hasInsurance) { this.hasInsurance = hasInsurance; }

    public void setBadgeCount(Integer badgeCount) {
        if (badgeCount != null && (badgeCount < 0 || badgeCount > 109)) {
            throw new IllegalArgumentException("Badge count must be between 0 and 109.");
        }
        this.badgeCount = badgeCount;
    }

    public void setName(String name) { this.name = name; }
    public void setSurname(String surname) { this.surname = surname; }
    public void setAge(int age) { this.age = age; }


    void _addRegisteredEvent(Event event) { if (event != null) registeredEvents.add(event); }
    void _removeRegisteredEvent(Event event) { if (event != null) registeredEvents.remove(event); }

    void _addRegistration(Registration r) {
        if (r != null) registrations.add(r);
    }

    void _removeRegistration(Registration r) {
        if (r != null) registrations.remove(r);
    }

    //Pokemon logic

    public void catchPokemon(Pokemon pokemon) {
        if (pokemon == null) throw new IllegalArgumentException("Pokemon cannot be null.");
        if (caughtPokemons.add(pokemon)) pokemon._addCatchingTrainer(this);
    }

    public void releasePokemon(Pokemon pokemon) {
        if (pokemon == null) return;

        if (activeTeamPokemons.remove(pokemon)) pokemon._removeActiveTrainer(this);
        if (caughtPokemons.remove(pokemon)) pokemon._removeCatchingTrainer(this);
    }

    public void addToActiveTeam(Pokemon pokemon) {
        if (pokemon == null) throw new IllegalArgumentException("Pokemon cannot be null.");
        if (!caughtPokemons.contains(pokemon)) {
            throw new IllegalStateException("Subset rule violated: Pokemon must be caught before adding to active team.");
        }
        if (!activeTeamPokemons.contains(pokemon) && activeTeamPokemons.size() >= 6) {
            throw new IllegalStateException("Active team cannot contain more than 6 Pokemons.");
        }
        if (activeTeamPokemons.add(pokemon)) pokemon._addActiveTrainer(this);
    }

    public void removeFromActiveTeam(Pokemon pokemon) {
        if (pokemon == null) return;
        if (activeTeamPokemons.remove(pokemon)) pokemon._removeActiveTrainer(this);
    }

    @PrePersist
    @PreUpdate
    private void validateSubsetInvariant() {
        if (activeTeamPokemons.size() > 6) {
            throw new IllegalStateException("Active team cannot contain more than 6 Pokemons.");
        }
        if (!caughtPokemons.containsAll(activeTeamPokemons)) {
            throw new IllegalStateException("Subset rule violated: activeTeamPokemons must be a subset of caughtPokemons.");
        }
    }

    @Override
    public double calculateMonthlyCost() {
        int eventCount = 0;
        double cost = eventCount * 50;
        if (Boolean.TRUE.equals(hasInsurance)) cost *= 0.7;
        return cost;
    }

    @Override
    public String getRoleDescription() {
        return "Trainer with " + (badgeCount != null ? badgeCount : 0) + " badges";
    }

    @Override
    public String toString() {
        return super.toString() + ", Email: " + email + ", Insurance: " +
                (Boolean.TRUE.equals(hasInsurance) ? "Yes" : "No");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Trainer)) return false;
        Trainer other = (Trainer) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Trainer.class.hashCode();
    }
}
