package pokecenter.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

@Entity
@Table(
        name = "registration",
        indexes = {
                @Index(name = "idx_registration_trainer", columnList = "trainer_id"),
                @Index(name = "idx_registration_pokecenter", columnList = "pokecenter_id"),
                @Index(name = "idx_registration_trainer_pokecenter", columnList = "trainer_id,pokecenter_id")
        }
)
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pokecenter_id", nullable = false)
    private Pokecenter pokecenter;

    @PastOrPresent
    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate = LocalDate.now();

    protected Registration() {}

    public Registration(Trainer trainer, Pokecenter pokecenter) {
        this(trainer, pokecenter, LocalDate.now());
    }

    public Registration(Trainer trainer, Pokecenter pokecenter, LocalDate registrationDate) {
        setTrainer(trainer);
        setPokecenter(pokecenter);
        setRegistrationDate(registrationDate);
    }

    public Long getId() { return id; }
    public Trainer getTrainer() { return trainer; }
    public Pokecenter getPokecenter() { return pokecenter; }
    public LocalDate getRegistrationDate() { return registrationDate; }

    public void setTrainer(Trainer trainer) {
        if (trainer == null) throw new IllegalArgumentException("Trainer cannot be null.");
        if (this.trainer == trainer) return;

        // unlink old
        if (this.trainer != null) {
            this.trainer._removeRegistration(this);
        }

        // link new
        this.trainer = trainer;
        trainer._addRegistration(this);
    }

    public void setPokecenter(Pokecenter pokecenter) {
        if (pokecenter == null) throw new IllegalArgumentException("Pokecenter cannot be null.");
        if (this.pokecenter == pokecenter) return;

        // unlink old
        if (this.pokecenter != null) {
            this.pokecenter._removeRegistration(this);
        }

        // link new
        this.pokecenter = pokecenter;
        pokecenter._addRegistration(this);
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        if (registrationDate == null) throw new IllegalArgumentException("Registration date cannot be null.");
        if (registrationDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Registration date cannot be in the future.");
        }
        this.registrationDate = registrationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Registration other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Registration.class.hashCode();
    }
}
