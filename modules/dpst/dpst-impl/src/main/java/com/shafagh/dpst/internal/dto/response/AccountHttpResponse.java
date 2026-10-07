package com.shafagh.dpst.internal.dto.response;
import com.shafagh.dpst.api.AccountResponse;
public record AccountHttpResponse(String id, String customerId, java.math.BigDecimal balance) {
 public static AccountHttpResponse from(AccountResponse value) {return new AccountHttpResponse(value.id(),value.customerId(),value.balance());}
}
