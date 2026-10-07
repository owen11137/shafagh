package com.shafagh.platform.persistence;
import org.junit.jupiter.api.Test;
import javax.sql.XADataSource;
import java.sql.SQLException;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class XaConnectionFailureTest {
 @Test void preservesTheOriginalJdbcFailureBeforePoolStartup() throws Exception {
  var source=mock(XADataSource.class);
  var failure=new SQLException("fixture connection refusal", "08001", 12505);
  when(source.getXAConnection()).thenThrow(failure);
  assertThatThrownBy(()->XaPersistence.verifyConnection(source,"cif"))
   .isInstanceOf(IllegalStateException.class)
   .hasMessageContaining("module: cif")
   .hasMessageContaining("errorCode=12505")
   .hasCause(failure);
 }
}
