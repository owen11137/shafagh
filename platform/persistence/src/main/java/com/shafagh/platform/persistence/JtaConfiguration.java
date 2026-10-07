package com.shafagh.platform.persistence;
import com.atomikos.icatch.jta.UserTransactionManager;
import org.springframework.context.annotation.*;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;
@Configuration
@EnableTransactionManagement
public class JtaConfiguration {
 @Bean(initMethod="init", destroyMethod="close")
 public UserTransactionManager atomikosManager() {
  var manager = new UserTransactionManager();
  manager.setForceShutdown(false);
  return manager;
 }
 @Bean public JtaTransactionManager transactionManager(UserTransactionManager manager) {
  return new JtaTransactionManager(manager, manager);
 }
}
