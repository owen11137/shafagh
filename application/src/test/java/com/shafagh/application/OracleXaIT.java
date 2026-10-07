package com.shafagh.application;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
@SpringBootTest
@EnabledIfEnvironmentVariable(named="RUN_ORACLE_IT",matches="true")
class OracleXaIT extends AtomicityChecks {}
