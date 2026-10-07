package com.shafagh.cif.internal.config;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.shafagh.platform.persistence.XaPersistence;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
@Configuration
@EnableJpaRepositories(basePackages="com.shafagh.cif.internal.repository",entityManagerFactoryRef="cifEntityManager",transactionManagerRef="transactionManager")
public class CifPersistenceConfiguration {
 @Bean(initMethod="init",destroyMethod="close")
 @DependsOn("transactionManager")
 public AtomikosDataSourceBean cifDataSource(Environment env) { return XaPersistence.dataSource(env,"cif"); }
 @Bean public LocalContainerEntityManagerFactoryBean cifEntityManager(Environment env,@Qualifier("cifDataSource") AtomikosDataSourceBean source) { return XaPersistence.entityManager(env,"cif",source); }
}
