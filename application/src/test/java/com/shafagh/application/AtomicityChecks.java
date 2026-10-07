package com.shafagh.application;
import com.shafagh.cif.internal.service.CustomerService;
import com.shafagh.dpst.internal.service.DepositService;
import com.shafagh.transaction.internal.entity.BankTransaction;
import com.shafagh.transaction.internal.repository.BankTransactionRepository;
import com.shafagh.dpst.internal.repository.DepositPostingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import java.math.BigDecimal;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
abstract class AtomicityChecks {
 @Autowired CustomerService customers;
 @Autowired DepositService deposits;
 @Autowired BankTransactionRepository transactions;
 @Autowired DepositPostingRepository postings;
 @Autowired PlatformTransactionManager manager;
 @Test void commitsAndRetriesWithoutDoubleCredit() {
  var customer=customers.create("XA demo "+UUID.randomUUID());
  var account=deposits.open(customer.id());
  String operation=UUID.randomUUID().toString();
  deposits.credit(account.id(),operation,new BigDecimal("100.00"));
  deposits.credit(account.id(),operation,new BigDecimal("100.00"));
  assertThat(deposits.account(account.id()).balance()).isEqualByComparingTo("100.00");
  assertThat(postings.findById(operation)).isPresent();
  assertThat(transactions.findById(operation)).isPresent();
 }
 @Test void lateConstraintFailureRollsBackAccountAndPosting() {
  var customer=customers.create("XA rollback "+UUID.randomUUID());
  var account=deposits.open(customer.id());
  String operation=UUID.randomUUID().toString();
  // Existing transaction with this PK forces a genuine database failure in the last module.
  new TransactionTemplate(manager).executeWithoutResult(status->{
   var t=new BankTransaction();t.operationId=operation;t.accountId=account.id();t.amount=BigDecimal.ONE;
   transactions.saveAndFlush(t);
  });
  assertThatThrownBy(()->deposits.credit(account.id(),operation,new BigDecimal("100.00"))).isInstanceOf(RuntimeException.class);
  assertThat(deposits.account(account.id()).balance()).isEqualByComparingTo("0");
  assertThat(postings.findById(operation)).isEmpty();
  assertThat(transactions.findById(operation).orElseThrow().amount).isEqualByComparingTo("1");
 }
 @Test void rejectsReuseWithDifferentAmount() {
  var customer=customers.create("XA duplicate "+UUID.randomUUID());
  var account=deposits.open(customer.id());String operation=UUID.randomUUID().toString();
  deposits.credit(account.id(),operation,new BigDecimal("10.00"));
  assertThatThrownBy(()->deposits.credit(account.id(),operation,new BigDecimal("20.00"))).isInstanceOf(IllegalArgumentException.class);
  assertThat(deposits.account(account.id()).balance()).isEqualByComparingTo("10");
 }
}
