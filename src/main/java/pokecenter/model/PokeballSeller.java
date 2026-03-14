package pokecenter.model;

public interface PokeballSeller {
    void sellPokeball();
    void sellUltraBall();
    double calculatePokeballRevenue();
    void offerPokeballDiscount();
}