package edu.cit.besanez.cookbook.ingredient;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientRequestDTO {

    @NotBlank(message = "Ingredient name is required")
    @Size(max = 150, message = "Ingredient name must not exceed 150 characters")
    private String name;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be 0 or greater")
    private Integer quantity;

    private IngredientUnit unit;

    @Size(max = 50, message = "Notes must not exceed 50 characters")
    private String notes;
}