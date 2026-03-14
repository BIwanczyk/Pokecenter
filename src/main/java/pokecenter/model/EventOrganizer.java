package pokecenter.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@DiscriminatorValue("EventOrganizer")
public class EventOrganizer extends Employee implements ITrainer {

    @NotNull
    private Boolean hasInsurance;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @Min(0)
    @Max(109)
    private Integer badgeCount;

    public EventOrganizer() {}

    public EventOrganizer(String name, String surname, int age, String phoneNumber,
                          boolean hasInsurance, String email, Integer badgeCount,
                          Pokecenter pokecenter) {
        super(name, surname, age, phoneNumber, pokecenter);
        this.hasInsurance = hasInsurance;
        setEmail(email);
        this.badgeCount = badgeCount;
    }

    @Override
    public Boolean getHasInsurance() { return hasInsurance; }

    @Override
    public void setHasInsurance(Boolean hasInsurance) { this.hasInsurance = hasInsurance; }

    @Override
    public String getEmail() { return email; }

    @Override
    public void setEmail(String email) {
        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        this.email = email.trim().toLowerCase();
    }


    @Override
    public Integer getBadgeCount() { return badgeCount; }

    @Override
    public void setBadgeCount(Integer badgeCount) { this.badgeCount = badgeCount; }

    @Override
    @Transient
    public String getRoleDescription() {
        return "Event Organizer at " + (getPokecenter() != null ? getPokecenter().getLocation() : "N/A");
    }
}
