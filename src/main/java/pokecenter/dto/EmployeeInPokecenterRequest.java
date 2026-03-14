
package pokecenter.dto;

import pokecenter.model.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record EmployeeInPokecenterRequest(
        @NotBlank(message = "name is required")
        String name,

        @NotBlank(message = "surname is required")
        String surname,

        @Min(value = 1, message = "age must be >= 1")
        @Max(value = 120, message = "age must be <= 120")
        int age,

        @NotBlank(message = "phoneNumber is required")
        String phoneNumber
) {}