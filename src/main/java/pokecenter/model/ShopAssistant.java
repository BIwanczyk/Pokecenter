package pokecenter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@DiscriminatorValue("SHOP_ASSISTANT")
public class ShopAssistant extends Employee {

    

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pokeshop_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    @NotNull
    private Pokeshop pokeshop;

    public ShopAssistant() {}

    public ShopAssistant(String name, String surname, int age, String phoneNumber, Pokecenter pokecenter, Pokeshop pokeshop) {
        super(name, surname, age, phoneNumber, pokecenter);
        if (pokeshop == null) throw new IllegalArgumentException("ShopAssistant must be assigned to a Pokeshop.");
        this.pokeshop = pokeshop;
    }

    public ShopAssistant(Employee old, Pokeshop pokeshop) {
        super(old);
        if (pokeshop == null) throw new IllegalArgumentException("ShopAssistant must be assigned to a Pokeshop.");
        this.pokeshop = pokeshop;
    }

    public Pokeshop getPokeshop() {
        return pokeshop;
    }

    public void setPokeshop(Pokeshop pokeshop) {
        if (pokeshop == null) throw new IllegalArgumentException("pokeshop cannot be null for ShopAssistant.");
        this.pokeshop = pokeshop;
    }

    public void sellItem() {}

    @Override
    public String getRoleDescription() {
        String pc = (getPokecenter() != null ? getPokecenter().getLocation() : "No Pokecenter assigned");
        String shop = (pokeshop != null ? pokeshop.getName() : "No Pokeshop assigned");
        return "Shop Assistant at Pokecenter: " + pc + " (Shop: " + shop + ")";
    }
}
