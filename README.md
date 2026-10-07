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

1. میزبان: `172.31.65.19`، پورت: `1521`، SID: `centraldb`. اتصال با قالب SID است: `jdbc:oracle:thin:@172.31.65.19:1521:centraldb`.
2. وجود کاربران `SHFQ_CIF`، `SHFQ_DPST` و `SHFQ_TRX` را بررسی کنید. هر کاربر فقط روی اسکیمای خودش کار می‌کند.
3. اسکریپت‌های `database/cif.sql`، `database/dpst.sql` و `database/trx.sql` را با کاربر همان اسکیما روی محیط آزمایشی اجرا کنید. این اسکریپت‌ها برای اجرا یک‌باره‌اند و جدول موجود را حذف نمی‌کنند. از قبل باید مجوز ساخت جدول و quota مناسب داشته باشید.
4. DBA فایل `database/xa-dba-review.sql` و نیازهای XA recovery نسخه Oracle را بررسی کند. این مجوزهای فنی، دسترسی عمومی به جدول‌های ماژول‌های دیگر نیستند.
5. در checkout آماده‌شده، فایل محلی و Git-ignored به نام `.env` با مشخصات اعلام‌شده شما آماده است. پس از clone جدید، برای اتصال نمونه نیازی به فایل `.env` نیست؛ رمز پیش‌فرض از نام کاربری گرفته می‌شود. برای تغییر مشخصات، `.env.example` را به `.env` کپی و overrideها را وارد کنید.

```sh
# Only when using an optional .env file:
set -a
[ ! -f .env ] || . ./.env
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

## ماژول Maven با پکیج جاوا چه تفاوتی دارد؟

ریشه `com.shafagh` فقط namespace است. هر ماژول زیر پوشه مستقل `modules/` یک `pom.xml` از نوع aggregator دارد و شامل دو پروژه مستقل Maven، با artifact و JAR جدا، است:

```text
modules/
├── base/pom.xml        → base-api + base-impl
├── cif/pom.xml         → cif-api + cif-impl
├── dpst/pom.xml        → dpst-api + dpst-impl
├── loan/pom.xml        → loan-api + loan-impl
└── SHFQ_TRX/pom.xml → trx-api + trx-impl
```

در IntelliJ، فایل `pom.xml` ریشه را به‌عنوان Maven Project باز و Reload کنید. Maven همه پروژه‌ها را به‌عنوان module مستقل شناسایی می‌کند. فقط `application` یک برنامه اجرایی است؛ deploy مشترک، ویژگی Modular Monolith است. جداکردن سرویس‌های اجرایی، معماری متفاوتی خواهد بود.

تنظیمات اتصال هر ماژول در `*-impl/src/main/resources/bank/` خودش است. تنظیمات Hibernate/JTA مشترک در `platform/persistence` قرار دارد. هر ماژول فعال EntityManagerFactory، اتصال XA و schema اختصاصی دارد؛ repositories به EntityManager همان ماژول متصل هستند. `base` و `loan` تنظیمات آماده دارند ولی چون هنوز Entity یا کاربرد اجرایی ندارند، اتصال فعال مصرف نمی‌کنند.

Hibernate در Oracle با dialect اختصاصی، schema صریح، DDL برابر validate، batch size برابر 20، SQL logging خاموش و Entity scan محدود به ماژول تنظیم شده است. برنامه فقط جدول‌ها را بررسی می‌کند؛ SQLهای پوشه database را باید یک‌بار با کاربران همان اسکیما اجرا کنید. تغییر SID/hostname یا رمز از طریق متغیر محیط ممکن است.

## رفع خطای Missing database password در IDE

Spring فایل `.env` را اکنون به‌عنوان Java properties از پوشه کاری یا یک سطح بالاتر بارگذاری می‌کند. هنگام اجرا در IDE، Working directory را ریشه پروژه یا پوشه `application` قرار دهید. پس از clone، فایل `.env` موجود در ماشین ابری به سیستم شما منتقل نمی‌شود؛ با پیش‌فرض فعلی، نبودن این فایل مانع اجرای نمونه نیست. برای مشخصات متفاوت، `.env.example` را به `.env` کپی و overrideها را وارد کنید. فایل شامل خطوط `KEY=value` است؛ از `export` یا کوتیشن shell در مقدارها استفاده نکنید. متغیرهای محیطی از فایل اولویت بالاتری دارند.

اگر متغیر رمز در Run Configuration وجود دارد ولی خالی است، آن را حذف یا مقداردهی کنید. فایل `.env` وارد Git نمی‌شود. این اصلاح فقط بارگذاری تنظیمات را پوشش می‌دهد؛ دسترسی Oracle و وجود جدول‌ها باید جدا بررسی شود.

## رمزهای پیش‌فرض نمونه

طبق تنظیمات دیتابیس آزمایشی اعلام‌شده، رمز هر اتصال در صورت تعریف‌نشدن متغیر رمز، از نام کاربری همان اتصال گرفته می‌شود. بنابراین برای این نمونه فایل `.env` الزامی نیست. برای استفاده از رمز متفاوت، متغیر `CIF_DB_PASSWORD`، `DPST_DB_PASSWORD`، `TRX_DB_PASSWORD` و متغیر متناظر سایر ماژول‌ها را تنظیم کنید. متغیر تعریف‌شده ولی خالی، مقدار پیش‌فرض را فعال نمی‌کند؛ آن را حذف کنید. تنظیم پیش‌فرض برابری رمز و نام کاربر مخصوص همین نمونه است.

## خطای Connection pool exhausted هنگام startup

بزرگ‌ترکردن pool لزوماً مشکل را حل نمی‌کند؛ شکست ساخت اتصال فیزیکی هم می‌تواند همین پیام را ایجاد کند. برنامه ابتدا یک اتصال XA واقعی را باز و اعتبار آن را بررسی می‌کند و سپس آن را می‌بندد. خطای اولیه JDBC با نام ماژول، SQLState و errorCode حفظ می‌شود. timeout اتصال Oracle برابر ۱۰ ثانیه و read timeout برابر ۶۰ ثانیه است؛ با تنظیمات `login-timeout-seconds`، `connect-timeout-ms` و `read-timeout-ms` هر ماژول قابل تغییر است.

خطای ORA-12505 معمولاً نیازمند بررسی SID در listener است؛ ORA-01017 اعتبار ورود را نشان می‌دهد؛ unknown host یا timeout نیازمند بررسی DNS، شبکه و پورت است. خطای pool به‌تنهایی هیچ‌کدام را اثبات نمی‌کند. برای تشخیص، اولین خطای startup و بخش Caused by آن را بررسی کنید.

## تشخیص ORA-01017 با رمز برابر نام کاربر

نام کاربری مشتری و رمز پیش‌فرض هر دو همان مقدار اعلام‌شده شما هستند. برنامه نام کاربری مؤثر و وضعیت وجود متغیر override رمز را هنگام ایجاد هر اتصال نمایش می‌دهد؛ مقدار رمز چاپ نمی‌شود. فایل `.env` یا Environment variables در IDE می‌توانند مقدار پیش‌فرض را override کنند. اگر قصد استفاده از پیش‌فرض دارید، overrideهای قدیمی را حذف کنید.

ابتدا نسخه جدید را با `./mvnw clean verify` بسازید و برنامه قدیمی را متوقف کنید. اگر Oracle همچنان ورود را رد کرد، همان hostname، port و SID را در SQL Developer با همان کاربر و رمز امتحان کنید. موفقیت روی Service Name دیگری، ورود با SID این برنامه را تأیید نمی‌کند؛ کاربران ممکن است متعلق به PDB متفاوت باشند. در اتصال موفق، برای مقایسه مقصد می‌توانید این درخواست خواندنی را اجرا کنید:

```sql
SELECT USER,
       SYS_CONTEXT('USERENV', 'DB_NAME') AS DB_NAME,
       SYS_CONTEXT('USERENV', 'CON_NAME') AS CON_NAME
