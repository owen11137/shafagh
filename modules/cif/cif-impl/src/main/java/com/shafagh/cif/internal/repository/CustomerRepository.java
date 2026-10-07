package com.shafagh.cif.internal.repository;
import com.shafagh.cif.internal.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CustomerRepository extends JpaRepository<Customer,String> {  }
