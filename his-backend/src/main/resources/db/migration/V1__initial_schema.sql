-- =============================================================================
-- V1: Hospital Information System — Initial Schema
-- =============================================================================
-- Bu migration, HIS uygulamasının tüm temel şemasını sıfırdan oluşturur.
-- Tablo oluşturma sırası, yabancı anahtar (FK) bağımlılıklarına göre
-- düzenlenmiştir: bağımsız tablolar önce, bağımlı tablolar sonra.
--
-- Bağımlılık Zinciri:
--   roles
--   users ──────────────────────────────────────┐
--   user_roles (users ↔ roles)                  │
--   departments                                 │
--   patients    (users)                         │
--   doctors     (users, departments)            │
--   appointments (patients, doctors, users)     │
--   medical_records (appointments)              │
--   refresh_tokens  (users)                     │
--   account_tokens  (users)                     │
-- =============================================================================


-- -----------------------------------------------------------------------------
-- 1. ROLLER (roles)
--    Sistemdeki kullanıcı tiplerini tanımlar.
--    Seeder (DataSeeder.java) uygulama ayağa kalkarken bu tabloya
--    ADMIN, DOCTOR, RECEPTIONIST, PATIENT rollerini ekler.
-- -----------------------------------------------------------------------------
create table if not exists roles (
    id   bigserial    primary key,
    name varchar(255) not null unique  -- Enum değeri: ADMIN | DOCTOR | RECEPTIONIST | PATIENT
);


-- -----------------------------------------------------------------------------
-- 2. KULLANICILAR (users)
--    Sisteme giriş yapabilecek tüm hesapları tutar.
--    Her kullanıcı bir veya daha fazla role sahip olabilir (user_roles üzerinden).
--    is_active   : Soft-delete mekanizması; false olan hesaplar login yapamaz.
--    email_verified : E-posta doğrulaması tamamlanmadan login engellidir.
-- -----------------------------------------------------------------------------
create table if not exists users (
    id             bigserial    primary key,
    username       varchar(50)  not null unique,
    password       varchar(255) not null,            -- BCrypt hash
    email          varchar(100) not null unique,
    is_active      boolean      default true,        -- false = hesap pasif (soft delete)
    email_verified boolean      not null default false, -- false = e-posta henüz doğrulanmamış
    created_at     timestamp,
    updated_at     timestamp
);


-- -----------------------------------------------------------------------------
-- 3. KULLANICI — ROL İLİŞKİSİ (user_roles)
--    ManyToMany köprü tablosu. Bir kullanıcı aynı anda birden fazla role
--    sahip olabilir (örn: ADMIN + DOCTOR).
-- -----------------------------------------------------------------------------
create table if not exists user_roles (
    user_id bigint not null,
    role_id bigint not null,
    primary key (user_id, role_id),
    constraint fk_user_roles_user foreign key (user_id) references users (id),
    constraint fk_user_roles_role foreign key (role_id) references roles (id)
);


-- -----------------------------------------------------------------------------
-- 4. DEPARTMANLAR (departments)
--    Hastanedeki klinikleri/bölümleri temsil eder (Kardiyoloji, Ortopedi vb.).
--    Her doktorun bağlı olduğu bir departmanı vardır.
-- -----------------------------------------------------------------------------
create table if not exists departments (
    id          bigserial    primary key,
    name        varchar(100) not null unique,
    description text,
    created_at  timestamp,
    updated_at  timestamp
);


-- -----------------------------------------------------------------------------
-- 5. HASTALAR (patients)
--    Sisteme kayıtlı hasta profillerini tutar. Her hasta bir user hesabıyla
--    bire-bir ilişkilendirilir (OneToOne). T.C. Kimlik No sistemde benzersizdir.
--    is_deleted : Soft delete mekanizması; ilişkili randevular korunur.
-- -----------------------------------------------------------------------------
create table if not exists patients (
    id                bigserial   primary key,
    user_id           bigint      not null unique,   -- OneToOne: her user yalnızca bir hasta profili
    first_name        varchar(50) not null,
    last_name         varchar(50) not null,
    tc_no             varchar(11) not null unique,   -- 11 haneli T.C. Kimlik No
    birth_date        date,
    gender            varchar(10),                   -- Enum: MALE | FEMALE | OTHER
    phone             varchar(15),
    email             varchar(100),
    address           text,
    emergency_contact varchar(100),
    blood_type        varchar(5),                    -- Örn: A+, B-, 0+
    is_deleted        boolean     default false,     -- false = aktif hasta profili
    created_at        timestamp,
    updated_at        timestamp,
    constraint fk_patients_user foreign key (user_id) references users (id)
);


-- -----------------------------------------------------------------------------
-- 6. DOKTORLAR (doctors)
--    Hastanede çalışan doktorların profillerini tutar. Her doktor bir user
--    hesabıyla bire-bir, bir departmanla ise bire-çok ilişkilidir.
-- -----------------------------------------------------------------------------
create table if not exists doctors (
    id             bigserial    primary key,
    user_id        bigint       not null unique,  -- OneToOne: her user yalnızca bir doktor profili
    department_id  bigint       not null,         -- Doktorun bağlı olduğu klinik/departman
    first_name     varchar(50)  not null,
    last_name      varchar(50)  not null,
    specialization varchar(100),                 -- Uzmanlık alanı (Örn: Kardiyoloji)
    phone          varchar(15),
    created_at     timestamp,
    updated_at     timestamp,
    constraint fk_doctors_user       foreign key (user_id)       references users (id),
    constraint fk_doctors_department foreign key (department_id) references departments (id)
);


