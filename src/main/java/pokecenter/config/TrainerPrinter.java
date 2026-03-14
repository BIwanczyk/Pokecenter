package pokecenter.config;

import pokecenter.dto.*;
import pokecenter.model.*;
import pokecenter.repository.*;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class TrainerPrinter implements CommandLineRunner {

    private final TrainerRepository trainerRepository;
    private final RegistrationRepository registrationRepository;

    public TrainerPrinter(TrainerRepository trainerRepository,
                          RegistrationRepository registrationRepository) {
        this.trainerRepository = trainerRepository;
        this.registrationRepository = registrationRepository;
    }

    @Override
    public void run(String... args) {
        trainerRepository.findByEmail("ash.ketchum@pallet.com").ifPresentOrElse(trainer -> {
            System.out.println("Znaleziony trener: " + trainer.getFullName());
            System.out.println("Rejestracje w Pokecentrach:");
            registrationRepository.findByTrainerId(trainer.getId()).forEach(r ->
                    System.out.println("- " + r.getPokecenter().getLocation()
                            + " (od " + r.getRegistrationDate() + ")")
            );
        }, () -> {
            System.out.println("Brak trenera o podanym emailu.");
        });
    }
}