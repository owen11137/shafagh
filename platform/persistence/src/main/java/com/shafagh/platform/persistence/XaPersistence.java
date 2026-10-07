package com.shafagh.platform.persistence;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import java.util.*;
import javax.sql.XADataSource;
import java.sql.SQLException;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ClassUtils;
public final class XaPersistence {
 private XaPersistence() {}
 public static AtomikosDataSourceBean dataSource(Environment env, String module) {
  String prefix="bank.datasource."+module+".";
  var source=new AtomikosDataSourceBean();
  source.setUniqueResourceName("shafagh-"+module);
  String password=env.getRequiredProperty(prefix+"password");
  if(password.isBlank()) throw new IllegalStateException("Missing database password for module: "+module);
  try {
   Class<?> type=ClassUtils.forName(env.getProperty(prefix+"xa-class", "oracle.jdbc.xa.client.OracleXADataSource"),XaPersistence.class.getClassLoader());
   XADataSource xa=(XADataSource)BeanUtils.instantiateClass(type);
   var wrapper=new BeanWrapperImpl(xa);
   wrapper.setPropertyValue("URL",env.getRequiredProperty(prefix+"url"));
   wrapper.setPropertyValue("user",env.getRequiredProperty(prefix+"username"));
   wrapper.setPropertyValue("password",password);
   xa.setLoginTimeout(env.getProperty(prefix+"login-timeout-seconds",Integer.class,10));
   if(xa instanceof oracle.jdbc.xa.client.OracleXADataSource oracle) {
    var properties=new Properties();
    properties.setProperty("oracle.net.CONNECT_TIMEOUT",env.getProperty(prefix+"connect-timeout-ms","10000"));
    properties.setProperty("oracle.jdbc.ReadTimeout",env.getProperty(prefix+"read-timeout-ms","60000"));
    oracle.setConnectionProperties(properties);
   }
   verifyConnection(xa,module);
   source.setXaDataSource(xa);
  } catch (ClassNotFoundException | SQLException ex) {
   throw new IllegalStateException("Cannot configure XA connection for module: "+module,ex);
  }
  source.setMinPoolSize(env.getProperty(prefix+"min-pool-size",Integer.class,1));
  source.setMaxPoolSize(env.getProperty(prefix+"max-pool-size",Integer.class,5));
  source.setBorrowConnectionTimeout(env.getProperty(prefix+"borrow-timeout-seconds",Integer.class,15));
  return source;
 }
 // Test the physical XA connection before Atomikos can hide creation failures as pool exhaustion.
 static void verifyConnection(XADataSource source,String module) {
  javax.sql.XAConnection xa=null;
  Throwable failure=null;
  try {
   xa=source.getXAConnection();
   try(var connection=xa.getConnection()) {
    if(!connection.isValid(5)) throw new SQLException("Connection validation failed");
   }
  } catch(SQLException ex) {
   failure=ex;
   throw new IllegalStateException("Database connection failed for module: "+module+" (SQLState="+ex.getSQLState()+", errorCode="+ex.getErrorCode()+"). See the underlying JDBC error.",ex);
  } finally {
   if(xa!=null) {
    try {xa.close();} catch(SQLException closeError) {
     if(failure!=null) failure.addSuppressed(closeError);
     else throw new IllegalStateException("Cannot close startup XA connection for module: "+module,closeError);
    }
   }
  }
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
