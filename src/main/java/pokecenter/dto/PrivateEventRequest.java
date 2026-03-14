
package pokecenter.dto;

import pokecenter.model.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record PrivateEventRequest(
        @NotBlank(message = "eventName is required")
        String eventName,

        @NotNull(message = "date is required")
        LocalDate date,

        @Min(value = 1, message = "maxParticipants must be >= 1")
        Integer maxParticipants,

        @Positive(message = "pokecenterId must be positive")
        Long pokecenterId
) {}