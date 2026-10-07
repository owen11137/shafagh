package com.shafagh.trx.internal.repository;
import com.shafagh.trx.internal.entity.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
public interface BankTransactionRepository extends JpaRepository<BankTransaction,String> {  }
