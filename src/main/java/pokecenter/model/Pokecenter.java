package pokecenter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Pokecenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String location;

    @NotBlank
    private String phoneNumber;

    @Email
    private String email;

    @OneToMany(mappedBy = "pokecenter", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("pokecenter-employees")
    private Set<Employee> employees = new HashSet<>();

    // kompozycja
    @OneToMany(mappedBy = "pokecenter", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("pokecenter-shops")
    private Set<Pokeshop> shops = new HashSet<>();

    @OneToMany(mappedBy = "pokecenter", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Registration> registrations = new HashSet<>();

    public Pokecenter() {}

    public Pokecenter(String location, String phoneNumber, String email) {
        this.location = location;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public Long getId() { return id; }
    public String getLocation() { return location; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }

    public Set<Employee> getEmployees() { return employees; }
    public Set<Pokeshop> getShops() { return shops; }

    public Set<Registration> getRegistrations() {
        return Collections.unmodifiableSet(registrations);
    }

    public void setLocation(String location) { this.location = location; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setEmail(String email) { this.email = email; }

    public void setEmployees(Set<Employee> employees) { this.employees = employees; }

    public void setShops(Set<Pokeshop> shops) {
        this.shops.clear();
        if (shops != null) {
            for (Pokeshop s : shops) addShop(s);
        }
    }

    void _addRegistration(Registration r) {
        if (r != null) registrations.add(r);
    }

    void _removeRegistration(Registration r) {
        if (r != null) registrations.remove(r);
    }

    public void addShop(Pokeshop shop) {
        if (shop == null) throw new IllegalArgumentException("Shop cannot be null.");

        Pokecenter owner = shop.getPokecenter();
        if (owner != null && owner != this) {
            Long ownerId = owner.getId();
            Long thisId = this.id;
            boolean sameById = ownerId != null && thisId != null && ownerId.equals(thisId);
            if (!sameById) {
                throw new IllegalStateException("Pokeshop is already assigned to another Pokecenter (composition rule).");
            }
        }

        shop._attachToPokecenter(this);
        shops.add(shop);
    }

    public void removeShop(Pokeshop shop) {
        if (shop == null) return;
        shops.remove(shop);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pokecenter other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Pokecenter.class.hashCode();
    }
}
