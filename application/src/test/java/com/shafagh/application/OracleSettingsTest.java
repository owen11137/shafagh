package com.shafagh.application;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.assertj.core.api.Assertions.assertThat;

class OracleSettingsTest {
    @Test
    void productionSettingsUseSidAndModuleOwnedSchemas() {
        new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .run(context -> {
                var env = context.getEnvironment();
                for (String module : new String[]{"base", "cif", "dpst", "loan", "transaction"}) {
                    String prefix = "bank.datasource." + module + ".";
                    assertThat(env.getRequiredProperty(prefix + "url"))
                        .isEqualTo("jdbc:oracle:thin:@CENTRALDB-19C.MODERNISC.COM:1521:centraldb");
                    assertThat(env.getRequiredProperty(prefix + "xa-class"))
                        .isEqualTo("oracle.jdbc.xa.client.OracleXADataSource");
                    String schema = module.equals("transaction") ? "SHFQ_TRX" : "SHFQ_" + module.toUpperCase(java.util.Locale.ROOT);
                    assertThat(env.getRequiredProperty(prefix + "schema")).isEqualTo(schema);
                    assertThat(env.getRequiredProperty(prefix + "username")).isEqualTo(schema);
                }
                assertThat(env.getRequiredProperty("bank.ddl-mode")).isEqualTo("validate");
            });
    }
}
