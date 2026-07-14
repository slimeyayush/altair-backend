package com.example.demo.DTO.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderStatusDTO {
    @NotBlank(message = "Status cannot be blank")
    private String status;
}
