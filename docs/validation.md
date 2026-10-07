# Validation

Current workspace: Java 21, Maven 3.9.11, Spring Boot 4.1.1, Atomikos 6.0.1 Jakarta.

- Maven reactor build and executable Spring Boot JAR: passed.
- Ten executed tests: architecture isolation, XA commit/idempotent retry, late database failure rollback, mismatched retry rejection, HTTP customer/account/credit workflow, and module-owned Oracle SID/schema configuration, and dotenv password loading without shell export, and username fallback when no password is configured, and preservation of the original JDBC connection failure before XA pool initialization, and preservation of configured credentials through the actual Oracle XA driver credential selection. All passed.
- Three Oracle tests: skipped (RUN_ORACLE_IT not set; Oracle network and credentials unavailable).
- Crash recovery and Oracle XA behavior: not validated; required before production use.

The late-failure test pre-inserts a conflicting operation in the transaction database, then verifies the deposit account balance is unchanged and its posting is absent after the insert fails. Independent H2 XA databases exercise the real Atomikos coordinator; they do not emulate Oracle-specific behavior.

Oracle tests insert DEMO records and intentionally leave them for inspection. They do not delete existing business data. Use dedicated test schemas. Schema DDL is manual and application startup uses validate, never create/drop, for Oracle.

Oracle connection configuration corrected to hostname CENTRALDB-19C.MODERNISC.COM, port 1521, SID centraldb. Transaction schema is SHFQ_TRX. Credentials remain in ignored local .env only. Business-module Maven aggregators make module grouping explicit.

Oracle driver credential selection was exercised offline with fictitious credentials and a capturing XA subclass. Authentication against the user database is still unverified. Timeout properties do not clear authentication in ojdbc11 23.26.3.0.0.
