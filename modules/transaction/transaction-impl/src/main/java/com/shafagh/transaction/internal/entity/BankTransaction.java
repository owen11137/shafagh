package com.shafagh.transaction.internal.entity;
import jakarta.persistence.*;
@Entity @Table(name="DEMO_BANK_TRANSACTION")
public class BankTransaction {
 @Id @Column(name="OPERATION_ID",length=36) public String operationId;
 @Column(name="ACCOUNT_ID",nullable=false,length=36) public String accountId;
 @Column(nullable=false,precision=19,scale=2) public java.math.BigDecimal amount;
 public BankTransaction() {}
}
