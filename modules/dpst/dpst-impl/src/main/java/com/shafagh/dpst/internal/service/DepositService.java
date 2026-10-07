package com.shafagh.dpst.internal.service;
import com.shafagh.dpst.api.*;
import com.shafagh.cif.api.CustomerApi;
import com.shafagh.trx.api.*;
import com.shafagh.dpst.internal.entity.*;
import com.shafagh.dpst.internal.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.UUID;
@Service @Transactional(rollbackFor=Exception.class)
public class DepositService implements DepositApi {
 private final DepositAccountRepository accounts;
 private final DepositPostingRepository postings;
 private final CustomerApi customers;
 private final TransactionApi transactions;
 public DepositService(DepositAccountRepository a,DepositPostingRepository p,CustomerApi c,TransactionApi t) {accounts=a;postings=p;customers=c;transactions=t;}
 public AccountResponse open(String customerId) {
  customers.requireCustomer(customerId);
  var a=new DepositAccount();a.id=UUID.randomUUID().toString();a.customerId=customerId;a.balance=BigDecimal.ZERO;
  accounts.save(a);return response(a);
 }
 public AccountResponse account(String id) {return response(accounts.findById(id).orElseThrow(()->new IllegalArgumentException("Account not found")));}
 public AccountResponse credit(String id,String operationId,BigDecimal amount) {
  if(amount==null || amount.signum()<=0 || amount.scale()>2 || amount.precision()-amount.scale()>17) throw new IllegalArgumentException("Invalid amount");
  UUID.fromString(operationId);
  var a=accounts.lockById(id).orElseThrow(()->new IllegalArgumentException("Account not found"));
  customers.requireCustomer(a.customerId);
  var existing=postings.findById(operationId);
  if(existing.isPresent()) {
   var p=existing.get();
   if(!p.accountId.equals(id)||p.amount.compareTo(amount)!=0) throw new IllegalArgumentException("Operation ID already used with different data");
   return response(a);
  }
  a.balance=a.balance.add(amount);accounts.saveAndFlush(a);
  var p=new DepositPosting();p.operationId=operationId;p.accountId=id;p.amount=amount;postings.saveAndFlush(p);
  transactions.record(new RecordTransactionCommand(operationId,id,amount));
  return response(a);
 }
 private AccountResponse response(DepositAccount a) {return new AccountResponse(a.id,a.customerId,a.balance);}
}
