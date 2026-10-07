package com.shafagh.transaction.internal.config;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.shafagh.platform.persistence.XaPersistence;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
@Configuration
@EnableJpaRepositories(basePackages="com.shafagh.transaction.internal.repository",entityManagerFactoryRef="transactionEntityManager",transactionManagerRef="transactionManager")
public class TransactionPersistenceConfiguration {
 @Bean(initMethod="init",destroyMethod="close")
 @DependsOn("transactionManager")
 public AtomikosDataSourceBean transactionDataSource(Environment env) { return XaPersistence.dataSource(env,"transaction"); }
 @Bean public LocalContainerEntityManagerFactoryBean transactionEntityManager(Environment env,@Qualifier("transactionDataSource") AtomikosDataSourceBean source) { return XaPersistence.entityManager(env,"transaction",source); }
}
