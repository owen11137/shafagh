package com.shafagh.dpst.api;
import java.math.BigDecimal;
public record AccountResponse(String id, String customerId, BigDecimal balance) {}
