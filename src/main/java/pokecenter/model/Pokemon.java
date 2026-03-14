package pokecenter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.*;

@Entity
public class Pokemon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int baseHP;
    private int level;

    @ElementCollection
    private List<String> types = new ArrayList<>();

    private int totalHP;

    @ManyToMany(mappedBy = "pokemons")
    @JsonIgnore
    private Set<Team> teams = new HashSet<>();

    @ManyToMany(mappedBy = "caughtPokemons")
    @JsonIgnore
    private Set<Trainer> caughtByTrainers = new HashSet<>();

    @ManyToMany(mappedBy = "activeTeamPokemons")
    @JsonIgnore
    private Set<Trainer> activeForTrainers = new HashSet<>();

    public Pokemon() {}

    public Pokemon(String name, int baseHP, int level, List<String> types) {
        this.name = name;
        this.baseHP = baseHP;
        this.level = level;
        this.types = (types != null) ? new ArrayList<>(types) : new ArrayList<>();
        this.totalHP = calculateTotalHP();
    }

    public int calculateTotalHP() {
        return baseHP + (level * 5);
    }

    @PrePersist
    @PreUpdate
    public void syncHP() {
        this.totalHP = calculateTotalHP();
    }

    public Long getId() { return id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public int getBaseHP() { return baseHP; }

    public void setBaseHP(int baseHP) {
        this.baseHP = baseHP;
        this.totalHP = calculateTotalHP();
    }

    public int getLevel() { return level; }

    public void setLevel(int level) {
        this.level = level;
        this.totalHP = calculateTotalHP();
    }

    public List<String> getTypes() { return types; }

    public void setTypes(List<String> types) {
        this.types = (types != null) ? new ArrayList<>(types) : new ArrayList<>();
    }

    public int getTotalHP() { return totalHP; }

    public Set<Team> getTeams() { return teams; }

    public void setTeams(Set<Team> teams) {
        this.teams = (teams != null) ? teams : new HashSet<>();
    }

    public void addType(String type) {
        if (type != null) this.types.add(type);
    }

    public void addToTeam(Team team) {
        if (team != null) this.teams.add(team);
    }

    void _addCatchingTrainer(Trainer trainer) {
        caughtByTrainers.add(trainer);
    }

    void _removeCatchingTrainer(Trainer trainer) {
        caughtByTrainers.remove(trainer);
    }

    void _addActiveTrainer(Trainer trainer) {
        activeForTrainers.add(trainer);
    }

    void _removeActiveTrainer(Trainer trainer) {
        activeForTrainers.remove(trainer);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pokemon)) return false;
        Pokemon other = (Pokemon) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
