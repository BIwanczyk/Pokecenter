package pokecenter.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

@Entity
@DiscriminatorValue("NURSE")
public class Nurse extends Employee {

    public Nurse() {}

    public Nurse(String name, String surname, int age, String phoneNumber, Pokecenter pokecenter) {
        super(name, surname, age, phoneNumber, pokecenter);
    }

    public Nurse(Employee old) {
        super(old);
    }

    public void healPokemon() {
    }

    @Override
    @Transient
    public String getRoleDescription() {
        return "Nurse at Pokecenter: " +
                (getPokecenter() != null ? getPokecenter().getLocation() : "No Pokecenter assigned");
    }
}
