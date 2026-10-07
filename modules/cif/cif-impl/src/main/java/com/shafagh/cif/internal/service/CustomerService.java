package com.shafagh.cif.internal.service;
import com.shafagh.cif.api.*;
import com.shafagh.cif.internal.entity.Customer;
import com.shafagh.cif.internal.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
@Service @Transactional
public class CustomerService implements CustomerApi {
 private final CustomerRepository repository;
 public CustomerService(CustomerRepository repository) { this.repository=repository; }
 public CustomerResponse create(String name) {
  var customer=new Customer();customer.id=UUID.randomUUID().toString();customer.name=name;
  repository.save(customer);return new CustomerResponse(customer.id,customer.name);
 }
 public CustomerResponse requireCustomer(String id) {
  var c=repository.findById(id).orElseThrow(()->new IllegalArgumentException("Customer not found"));
  return new CustomerResponse(c.id,c.name);
 }
}
