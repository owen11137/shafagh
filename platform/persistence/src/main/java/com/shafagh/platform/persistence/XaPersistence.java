package com.shafagh.platform.persistence;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import java.util.*;
public final class XaPersistence {
 private XaPersistence() {}
 public static AtomikosDataSourceBean dataSource(Environment env, String module) {
  String prefix="bank.datasource."+module+".";
  var source=new AtomikosDataSourceBean();
  source.setUniqueResourceName("shafagh-"+module);
  source.setXaDataSourceClassName(env.getProperty(prefix+"xa-class", "oracle.jdbc.xa.client.OracleXADataSource"));
  var props=new Properties();
  props.setProperty("URL",env.getRequiredProperty(prefix+"url"));
  props.setProperty("user",env.getRequiredProperty(prefix+"username"));
  String password=env.getRequiredProperty(prefix+"password");
  if(password.isBlank()) throw new IllegalStateException("Missing database password for module: "+module);
  props.setProperty("password",password);
  source.setXaProperties(props);
  source.setMinPoolSize(env.getProperty(prefix+"min-pool-size",Integer.class,1));
  source.setMaxPoolSize(env.getProperty(prefix+"max-pool-size",Integer.class,5));
  source.setBorrowConnectionTimeout(env.getProperty(prefix+"borrow-timeout-seconds",Integer.class,15));
  return source;
 }
 public static LocalContainerEntityManagerFactoryBean entityManager(Environment env, String module, AtomikosDataSourceBean source) {
  var factory=new LocalContainerEntityManagerFactoryBean();
  factory.setPersistenceUnitName(module);
  factory.setPackagesToScan("com.shafagh."+module+".internal.entity");
  factory.setJtaDataSource(source);
  factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
  factory.setJpaPropertyMap(Map.of(
   "hibernate.transaction.coordinator_class","jta",
   "hibernate.transaction.jta.platform","org.hibernate.engine.transaction.jta.platform.internal.AtomikosJtaPlatform",
   "hibernate.hbm2ddl.auto",env.getProperty("bank.ddl-mode","validate"),
   "hibernate.default_schema",env.getRequiredProperty("bank.datasource."+module+".schema"),
   "hibernate.jdbc.batch_size",20,
   "hibernate.order_inserts",true,
   "hibernate.order_updates",true,
   "hibernate.show_sql",false,
   "hibernate.format_sql",false,
   "hibernate.dialect",env.getProperty("bank.dialect","org.hibernate.dialect.OracleDialect")));
  return factory;
 }
}