-- -----------------------------------------------------------------------------
-- 7. RANDEVULAR (appointments)
--    Hasta-Doktor randevularını tutar. Temel iş akışı:
--      SCHEDULED → COMPLETED (tıbbi kayıt oluşturulunca otomatik)
--      SCHEDULED → CANCELED  (hasta veya yetkili kullanıcı tarafından)
--
--    created_by_user_id: Randevuyu sisteme kim girdi (resepsiyon, hasta vb.)
--    uk_appointments_doctor_date_time: Aynı doktorun aynı saat-tarihte
--      iki randevusu olamaz (çakışma engeli — DB seviyesi güvence).
-- -----------------------------------------------------------------------------
create table if not exists appointments (
    id                  bigserial   primary key,
    patient_id          bigint      not null,
    doctor_id           bigint      not null,
    appointment_date    date        not null,
    appointment_time    time        not null,
    status              varchar(20) not null,    -- Enum: SCHEDULED | COMPLETED | CANCELED
    notes               text,
    created_by_user_id  bigint,                 -- Nullable: kim oluşturdu (audit)
    created_at          timestamp,
    updated_at          timestamp,
    constraint fk_appointments_patient         foreign key (patient_id)         references patients (id),
    constraint fk_appointments_doctor          foreign key (doctor_id)          references doctors (id),
    constraint fk_appointments_created_by_user foreign key (created_by_user_id) references users (id),
    -- Doktor çakışma engeli: aynı doktor, aynı gün, aynı saatte yalnızca bir randevu
    constraint uk_appointments_doctor_date_time unique (doctor_id, appointment_date, appointment_time)
);


-- -----------------------------------------------------------------------------
-- 8. TIBBİ KAYITLAR (medical_records)
--    Her randevu için en fazla bir tıbbi kayıt oluşturulabilir (OneToOne).
--    Teşhis, tedavi ve reçete bilgilerini saklar. Kayıt oluşturulduğunda
--    ilgili randevu otomatik olarak COMPLETED statüsüne geçer.
--    NOT: patient_id ve doctor_id bu tabloda tutulmaz; appointment üzerinden
--    erişilir (V2 migration'ı bu gereksiz kolonları kaldırmıştır).
-- -----------------------------------------------------------------------------
create table if not exists medical_records (
    id                 bigserial primary key,
    appointment_id     bigint    not null unique,  -- OneToOne: bir randevunun tek kaydı olabilir
    diagnosis          text,
    treatment_notes    text,
    prescription_notes text,
    created_at         timestamp,
    updated_at         timestamp,
    constraint fk_medical_records_appointment foreign key (appointment_id) references appointments (id)
);


-- -----------------------------------------------------------------------------
-- 9. YENİLEME TOKEN'LARI (refresh_tokens)
--    JWT access token'larının yenilenmesi için kullanılan uzun ömürlü
--    token'ları saklar. Güvenlik için:
--      • Veritabanında yalnızca SHA-256 hash'i saklanır (ham token asla yazılmaz).
--      • Her kullanımda rotation: eski token revoke edilir, yeni token üretilir.
--      • Süresi dolan/iptal edilen token'lar RefreshTokenCleanupService tarafından
--        periyodik olarak temizlenir.
-- -----------------------------------------------------------------------------
create table if not exists refresh_tokens (
    id         bigserial   primary key,
    user_id    bigint      not null,
    token_hash varchar(64) not null unique,  -- SHA-256 hex (64 karakter)
    expires_at timestamp   not null,
    revoked    boolean     not null default false,
    created_at timestamp,
    constraint fk_refresh_tokens_user foreign key (user_id) references users (id)
);

-- Kullanıcının aktif token'larını hızlı sorgulayabilmek için index
create index if not exists idx_refresh_tokens_user_id
    on refresh_tokens (user_id);


-- -----------------------------------------------------------------------------
-- 10. HESAP TOKEN'LARI (account_tokens)
--     E-posta doğrulama ve şifre sıfırlama akışları için tek kullanımlık
--     token'ları saklar. Güvenlik için:
--       • Veritabanında yalnızca SHA-256 hash'i saklanır.
--       • Token bir kez kullanıldığında used_at doldurulur (yeniden kullanım engeli).
--       • token_type alanı ile hangi amaçla üretildiği ayrıştırılır:
--           EMAIL_VERIFICATION | PASSWORD_RESET
-- -----------------------------------------------------------------------------
create table if not exists account_tokens (
    id         bigserial   primary key,
    user_id    bigint      not null,
    token_type varchar(30) not null,         -- Enum: EMAIL_VERIFICATION | PASSWORD_RESET
    token_hash varchar(64) not null unique,  -- SHA-256 hex (64 karakter)
    expires_at timestamp   not null,
    used_at    timestamp,                    -- null = henüz kullanılmamış
    created_at timestamp,
    constraint fk_account_tokens_user foreign key (user_id) references users (id)
);

-- Kullanıcı + token tipi kombinasyonu üzerinden hızlı arama için bileşik index
create index if not exists idx_account_tokens_user_type
    on account_tokens (user_id, token_type);
