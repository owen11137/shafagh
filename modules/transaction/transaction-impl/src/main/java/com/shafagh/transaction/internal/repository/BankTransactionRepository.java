package com.shafagh.transaction.internal.repository;
import com.shafagh.transaction.internal.entity.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
public interface BankTransactionRepository extends JpaRepository<BankTransaction,String> {  }
