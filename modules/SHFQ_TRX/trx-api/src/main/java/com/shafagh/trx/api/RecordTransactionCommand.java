package com.shafagh.trx.api;
import java.math.BigDecimal;
public record RecordTransactionCommand(String operationId, String accountId, BigDecimal amount) {}
