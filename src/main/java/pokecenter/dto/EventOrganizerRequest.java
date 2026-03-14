
package pokecenter.dto;

import pokecenter.model.*;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EventOrganizerRequest(
        @NotBlank(message = "name is required")
        String name,

        @NotBlank(message = "surname is required")
        String surname,

        @Min(value = 1, message = "age must be >= 1")
        @Max(value = 120, message = "age must be <= 120")
        int age,

        @NotBlank(message = "phoneNumber is required")
        String phoneNumber,

        boolean hasInsurance,

        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid email address")
        String email,

        @Min(value = 0, message = "badgeCount must be >= 0")
        Integer badgeCount,

        @NotNull(message = "pokecenterId is required")
        @Positive(message = "pokecenterId must be positive")
        Long pokecenterId
) {}