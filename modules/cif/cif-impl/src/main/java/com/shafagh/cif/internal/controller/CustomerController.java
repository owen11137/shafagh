package com.shafagh.cif.internal.controller;
import com.shafagh.cif.internal.dto.response.CustomerHttpResponse;
import com.shafagh.cif.internal.dto.request.CreateCustomerRequest;
import com.shafagh.cif.internal.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/customers")
public class CustomerController {
 private final CustomerService service;
 public CustomerController(CustomerService service) {this.service=service;}
 @PostMapping public CustomerHttpResponse create(@Valid @RequestBody CreateCustomerRequest request) {return CustomerHttpResponse.from(service.create(request.name()));}
 @GetMapping("/{id}") public CustomerHttpResponse get(@PathVariable String id) {return CustomerHttpResponse.from(service.requireCustomer(id));}
}
