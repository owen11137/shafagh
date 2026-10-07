package com.shafagh.cif.internal.dto.response;
import com.shafagh.cif.api.CustomerResponse;
public record CustomerHttpResponse(String id, String name) {
 public static CustomerHttpResponse from(CustomerResponse value) {return new CustomerHttpResponse(value.id(),value.name());}
}
