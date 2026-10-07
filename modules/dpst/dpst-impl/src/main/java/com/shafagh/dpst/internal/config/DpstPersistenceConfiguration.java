package com.shafagh.dpst.internal.config;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.shafagh.platform.persistence.XaPersistence;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
@Configuration
@EnableJpaRepositories(basePackages="com.shafagh.dpst.internal.repository",entityManagerFactoryRef="dpstEntityManager",transactionManagerRef="transactionManager")
public class DpstPersistenceConfiguration {
 @Bean(initMethod="init",destroyMethod="close")
 @DependsOn("transactionManager")
 public AtomikosDataSourceBean dpstDataSource(Environment env) { return XaPersistence.dataSource(env,"dpst"); }
 @Bean public LocalContainerEntityManagerFactoryBean dpstEntityManager(Environment env,@Qualifier("dpstDataSource") AtomikosDataSourceBean source) { return XaPersistence.entityManager(env,"dpst",source); }
}
