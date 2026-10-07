package com.shafagh.trx.internal.config;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.shafagh.platform.persistence.XaPersistence;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
@Configuration
@EnableJpaRepositories(basePackages="com.shafagh.trx.internal.repository",entityManagerFactoryRef="trxEntityManager",transactionManagerRef="transactionManager")
public class TransactionPersistenceConfiguration {
 @Bean(initMethod="init",destroyMethod="close")
 @DependsOn("transactionManager")
 public AtomikosDataSourceBean trxDataSource(Environment env) { return XaPersistence.dataSource(env,"trx"); }
 @Bean public LocalContainerEntityManagerFactoryBean trxEntityManager(Environment env,@Qualifier("trxDataSource") AtomikosDataSourceBean source) { return XaPersistence.entityManager(env,"trx",source); }
}
