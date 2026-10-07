# Shafagh — نمونه Modular Monolith با Oracle XA

نمونه آموزشی: ثبت مشتری، افتتاح حساب و واریز با ثبت گردش سپرده و سابقه عملیات در اسکیمای مستقل. یک برنامه Spring Boot، سه کاربر و اتصال XA مستقل، و مدیر JTA مشترک Atomikos دارد. Connection یا Repository بین ماژول‌ها منتقل نمی‌شود.

## ابزارها

Java 21، Spring Boot 4.1.1، Atomikos 6.0.1 (Jakarta)، Oracle JDBC از BOM بوت. نسخه پایدار بوت از metadata رسمی Maven Central انتخاب شده است. base و loan فعلاً فقط ساختار API/implementation دارند؛ برای آن‌ها اتصال بدون کاربرد ایجاد نمی‌شود.

## ساخت و تست محلی

```sh
./mvnw clean verify
```

تست محلی از سه دیتابیس H2 مستقل با XA واقعی استفاده می‌کند: commit، خطای واقعی کلید تکراری در مرحله آخر و rollback سپرده، جلوگیری از واریز مجدد، و تست مرز ماژول‌ها. این تست، تأیید رفتار Oracle یا recovery بعد از crash نیست.

## آماده‌سازی Oracle شما

1. نام سرویس اعلام‌شده: `CENTRALDB-19C.MODERNISC.COM`، میزبان `172.31.65.19`، پورت `1521`.
2. وجود کاربران `SHFQ_CIF`، `SHFQ_DPST` و `SHFQ_TXN` را بررسی کنید. هر کاربر فقط روی اسکیمای خودش کار می‌کند.
3. اسکریپت‌های `database/cif.sql`، `database/dpst.sql` و `database/transaction.sql` را با کاربر همان اسکیما روی محیط آزمایشی اجرا کنید. این اسکریپت‌ها برای اجرا یک‌باره‌اند و جدول موجود را حذف نمی‌کنند. از قبل باید مجوز ساخت جدول و quota مناسب داشته باشید.
4. DBA فایل `database/xa-dba-review.sql` و نیازهای XA recovery نسخه Oracle را بررسی کند. این مجوزهای فنی، دسترسی عمومی به جدول‌های ماژول‌های دیگر نیستند.
5. `.env.example` را به `.env` کپی و رمزها را فقط در فایل محلی وارد کنید. در این نمونه هیچ رمز واقعی ذخیره نشده است.

```sh
set -a
. ./.env
set +a
RUN_ORACLE_IT=true ./mvnw clean verify
```

تست Oracle تنها با `RUN_ORACLE_IT=true` فعال می‌شود.

تست Oracle همان سناریوهای اتمیک بودن را روی جدول‌های DEMO اجرا می‌کند و رکوردهای آزمایشی با UUID باقی می‌گذارد. روی دیتابیس تولید اجرا نکنید. پروفایل Oracle فقط schema validation دارد و جدول نمی‌سازد یا حذف نمی‌کند.

## اجرای برنامه

پس از build و بارگذاری متغیرها:

```sh
java -jar application/target/application-0.1.0-SNAPSHOT.jar
```

نمونه درخواست‌ها، روی ماشین خودتان:

```sh
curl -H 'Content-Type: application/json' -d '{"name":"Ali"}' http://localhost:8080/api/customers
# شناسه برگشتی مشتری را جایگزین کنید:
curl -H 'Content-Type: application/json' -d '{"customerId":"CUSTOMER_UUID"}' http://localhost:8080/api/accounts
# شناسه حساب برگشتی را جایگزین کنید:
curl -H 'Content-Type: application/json' -d '{"operationId":"11111111-1111-4111-8111-111111111111","amount":100000}' http://localhost:8080/api/accounts/ACCOUNT_UUID/credits
curl http://localhost:8080/api/accounts/ACCOUNT_UUID
```

درخواست یکسان با همان operationId دوباره واریز نمی‌کند. استفاده مجدد با مبلغ متفاوت رد می‌شود. صورت‌حساب در DEMO_POSTING ثبت می‌شود؛ API فهرست صورت‌حساب در دامنه این نمونه نیست.

## تراکنش و recovery

`DepositService.credit` مرز JTA است. ابتدا حساب قفل می‌شود، مشتری از CustomerApi بررسی می‌شود، موجودی و گردش در سپرده ذخیره می‌شوند، سپس TransactionApi سابقه عملیات را ثبت می‌کند. هر سه اتصال تحت مدیر JTA هستند؛ failure در ثبت آخر باید تغییرات سپرده را rollback کند. رکورد تراکنش ناموفق نیز در همان تراکنش باقی نمی‌ماند.

فایل `application/src/main/resources/jta.properties` شناسه پایدار مدیر و محل لاگ را مشخص می‌کند. `.runtime/atomikos` باید در محیط واقعی روی volume پایدار باشد. آن را هنگام تراکنش pending حذف نکنید. نمونه برای یک instance است؛ چند instance نباید شناسه مدیر یا مسیر لاگ مشترک داشته باشند. XA اتمیک بودن دیتابیس را هدف می‌گیرد؛ تماس با سرویس پرداخت بیرونی در این تضمین قرار نمی‌گیرد.

Recovery پس از قطع برنامه هنوز تأیید نشده است. پیش از استفاده واقعی باید crash هنگام prepare/commit، راه‌اندازی مجدد با همان لاگ و شناسه، و وضعیت pending در Oracle توسط DBA بررسی شود. این نمونه بدون احراز هویت است و فقط برای تست در محیط محدود استفاده شود.

## ساختار

`modules/*/*-api` قراردادهای جاوا و DTOها؛ `*-impl` شامل controller، request DTO، service، entity، repository و config. فقط API ماژول‌های دیگر dependency می‌شود. `application` برنامه را assemble می‌کند. `platform/persistence` زیرساخت مشترک است و داده کسب‌وکار ندارد.

در tasks ابری از checkout موجود استفاده کنید؛ هر task از قبل ایزوله است و worktree جدید لازم نیست. برای Oracle خصوصی در cloud، VPN و مجوز TCP مقصد لازم است؛ allowlist وب به‌تنهایی اتصال JDBC را فراهم نمی‌کند.
