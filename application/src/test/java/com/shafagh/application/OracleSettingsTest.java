package com.shafagh.application;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.assertj.core.api.Assertions.assertThat;

class OracleSettingsTest {
    @org.junit.jupiter.api.io.TempDir java.nio.file.Path temporaryDirectory;

    @Test
    void dotenvPropertiesSupplyPasswordWithoutShellExport() throws Exception {
        var file = temporaryDirectory.resolve("test.env");
        java.nio.file.Files.writeString(file, "CIF_DB_PASSWORD=fixture-only\n");
        new ApplicationContextRunner()
            .withPropertyValues("spring.config.import=optional:file:" + file.toUri().getPath() + "[.properties],classpath:bank/cif.yml")
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .run(context -> assertThat(context.getEnvironment().getRequiredProperty("bank.datasource.cif.password"))
                .isEqualTo("fixture-only"));
    }

    @Test
    void absentPasswordDefaultsToTheConfiguredUsername() {
        new ApplicationContextRunner()
            .withPropertyValues("spring.config.location=classpath:bank/cif.yml", "CIF_DB_USERNAME=DEMO_FIXTURE_USER")
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .run(context -> assertThat(context.getEnvironment().getRequiredProperty("bank.datasource.cif.password"))
                .isEqualTo("DEMO_FIXTURE_USER"));
    }

    @Test
    void productionSettingsUseSidAndModuleOwnedSchemas() {
        new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .run(context -> {
                var env = context.getEnvironment();
                for (String module : new String[]{"base", "cif", "dpst", "loan", "trx"}) {
                    String prefix = "bank.datasource." + module + ".";
                    assertThat(env.getRequiredProperty(prefix + "url"))
                        .isEqualTo("jdbc:oracle:thin:@172.31.65.19:1521:centraldb");
                    assertThat(env.getRequiredProperty(prefix + "xa-class"))
                        .isEqualTo("oracle.jdbc.xa.client.OracleXADataSource");
                    String schema = module.equals("trx") ? "SHFQ_TRX" : "SHFQ_" + module.toUpperCase(java.util.Locale.ROOT);
                    assertThat(env.getRequiredProperty(prefix + "schema")).isEqualTo(schema);
                    assertThat(env.getRequiredProperty(prefix + "username")).isEqualTo(schema);
                }
                assertThat(env.getRequiredProperty("bank.ddl-mode")).isEqualTo("validate");
            });
    }
}
