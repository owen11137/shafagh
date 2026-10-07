package com.shafagh.dpst.internal.entity;
import jakarta.persistence.*;
@Entity @Table(name="DEMO_ACCOUNT")
public class DepositAccount {
 @Id @Column(length=36) public String id;
 @Column(name="CUSTOMER_ID",nullable=false,length=36) public String customerId;
 @Column(nullable=false,precision=19,scale=2) public java.math.BigDecimal balance;
 @Version public long version;
 public DepositAccount() {}
}
