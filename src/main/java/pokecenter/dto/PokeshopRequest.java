
package pokecenter.dto;

import pokecenter.model.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Set;

public record PokeshopRequest(
        @NotBlank(message = "name is required")
        String name,

        @NotBlank(message = "managerName is required")
        String managerName,

        @NotBlank(message = "phoneNumber is required")
        String phoneNumber,

        @NotNull(message = "pokecenterId is required")
        @Positive(message = "pokecenterId must be positive")
        Long pokecenterId,

        @NotEmpty(message = "shopTypes must contain at least one role")
        Set<ShopType> shopTypes,

        @Min(value = 0, message = "pokeballVarieties must be >= 0")
        Integer pokeballVarieties,

        @Min(value = 0, message = "medicalItemsCount must be >= 0")
        Integer medicalItemsCount
) {}