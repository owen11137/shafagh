package com.shafagh.trx.internal.service;
import com.shafagh.trx.api.*;
import com.shafagh.trx.internal.entity.BankTransaction;
import com.shafagh.trx.internal.repository.BankTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
@Service
public class TransactionService implements TransactionApi {
 @jakarta.persistence.PersistenceContext(unitName="trx")
 private jakarta.persistence.EntityManager entityManager;
 
 @Transactional(propagation=Propagation.MANDATORY)
 public void record(RecordTransactionCommand c) {
  var t=new BankTransaction();t.operationId=c.operationId();t.accountId=c.accountId();t.amount=c.amount();
  entityManager.persist(t);entityManager.flush();
 }
}
