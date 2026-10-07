package com.shafagh.platform.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.XAConnection;
import oracle.jdbc.xa.client.OracleXADataSource;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OracleCredentialsTest {

    @Test
    void oracleStartupCheckReceivesConfiguredCredentialsAfterTimeoutPropertiesAreApplied() throws Exception {
        var environment = new MockEnvironment()
            .withProperty("bank.datasource.cif.url", "jdbc:oracle:thin:@fixture.invalid:1521:fixture")
            .withProperty("bank.datasource.cif.username", "FIXTURE_USERNAME")
            .withProperty("bank.datasource.cif.password", "FIXTURE_PASSWORD")
            .withProperty("bank.datasource.cif.xa-class", CapturingOracleXADataSource.class.getName())
            .withProperty("bank.datasource.cif.connect-timeout-ms", "12000")
            .withProperty("bank.datasource.cif.read-timeout-ms", "45000");

        var pool = XaPersistence.dataSource(environment, "cif");
        var oracle = (CapturingOracleXADataSource) pool.getXaDataSource();

        // The inherited no-argument method performs Oracle's real credential selection.
        // Only its final network operation is replaced by the capturing overload below.
        assertThat(oracle.suppliedUsername).isEqualTo("FIXTURE_USERNAME");
        assertThat(oracle.suppliedPassword).isEqualTo("FIXTURE_PASSWORD");
        assertThat(oracle.getConnectionProperties())
            .containsEntry("oracle.net.CONNECT_TIMEOUT", "12000")
            .containsEntry("oracle.jdbc.ReadTimeout", "45000");
        verify(oracle.logicalConnection).isValid(5);
        verify(oracle.logicalConnection).close();
        verify(oracle.physicalConnection).close();
    }

    public static final class CapturingOracleXADataSource extends OracleXADataSource {
        private final Connection logicalConnection;
        private final XAConnection physicalConnection;
        private String suppliedUsername;
        private String suppliedPassword;

        public CapturingOracleXADataSource() throws SQLException {
            logicalConnection = mock(Connection.class);
            physicalConnection = mock(XAConnection.class);
            when(logicalConnection.isValid(5)).thenReturn(true);
            when(physicalConnection.getConnection()).thenReturn(logicalConnection);
        }

        @Override
        public XAConnection getXAConnection(String username, String password) {
            suppliedUsername = username;
            suppliedPassword = password;
            return physicalConnection;
        }
    }
}
