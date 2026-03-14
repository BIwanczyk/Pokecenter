
package pokecenter.dto;

import pokecenter.model.*;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PokecenterRequest(
        @NotBlank(message = "location is required")
        String location,

        @NotBlank(message = "phoneNumber is required")
        String phoneNumber,

        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid email address")
        String email
) {}