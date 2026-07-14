package com.example.demo.DTO.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartDeltaDTO {
    @NotNull(message = "Delta is required")
    private Integer delta;

    /** Optional — null for products without variants. */
    private Long variantId;
}
