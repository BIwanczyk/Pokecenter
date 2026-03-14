package pokecenter.dto;

import pokecenter.model.*;

import java.time.LocalDate;

public record RegistrationDto(
        Long id,
        Long trainerId,
        Long pokecenterId,
        String trainerName,
        String pokecenterName,
        LocalDate registrationDate
) {}