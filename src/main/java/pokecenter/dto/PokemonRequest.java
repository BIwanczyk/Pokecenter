
package pokecenter.dto;

import pokecenter.model.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PokemonRequest(
        @NotBlank(message = "name is required")
        String name,

        @Min(value = 1, message = "baseHP must be >= 1")
        int baseHP,

        @Min(value = 1, message = "level must be >= 1")
        int level,

        @NotEmpty(message = "types must contain at least one type")
        List<String> types
) {}