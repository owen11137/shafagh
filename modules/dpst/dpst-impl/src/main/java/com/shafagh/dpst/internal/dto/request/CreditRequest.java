package com.shafagh.dpst.internal.dto.request;
import jakarta.validation.constraints.*;
public record CreditRequest(@NotBlank @Pattern(regexp="[0-9a-fA-F-]{36}") String operationId, @NotNull @DecimalMin("0.01") @Digits(integer=17,fraction=2) java.math.BigDecimal amount) {}
