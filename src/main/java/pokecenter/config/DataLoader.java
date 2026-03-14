package pokecenter.config;

import pokecenter.dto.*;
import pokecenter.model.*;
import pokecenter.repository.*;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Profile("dev")
@Order(1)
public class DataLoader implements CommandLineRunner {

    private final PokecenterRepository pokecenterRepo;
    private final TrainerRepository trainerRepo;
    private final PokeshopRepository pokeshopRepo;
    private final RegistrationRepository registrationRepo;
    private final NurseRepository nurseRepo;
    private final ShopAssistantRepository shopAssistantRepo;
    private final EventOrganizerRepository eventOrganizerRepo;
    private final PokemonRepository pokemonRepo;
    private final TeamRepository teamRepo;
    private final PrivateEventRepository privateEventRepo;
    private final PublicEventRepository publicEventRepo;

    public DataLoader(PokecenterRepository pokecenterRepo,
                      TrainerRepository trainerRepo,
                      PokeshopRepository pokeshopRepo,
                      RegistrationRepository registrationRepo,
                      NurseRepository nurseRepo,
                      ShopAssistantRepository shopAssistantRepo,
                      EventOrganizerRepository eventOrganizerRepo,
                      PokemonRepository pokemonRepo,
                      TeamRepository teamRepo,
                      PrivateEventRepository privateEventRepo,
                      PublicEventRepository publicEventRepo) {
        this.pokecenterRepo = pokecenterRepo;
        this.trainerRepo = trainerRepo;
        this.pokeshopRepo = pokeshopRepo;
        this.registrationRepo = registrationRepo;
        this.nurseRepo = nurseRepo;
        this.shopAssistantRepo = shopAssistantRepo;
        this.eventOrganizerRepo = eventOrganizerRepo;
        this.pokemonRepo = pokemonRepo;
        this.teamRepo = teamRepo;
        this.privateEventRepo = privateEventRepo;
        this.publicEventRepo = publicEventRepo;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Pokecenter pallet = getOrCreatePokecenter("Pallet Town", "123-456-789", "contact@pallet.com");

        Trainer ash = getOrCreateTrainer(
                "Ash", "Ketchum", 15, "111-222-333",
                true, "ash.ketchum@pallet.com", 8
        );

        Pokeshop mart = getOrCreatePokeshop(
                "PokéMart Pallet",
                "Gary Oak",
                "321-654-987",
                pallet,
                Set.of(ShopType.POKEBALL, ShopType.MEDICAL),
                5,
                10
        );

        getOrCreateRegistration(ash, pallet);

        getOrCreateNurse("Nurse", "Joy", 28, "555-000-111", pallet);

        getOrCreateShopAssistant("Marcus", "Clerk", 24, "555-000-222", pallet, mart);

        getOrCreateEventOrganizer(
                "Brock", "Harrison", 17, "555-000-333",
                false, "brock@pewter.com", 3, pallet
        );

        Pokemon pikachu    = getOrCreatePokemon("Pikachu",    35, 20, List.of("Electric"));
        Pokemon charmander = getOrCreatePokemon("Charmander", 39, 16, List.of("Fire"));
        Pokemon squirtle   = getOrCreatePokemon("Squirtle",   44, 15, List.of("Water"));

        Team ashTeam = getOrCreateTeamForTrainer(ash);
        Set<String> current = ashTeam.getPokemons().stream()
                .map(Pokemon::getName)
                .collect(Collectors.toSet());
        if (current.add(pikachu.getName()))    ashTeam.getPokemons().add(pikachu);
        if (current.add(charmander.getName())) ashTeam.getPokemons().add(charmander);
        teamRepo.save(ashTeam);

        LocalDate today = LocalDate.now();

        getOrCreatePrivateEvent("VIP Meetup",     today.plusDays(7),  30, pallet);
        getOrCreatePrivateEvent("Gym Scrimmage",  today.plusDays(10), 24, pallet);
        getOrCreatePrivateEvent("Elite Practice", today.plusDays(12), 18, pallet);

        getOrCreatePublicEvent("League Qualifier", today.plusDays(14), 64,  800,  pallet);
        getOrCreatePublicEvent("City Cup",         today.plusDays(21), 128, 1200, pallet);
        getOrCreatePublicEvent("League Finals",    today.plusDays(28), 32,  2000, pallet);
    }

    private Pokecenter getOrCreatePokecenter(String location, String phone, String email) {
        return pokecenterRepo.findAll().stream()
                .filter(p -> Objects.equals(p.getLocation(), location))
                .findFirst()
                .orElseGet(() -> pokecenterRepo.save(new Pokecenter(location, phone, email)));
    }

    private Trainer getOrCreateTrainer(String name, String surname, int age, String phone,
                                       Boolean hasInsurance, String email, Integer badges) {
        return trainerRepo.findAll().stream()
                .filter(t -> Objects.equals(t.getEmail(), email))
                .findFirst()
                .orElseGet(() -> trainerRepo.save(new Trainer(name, surname, age, phone, hasInsurance, email, badges)));
    }

