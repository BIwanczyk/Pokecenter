package pokecenter.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;
import java.util.*;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Pokeshop implements Serializable, PokeballSeller, MedicalSeller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String managerName;

    @NotBlank
    private String phoneNumber;

    

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pokecenter_id", nullable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Pokecenter pokecenter;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<ShopType> shopTypes = EnumSet.noneOf(ShopType.class);

    private Integer pokeballVarieties;
    private Integer medicalItemsCount;

    private long pokeballsSold;
    private long medicalItemsSold;

    private double pokeballRevenue;
    private double medicalRevenue;

    protected Pokeshop() {
    }

    public Pokeshop(
            String name,
            String managerName,
            String phoneNumber,
            Pokecenter pokecenter,
            Set<ShopType> shopTypes,
            Integer pokeballVarieties,
            Integer medicalItemsCount
    ) {
        this.name = name;
        this.managerName = managerName;
        this.phoneNumber = phoneNumber;
        this.pokecenter = pokecenter;

        this.shopTypes = copyRoles(shopTypes);
        this.pokeballVarieties = pokeballVarieties;
        this.medicalItemsCount = medicalItemsCount;

        validateInvariant();

        this.pokecenter.addShop(this);
    }

    public static Pokeshop create(
            String name,
            String managerName,
            String phoneNumber,
            Pokecenter pokecenter,
            Set<ShopType> shopTypes,
            Integer pokeballVarieties,
            Integer medicalItemsCount
    ) {
        return new Pokeshop(name, managerName, phoneNumber, pokecenter, shopTypes, pokeballVarieties, medicalItemsCount);
    }

    @PrePersist
    @PreUpdate
    private void beforeSave() {
        validateInvariant();
    }

    @PostLoad
    private void afterLoad() {
        if (this.shopTypes == null) {
            this.shopTypes = EnumSet.noneOf(ShopType.class);
        }
    }

    private static Set<ShopType> copyRoles(Set<ShopType> roles) {
        if (roles == null || roles.isEmpty()) {
            return EnumSet.noneOf(ShopType.class);
        }
        return EnumSet.copyOf(roles);
    }

    private void validateInvariant() {
        if (pokecenter == null) {
            throw new IllegalArgumentException("Pokeshop must belong to a Pokecenter.");
        }

        if (shopTypes == null || shopTypes.isEmpty()) {
            throw new IllegalArgumentException("Pokeshop must have at least one role (ShopType).");
        }

        if (shopTypes.contains(ShopType.POKEBALL)) {
            if (pokeballVarieties == null || pokeballVarieties < 1) {
                throw new IllegalArgumentException("POKEBALL role requires pokeballVarieties >= 1.");
            }
        } else {
            if (pokeballVarieties != null) {
                throw new IllegalArgumentException("pokeballVarieties must be null when role POKEBALL is not assigned.");
            }
            if (pokeballsSold != 0 || pokeballRevenue != 0.0) {
                throw new IllegalArgumentException("POKEBALL counters/revenue must be 0 when role POKEBALL is not assigned.");
            }
        }

        if (shopTypes.contains(ShopType.MEDICAL)) {
            if (medicalItemsCount == null || medicalItemsCount < 1) {
                throw new IllegalArgumentException("MEDICAL role requires medicalItemsCount >= 1.");
            }
        } else {
            if (medicalItemsCount != null) {
                throw new IllegalArgumentException("medicalItemsCount must be null when role MEDICAL is not assigned.");
            }
            if (medicalItemsSold != 0 || medicalRevenue != 0.0) {
                throw new IllegalArgumentException("MEDICAL counters/revenue must be 0 when role MEDICAL is not assigned.");
            }
        }
    }

    private void requireRole(ShopType role) {
        if (!shopTypes.contains(role)) {
            throw new IllegalStateException("Shop '" + name + "' does not have role: " + role);
        }
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getManagerName() { return managerName; }
    public String getPhoneNumber() { return phoneNumber; }

    @JsonBackReference("pokecenter-shops")
    public Pokecenter getPokecenter() { return pokecenter; }

    public Set<ShopType> getShopTypes() {
        return Collections.unmodifiableSet(shopTypes);
    }

    public boolean hasRole(ShopType role) {
        return shopTypes.contains(role);
    }

    public int getPokeballVarieties() {
        requireRole(ShopType.POKEBALL);
        return pokeballVarieties;
    }

    public int getMedicalItemsCount() {
        requireRole(ShopType.MEDICAL);
        return medicalItemsCount;
    }

    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    public void setName(String name) { this.name = name; }

    public void setPokecenter(Pokecenter pokecenter) {
        if (pokecenter == null) {
            throw new IllegalArgumentException("Pokeshop must belong to a Pokecenter.");
        }
        if (this.pokecenter != null && this.pokecenter != pokecenter) {
            Long a = this.pokecenter.getId();
            Long b = pokecenter.getId();
            boolean sameById = a != null && b != null && a.equals(b);
            if (!sameById) {
                throw new IllegalStateException("Pokeshop cannot be moved to another Pokecenter (composition).");
            }
        }
        this.pokecenter = pokecenter;
    }

    void _attachToPokecenter(Pokecenter pc) {
        if (pc == null) throw new IllegalArgumentException("Pokecenter cannot be null.");
        if (this.pokecenter != null && this.pokecenter != pc) {
            Long a = this.pokecenter.getId();
            Long b = pc.getId();
            boolean sameById = a != null && b != null && a.equals(b);
            if (!sameById) {
                throw new IllegalStateException("Pokeshop is already assigned to another Pokecenter (composition).");
            }
        }
        this.pokecenter = pc;
    }

    void _detachFromPokecenter() {
        throw new UnsupportedOperationException(
                "Pokeshop cannot be detached from Pokecenter. Remove it from Pokecenter.shops to delete (composition)."
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pokeshop other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Pokeshop.class.hashCode();
    }

    @Override
    public void sellPokeball() {
        requireRole(ShopType.POKEBALL);
        pokeballsSold += 1;
        pokeballRevenue += 200.0;
    }

    @Override
    public void sellUltraBall() {
        requireRole(ShopType.POKEBALL);
        pokeballsSold += 1;
        pokeballRevenue += 600.0;
    }

    @Override
    public double calculatePokeballRevenue() {
        requireRole(ShopType.POKEBALL);
        return pokeballRevenue;
    }

    @Override
    public void offerPokeballDiscount() {
        requireRole(ShopType.POKEBALL);
    }

    @Override
    public void sellMedicalItem() {
        requireRole(ShopType.MEDICAL);
        medicalItemsSold += 1;
        medicalRevenue += 150.0;
    }

    @Override
    public void sellPotion() {
        requireRole(ShopType.MEDICAL);
        medicalItemsSold += 1;
        medicalRevenue += 350.0;
    }

    @Override
    public double calculateMedicalRevenue() {
        requireRole(ShopType.MEDICAL);
        return medicalRevenue;
    }

    @Override
    public void offerMedicalDiscount() {
        requireRole(ShopType.MEDICAL);
    }

    @Override
    public String toString() {
        return name + " (" + shopTypes + ") - Manager: " + managerName;
    }
}
