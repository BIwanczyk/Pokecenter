
package pokecenter.dto;

import pokecenter.model.*;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Set;

public record TeamRequest(
        @NotNull(message = "trainerId is required")
        @Positive(message = "trainerId must be positive")
        Long trainerId,

        Set<Long> pokemonIds
) {}