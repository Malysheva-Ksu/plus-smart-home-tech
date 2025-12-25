package model.delivery;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookedProductsDto {
    @NotNull
    private double deliveryWeight;

    @NotNull
    private double deliveryVolume;

    @NotNull
    private boolean fragile;
}