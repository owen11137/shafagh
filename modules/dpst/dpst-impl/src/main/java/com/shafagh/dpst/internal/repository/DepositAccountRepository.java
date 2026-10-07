package com.shafagh.dpst.internal.repository;
import com.shafagh.dpst.internal.entity.DepositAccount;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DepositAccountRepository extends JpaRepository<DepositAccount,String> { @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
 @org.springframework.data.jpa.repository.Query("select a from DepositAccount a where a.id = :id")
 java.util.Optional<DepositAccount> lockById(@org.springframework.data.repository.query.Param("id") String id); }
