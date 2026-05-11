# HIS Backend

Hospital Information System backend uygulaması. Spring Boot ile geliştirilmiş REST API; JWT authentication, refresh token, e-posta doğrulama, şifre sıfırlama, rol bazlı yetkilendirme, Swagger dokümantasyonu ve Flyway migration desteği içerir.

> Not: Bu doküman şu an aktif olarak geliştirilen backend modülünü kapsar. Frontend modülü Angular ile geliştirilecektir.

## Teknolojiler

- Java 17
- Spring Boot 3.3.5
- Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway
- JWT
- Spring Mail
- Springdoc OpenAPI / Swagger UI
- Maven Wrapper

## Gereksinimler

- Java 17
- PostgreSQL
- Git
- IntelliJ IDEA veya benzeri bir IDE

Maven ayrıca kurulmak zorunda değildir. Projede Maven Wrapper vardır:

```bash
./mvnw test
```

Windows için:

```bash
.\mvnw.cmd test
```

## Veritabanı Kurulumu

PostgreSQL üzerinde bir veritabanı oluştur:

```sql
create database his_db;
```

Varsayılan local bağlantı:

```text
jdbc:postgresql://localhost:5432/his_db
```

Local geliştirme ortamında `postgres / postgres` kullanıcı bilgileri kullanılabilir. Kendi PostgreSQL kullanıcı adın ve şifren farklıysa `.env` dosyasında güncellemelisin.

## Environment Dosyası

Backend klasöründe `.env.example` dosyası vardır. Bunu kopyalayıp `.env` oluştur:

```bash
cp .env.example .env
```

Windows PowerShell için:

```powershell
Copy-Item .env.example .env
```

`.env` dosyası Git'e gönderilmez. Gerçek DB şifresi, Gmail app password ve JWT secret gibi değerler sadece local makinende kalmalıdır.

Örnek local `.env`:

```env
DB_URL=jdbc:postgresql://localhost:5432/his_db
DB_USERNAME=postgres
DB_PASSWORD=postgres

SPRING_PROFILES_ACTIVE=dev
JPA_DDL_AUTO=update
JPA_SHOW_SQL=true
HIBERNATE_FORMAT_SQL=true
APP_LOG_LEVEL=DEBUG
SECURITY_LOG_LEVEL=DEBUG

JWT_SECRET=replace-with-a-256-bit-base64-secret
JWT_EXPIRATION=86400000
REFRESH_TOKEN_EXPIRATION=604800000
REFRESH_TOKEN_CLEANUP_ENABLED=true
REFRESH_TOKEN_CLEANUP_INITIAL_DELAY=600000
REFRESH_TOKEN_CLEANUP_FIXED_DELAY=3600000
EMAIL_VERIFICATION_EXPIRATION=86400000
PASSWORD_RESET_EXPIRATION=900000

APP_BASE_URL=http://localhost:8080
CORS_ALLOWED_ORIGINS=http://localhost:4200
SWAGGER_ENABLED=true

MAIL_PROVIDER=log
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-gmail-address@gmail.com
MAIL_PASSWORD=your-google-app-password
```

Gmail SMTP kullanmak için:

```env
MAIL_PROVIDER=smtp
MAIL_USERNAME=your-gmail-address@gmail.com
MAIL_PASSWORD=your-google-app-password
```

Localde gerçek e-posta göndermek istemiyorsan:

```env
MAIL_PROVIDER=log
```

Bu durumda doğrulama ve şifre sıfırlama linkleri loglara yazılır.

## Profil Mantığı

Uygulamada üç temel çalışma modu vardır:

| Profil | Kullanım | Açıklama |
| --- | --- | --- |
| default | Genel varsayılan | `ddl-auto=validate`, loglar daha sakin |
| dev | Local geliştirme | `ddl-auto=update`, SQL/security logları açık |
| prod | Production | `ddl-auto=validate`, Swagger varsayılan kapalı |

Local geliştirme için önerilen ayar:

```env
SPRING_PROFILES_ACTIVE=dev
```

Production için:

```env
SPRING_PROFILES_ACTIVE=prod
JPA_DDL_AUTO=validate
SWAGGER_ENABLED=false
JPA_SHOW_SQL=false
SECURITY_LOG_LEVEL=WARN
```

Production profilinde default development JWT secret kullanılmamalıdır. `JWT_SECRET` gerçek ve güçlü bir Base64 secret olmalıdır.

## Uygulamayı Çalıştırma

Backend klasörüne geç:

```bash
cd his-backend
```

