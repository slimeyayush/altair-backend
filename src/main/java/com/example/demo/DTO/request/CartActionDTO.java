package com.example.demo.DTO.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Currently unused but kept around so any external/legacy code or tests that
 * reference it still compile. Safe to delete once you've confirmed nothing
 * imports it.
 */
@Data
public class CartActionDTO {
    @NotNull
    private Long productId;

    @Min(1)
    private Integer quantity;
}
