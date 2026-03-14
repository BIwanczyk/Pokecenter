package pokecenter.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Trainer trainer;

    @ManyToMany
    @JoinTable(
            name = "team_pokemon",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "pokemon_id")
    )
    private Set<Pokemon> pokemons = new HashSet<>();

    public Team() { }

    public Team(Trainer trainer) {
        this.trainer = trainer;
    }

    public Long getId() {
        return id;
    }

    public Trainer getTrainer() {
        return trainer;
    }

    public void setTrainer(Trainer trainer) {
        this.trainer = trainer;
    }

    public Set<Pokemon> getPokemons() {
        return pokemons;
    }

    public void setPokemons(Set<Pokemon> pokemons) {
        this.pokemons = (pokemons != null) ? pokemons : new HashSet<>();
    }

    public void addPokemon(Pokemon pokemon) {
        if (pokemon != null) {
            this.pokemons.add(pokemon);
        }
    }

    public void removePokemon(Pokemon pokemon) {
        if (pokemon != null) {
            this.pokemons.remove(pokemon);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Team)) return false;
        Team other = (Team) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
