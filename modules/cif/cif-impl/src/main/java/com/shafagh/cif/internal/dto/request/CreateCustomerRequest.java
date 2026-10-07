package com.shafagh.cif.internal.dto.request;
import jakarta.validation.constraints.*;
public record CreateCustomerRequest(@NotBlank @Size(max=100) String name) {}