Maven Wrapper ile çalıştır:

```bash
./mvnw spring-boot:run
```

Windows için:

```powershell
.\mvnw.cmd spring-boot:run
```

IntelliJ ile çalıştırmak için:

1. `his-backend` modülünü aç.
2. Main class olarak `com.his.HisBackendApplication` seç.
3. `.env` dosyasının `his-backend` klasöründe olduğundan emin ol.
4. Run butonuna bas.

Uygulama varsayılan olarak şu adreste çalışır:

```text
http://localhost:8080
```

## Swagger

Development ortamında Swagger açıktır:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

Production profilinde Swagger varsayılan olarak kapalıdır:

```env
SPRING_PROFILES_ACTIVE=prod
SWAGGER_ENABLED=false
```

Geliştirme ortamında Swagger üzerinden JWT isteyen endpointleri test etmek için:

1. `/api/auth/login` ile giriş yap.
2. Dönen `token` değerini kopyala.
3. Swagger sağ üstteki `Authorize` butonuna bas.
4. Token değerini Bearer auth alanına gir.

## Roller

Sistemdeki roller:

| Rol | Açıklama |
| --- | --- |
| `ADMIN` | Kullanıcı, departman, doktor, hasta ve genel yönetim işlemleri |
| `RECEPTIONIST` | Hasta ve randevu operasyonlarını yönetir |
| `DOCTOR` | Kendi randevularını ve tıbbi kayıt akışlarını yönetir |
| `PATIENT` | Kendi hasta bilgileri, randevuları ve tıbbi kayıtlarını görüntüler |

Roller uygulama açılırken `DataSeeder` tarafından otomatik eklenir. Eğer `roles` tablosu boşsa temel roller oluşturulur.

## Auth Akışı

Temel authentication akışı:

1. Kullanıcı register olur.
2. Sistem e-posta doğrulama tokenı üretir.
3. Kullanıcı e-postasını doğrular.
4. Login sonrası access token ve refresh token alır.
5. Access token API isteklerinde kullanılır.
6. Refresh token süresi dolmadan access token yenilenebilir.
7. Logout refresh tokenı iptal eder.

Şifre sıfırlama akışı:

1. `/api/auth/forgot-password` ile reset token istenir.
2. E-posta veya log üzerinden token alınır.
3. `/api/auth/reset-password` ile yeni şifre belirlenir.
4. Kullanıcının eski refresh tokenları temizlenir.

## Flyway

Flyway veritabanı migration yönetimi için kullanılır.

Migration dosyaları:

```text
src/main/resources/db/migration
```

Mevcut migrationlar:

```text
V2__cleanup_medical_records_schema.sql
V3__add_account_tokens_and_email_verification.sql
```

Önemli not:

- Production tarafında schema değişiklikleri Hibernate ile otomatik yapılmaz.
- `JPA_DDL_AUTO=validate` kullanılır.
- Yeni schema değişikliği gerekiyorsa yeni bir Flyway migration dosyası eklenmelidir.

Local geliştirme sırasında temiz DB ile hızlı başlamak için `dev` profili kullanılabilir:

```env
SPRING_PROFILES_ACTIVE=dev
JPA_DDL_AUTO=update
```

## Testler

Tüm testleri çalıştır:

```bash
./mvnw test
```

Windows için:

```powershell
.\mvnw.cmd test
```

Temiz build ve test:

```powershell
.\mvnw.cmd clean test
```

Testlerde H2 in-memory database kullanılır. Test profili:

```text
src/test/resources/application-test.properties
```

Mevcut test kapsamı:

- Auth controller validation testleri
- JWT utility testleri
- Appointment service testleri
- Patient service testleri
- Auth service register/login/refresh/logout/reset password testleri
- Security error response ve CORS preflight testleri

## CORS

Angular frontend development adresi varsayılan olarak izinlidir:

```env
CORS_ALLOWED_ORIGINS=http://localhost:4200
```

Production domain geldiğinde bu değer gerçek frontend domaini ile değiştirilmelidir:

```env
CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com
```

## Faydalı Endpointler

```text
POST /api/auth/register
POST /api/auth/verify-email
POST /api/auth/login
POST /api/auth/refresh-token
POST /api/auth/logout
POST /api/auth/forgot-password
POST /api/auth/reset-password
GET  /api/departments
GET  /api/doctors
GET  /api/patients
GET  /api/appointments
GET  /api/medical-records
```

Detaylı request/response örnekleri Swagger üzerinden görülebilir.