FROM DUAL;
```

ORA-01017 به‌تنهایی مشخص نمی‌کند رمز تغییر کرده است؛ override تنظیمات، حروف بزرگ/کوچک یا اتصال به container متفاوت نیز باید بررسی شوند. برنامه رمز را trim یا lowercase نمی‌کند.

## خطای cannot find symbol برای TransactionApi در IntelliJ

کلاس عمومی در `modules/SHFQ_TRX/trx-api/src/main/java/com/shafagh/trx/api/TransactionApi.java` قرار دارد و `dpst-impl` به artifact برابر `com.shafagh:trx-api` وابسته است. پس از تغییر نام ماژول، Maven model قدیمی IDE ممکن است همچنان به ماژول حذف‌شده اشاره کند. ابتدا سورس جدید را دریافت کنید و از ریشه پروژه در PowerShell اجرا کنید:

```powershell
cd C:\projects\intellij\shafagh
git pull
.\mvnw.cmd -pl :dpst-impl -am clean install -DskipTests
```

این دستور سپرده، API تراکنش و تمام پیش‌نیازهایشان را از سورس می‌سازد و در مخزن محلی Maven نصب می‌کند؛ اتصال Oracle لازم ندارد. در IntelliJ فایل `pom.xml` ریشه را به‌عنوان Maven project انتخاب و از پنجره Maven گزینه Reload All Maven Projects را اجرا کنید. ماژول `trx-api` باید در Maven tree و وابستگی‌های `dpst-impl` دیده شود. سپس Build → Rebuild Project را اجرا کنید. ساختن دستی یک پکیج یا کپی‌کردن TransactionApi داخل ماژول سپرده مرز ماژول را خراب می‌کند و لازم نیست.

اگر Maven هم خطا داد، خروجی همان دستور علت build را نشان می‌دهد. موفقیت Maven و شکست build داخلی IntelliJ نشان‌دهنده تفاوت مدل وابستگی IDE با مدل Maven است؛ از آن به‌تنهایی نمی‌توان مشکل cache را قطعی دانست.
