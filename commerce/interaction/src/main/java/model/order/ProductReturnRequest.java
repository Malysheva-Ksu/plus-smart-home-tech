package model.order;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

public record ProductReturnRequest(
        @NotNull
        UUID orderId,

        @NotEmpty
        Map<UUID, Integer> products
) {
}