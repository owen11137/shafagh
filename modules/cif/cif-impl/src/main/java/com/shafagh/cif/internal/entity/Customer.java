package com.shafagh.cif.internal.entity;
import jakarta.persistence.*;
@Entity @Table(name="DEMO_CUSTOMER")
public class Customer {
 @Id @Column(length=36) public String id;
 @Column(nullable=false,length=100) public String name;
 public Customer() {}
}