    private Pokeshop getOrCreatePokeshop(String name, String managerName, String phone,
                                         Pokecenter pokecenter, Set<ShopType> types,
                                         Integer pokeballVarieties, Integer medicalItemsCount) {
        return pokeshopRepo.findAll().stream()
                .filter(s -> Objects.equals(s.getName(), name) &&
                        s.getPokecenter() != null &&
                        Objects.equals(s.getPokecenter().getId(), pokecenter.getId()))
                .findFirst()
                .orElseGet(() -> pokeshopRepo.save(
                        new Pokeshop(name, managerName, phone, pokecenter, types, pokeballVarieties, medicalItemsCount)
                ));
    }

    private Registration getOrCreateRegistration(Trainer trainer, Pokecenter pokecenter) {
        return registrationRepo
                .findTopByTrainerIdAndPokecenterIdOrderByRegistrationDateDesc(trainer.getId(), pokecenter.getId())
                .orElseGet(() -> registrationRepo.save(new Registration(trainer, pokecenter)));
    }

    private Nurse getOrCreateNurse(String name, String surname, int age, String phone, Pokecenter pc) {
        return nurseRepo.findAll().stream()
                .filter(n -> Objects.equals(n.getPhoneNumber(), phone))
                .findFirst()
                .orElseGet(() -> nurseRepo.save(new Nurse(name, surname, age, phone, pc)));
    }

    private ShopAssistant getOrCreateShopAssistant(String name, String surname, int age, String phone,
                                                   Pokecenter pc, Pokeshop shop) {
        return shopAssistantRepo.findAll().stream()
                .filter(s -> Objects.equals(s.getPhoneNumber(), phone))
                .findFirst()
                .map(existing -> {
                    if (existing.getPokeshop() == null && shop != null) {
                        existing.setPokeshop(shop);
                        return shopAssistantRepo.save(existing);
                    }
                    return existing;
                })
                .orElseGet(() -> shopAssistantRepo.save(new ShopAssistant(name, surname, age, phone, pc, shop)));
    }

    private EventOrganizer getOrCreateEventOrganizer(String name, String surname, int age, String phone,
                                                     boolean hasInsurance, String email, int badgeCount, Pokecenter pc) {
        return eventOrganizerRepo.findAll().stream()
                .filter(e -> Objects.equals(e.getEmail(), email))
                .findFirst()
                .orElseGet(() -> eventOrganizerRepo.save(
                        new EventOrganizer(name, surname, age, phone, hasInsurance, email, badgeCount, pc)
                ));
    }

    private Pokemon getOrCreatePokemon(String name, int baseHP, int level, List<String> types) {
        return pokemonRepo.findAll().stream()
                .filter(p -> Objects.equals(p.getName(), name))
                .findFirst()
                .orElseGet(() -> pokemonRepo.save(new Pokemon(name, baseHP, level, types)));
    }

    private Team getOrCreateTeamForTrainer(Trainer trainer) {
        return teamRepo.findAll().stream()
                .filter(t -> t.getTrainer() != null && Objects.equals(t.getTrainer().getId(), trainer.getId()))
                .findFirst()
                .orElseGet(() -> teamRepo.save(new Team(trainer)));
    }

    private PrivateEvent getOrCreatePrivateEvent(String eventName, LocalDate date, int invitedCount) {
        return privateEventRepo.findAll().stream()
                .filter(e -> Objects.equals(e.getEventName(), eventName) && Objects.equals(e.getDate(), date))
                .findFirst()
                .orElseGet(() -> privateEventRepo.save(new PrivateEvent(eventName, date, invitedCount)));
    }

    private PublicEvent getOrCreatePublicEvent(String eventName, LocalDate date, int maxParticipants, int maxAudience) {
        return publicEventRepo.findAll().stream()
                .filter(e -> Objects.equals(e.getEventName(), eventName) && Objects.equals(e.getDate(), date))
                .findFirst()
                .orElseGet(() -> publicEventRepo.save(new PublicEvent(eventName, date, maxParticipants, maxAudience)));
    }

    private PrivateEvent getOrCreatePrivateEvent(String eventName, LocalDate date, int maxParticipants, Pokecenter pc) {
        return privateEventRepo.findAll().stream()
                .filter(e -> Objects.equals(e.getEventName(), eventName) && Objects.equals(e.getDate(), date))
                .findFirst()
                .map(e -> {
                    if (e.getPokecenter() == null && pc != null) {
                        e.setPokecenter(pc);
                        return privateEventRepo.save(e);
                    }
                    return e;
                })
                .orElseGet(() -> {
                    PrivateEvent ev = new PrivateEvent(eventName, date, maxParticipants);
                    ev.setPokecenter(pc);
                    return privateEventRepo.save(ev);
                });
    }

    private PublicEvent getOrCreatePublicEvent(String eventName, LocalDate date, int maxParticipants, int maxAudience, Pokecenter pc) {
        return publicEventRepo.findAll().stream()
                .filter(e -> Objects.equals(e.getEventName(), eventName) && Objects.equals(e.getDate(), date))
                .findFirst()
                .map(e -> {
                    if (e.getPokecenter() == null && pc != null) {
                        e.setPokecenter(pc);
                        return publicEventRepo.save(e);
                    }
                    return e;
                })
                .orElseGet(() -> {
                    PublicEvent ev = new PublicEvent(eventName, date, maxParticipants, maxAudience);
                    ev.setPokecenter(pc);
                    return publicEventRepo.save(ev);
                });
    }
}