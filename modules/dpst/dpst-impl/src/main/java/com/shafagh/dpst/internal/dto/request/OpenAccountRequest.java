package com.shafagh.dpst.internal.dto.request;
import jakarta.validation.constraints.*;
public record OpenAccountRequest(@NotBlank @Size(max=36) String customerId) {}
