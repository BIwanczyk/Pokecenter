
package pokecenter.dto;

import pokecenter.model.*;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ChangeToShopAssistantRequest(
        @NotNull(message = "pokeshopId is required")
        @Positive(message = "pokeshopId must be positive")
        Long pokeshopId
) {}