
package pokecenter.dto;

import pokecenter.model.*;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RegistrationRequest(
        @NotNull(message = "trainerId is required")
        @Positive(message = "trainerId must be positive")
        Long trainerId,

        @NotNull(message = "pokecenterId is required")
        @Positive(message = "pokecenterId must be positive")
        Long pokecenterId
) {}