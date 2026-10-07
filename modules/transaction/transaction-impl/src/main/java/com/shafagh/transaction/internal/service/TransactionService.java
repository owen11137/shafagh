package com.shafagh.transaction.internal.service;
import com.shafagh.transaction.api.*;
import com.shafagh.transaction.internal.entity.BankTransaction;
import com.shafagh.transaction.internal.repository.BankTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
@Service
public class TransactionService implements TransactionApi {
 @jakarta.persistence.PersistenceContext(unitName="transaction")
 private jakarta.persistence.EntityManager entityManager;
 
 @Transactional(propagation=Propagation.MANDATORY)
 public void record(RecordTransactionCommand c) {
  var t=new BankTransaction();t.operationId=c.operationId();t.accountId=c.accountId();t.amount=c.amount();
  entityManager.persist(t);entityManager.flush();
 }
}
