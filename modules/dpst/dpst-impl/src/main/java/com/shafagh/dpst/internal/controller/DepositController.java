package com.shafagh.dpst.internal.controller;
import com.shafagh.dpst.internal.dto.response.AccountHttpResponse;
import com.shafagh.dpst.internal.dto.request.*;
import com.shafagh.dpst.internal.service.DepositService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/accounts")
public class DepositController {
 private final DepositService service;
 public DepositController(DepositService service) {this.service=service;}
 @PostMapping public AccountHttpResponse open(@Valid @RequestBody OpenAccountRequest request) {return AccountHttpResponse.from(service.open(request.customerId()));}
 @GetMapping("/{id}") public AccountHttpResponse get(@PathVariable String id) {return AccountHttpResponse.from(service.account(id));}
 @PostMapping("/{id}/credits") public AccountHttpResponse credit(@PathVariable String id,@Valid @RequestBody CreditRequest request) {return AccountHttpResponse.from(service.credit(id,request.operationId(),request.amount()));}
}
