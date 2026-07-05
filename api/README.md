# SendMe — API Backend

API REST de SendMe, développée avec **Java 21** et **Spring Boot 3.2.5**.

---

## Stack technique

| Composant | Technologie |
|---|---|
| Langage | Java 21 |
| Framework | Spring Boot 3.2.5 |
| Build | Maven 3.9+ |
| Base de données | MySQL 8.0+ |
| Migrations | Flyway 9.22.3 (22 migrations V1–V22) |
| ORM | Spring Data JPA + Hibernate 6 |
| Sécurité | Spring Security 6 + JWT (jjwt 0.12.3) |
| Paiement | Stripe SDK Java 25.3.0 |
| Emails | Spring Mail + Thymeleaf (14 templates) |
| PDF | iText7 (itext7-core 7.2.5) |
| Taux de change | OpenExchangeRates API |
| Rate Limiting | Bucket4j 8.7.0 + Caffeine |
| Cache | Spring Cache + Caffeine |
| Documentation | SpringDoc OpenAPI 2.3.0 (Swagger UI) |
| Validation | Jakarta Validation |
| Internationalisation | Spring MessageSource (FR/EN) |
| Tests | JUnit 5, Mockito, AssertJ |
| Logs | SLF4J + Logback |

---

## Prérequis

- **Java 21** (ou supérieur)
- **Maven 3.9+**
- **MySQL 8.0+** (ou MariaDB 10.5+)
- **Compte Stripe** (mode Sandbox) — [stripe.com](https://stripe.com)
- **Clé API OpenExchangeRates** — [openexchangerates.org](https://openexchangerates.org)
- **Compte SMTP** (Mailtrap/Brevo pour le développement)

---

## Installation

### 1. Cloner le dépôt

```powershell
git clone https://github.com/dickoya/sendMe.git
cd sendMe/api
```

### 2. Configurer la base de données

Créer une base MySQL nommée `sendme_db` :

```sql
CREATE DATABASE sendme_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Configurer les propriétés locales

Créer `src/main/resources/application-local.properties` :

```properties
# ── Base de données ──────────────────────────────────────────────────────
spring.datasource.url=jdbc:mysql://localhost:3306/sendme_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=ton_mot_de_passe

# ── Stripe (mode Sandbox) ────────────────────────────────────────────────
stripe.api-key=pk_test_ta_cle_publishable
stripe.webhook.secret=whsec_ton_webhook_secret

# ── OpenExchangeRates ────────────────────────────────────────────────────
openexchangerates.api-key=ta_cle_api

# ── Email (Mailtrap / Brevo) ─────────────────────────────────────────────
spring.mail.host=smtp-relay.brevo.com
spring.mail.port=2525
spring.mail.username=ton_username
spring.mail.password=ton_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# ── Mode simulation des stratégies de transfert ──────────────────────────
strategy.simulation-mode=true
strategy.failure-rate=5
```

### 4. Lancer l'application

```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

L'API démarre sur `http://localhost:8080`.

---

## Structure du projet

```
api/src/main/java/ibrahima/dickoya/sendme/
├── annotation/          # @Auditable + AuditAspect (AOP)
├── config/              # 8 classes de configuration
│   ├── SecurityConfig.java          # Spring Security, CORS, JWT filter, Rate Limiting
│   ├── OpenApiConfig.java           # Swagger/OpenAPI documentation
│   ├── StripeConfig.java            # Stripe API key initialization
│   ├── CurrencyConfig.java          # OpenExchangeRates client
│   ├── CacheConfig.java             # Caffeine cache configuration
│   ├── AsyncConfig.java             # @Async for emails
│   ├── MessageSourceConfig.java     # i18n FR/EN
│   └── WebConfig.java               # CORS, converters
├── controller/          # 13 contrôleurs REST
│   ├── AuthController.java          # /api/auth (register, login, refresh, verify-email)
│   ├── TransferController.java      # /api/transfers, /api/transactions
│   ├── BeneficiaryController.java   # /api/beneficiaries (CRUD)
│   ├── NotificationController.java  # /api/notifications (SSE, historique)
│   ├── DashboardController.java     # /api/dashboard (stats, evolution, exchange rates)
│   ├── ProfileController.java       # /api/profile (update, password, delete, login-history)
│   ├── ReferralController.java      # /api/referral (code, coupons, discount, verify)
│   ├── CountryController.java       # /api/countries, /api/currencies (public)
│   ├── AdminController.java         # /api/admin (dashboard, users, transactions, config, logs, channels)
│   ├── ExchangeRateHistoryController.java  # /api/admin/exchange-rates/history
│   ├── StripeWebhookController.java # /api/webhooks/stripe
│   ├── DebugController.java         # /api/debug (rate, rates)
│   ├── EmailTestController.java     # /api/test/email (diagnostic)
│   └── RepairController.java        # /api/repair (à supprimer en production)
├── dto/                 # Request/Response DTOs
│   ├── request/         # 12 classes de requête
│   └── response/        # 18 classes de réponse
├── entity/              # 11 entités JPA
│   ├── User.java                    # Utilisateurs (BCrypt, email vérifié, parrainage)
│   ├── Beneficiary.java             # Bénéficiaires
│   ├── BeneficiaryChannel.java      # Canaux multiples par bénéficiaire (V15)
│   ├── Transaction.java             # Transactions (idempotence, Stripe, référence)
│   ├── Notification.java            # Notifications (SSE + historique)
│   ├── ReferralCode.java            # Codes de parrainage
│   ├── ReferralCoupon.java          # Coupons de réduction
│   ├── LoginHistory.java            # Historique des connexions (V19)
│   ├── AdminAuditLog.java           # Logs d'audit admin (V21)
│   ├── PaymentChannel.java          # Canaux de paiement dynamiques (V22)
│   └── ExchangeRate.java            # Taux de change historiques
├── enums/               # 4 énumérations
│   ├── ChannelType.java             # BANK_TRANSFER, ORANGE_MONEY, MTN_MOBILE_MONEY, WAVE, MOOV_MONEY, AIRTEL_MONEY
│   ├── TransactionStatus.java       # PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED
│   ├── NotificationType.java        # TRANSFER_*, PAYMENT_*, ACCOUNT_*, SECURITY_*, INFO, MAINTENANCE, FEE_UPDATE
│   └── Role.java                    # ROLE_USER, ROLE_ADMIN
├── exception/           # 7 exceptions personnalisées + GlobalExceptionHandler
│   ├── ResourceNotFoundException.java
│   ├── TransferException.java
│   ├── ReferralException.java
│   ├── EmailAlreadyVerifiedException.java
│   ├── InvalidTokenException.java
│   ├── AccountDisabledException.java
│   └── DuplicateEmailException.java
├── repository/          # 11 interfaces Spring Data JPA
│   ├── UserRepository.java
│   ├── BeneficiaryRepository.java
│   ├── TransactionRepository.java
│   ├── NotificationRepository.java
│   ├── ReferralCodeRepository.java
│   ├── ReferralCouponRepository.java
│   ├── LoginHistoryRepository.java
│   ├── AdminAuditLogRepository.java
│   ├── PaymentChannelRepository.java
│   ├── ExchangeRateRepository.java
│   └── BlockedIpRepository.java
├── security/            # JWT, Rate Limiting, UserDetails
│   ├── JwtService.java              # Génération/validation JWT (access 24h, refresh 7j)
│   ├── JwtAuthenticationFilter.java # Filtre d'extraction JWT
│   ├── RateLimitingFilter.java      # Bucket4j + exponential backoff
│   └── UserDetailsServiceImpl.java  # UserDetailsService personnalisé
├── service/             # 16 services métier
│   ├── AuthService.java             # Inscription, connexion, refresh, vérification email
│   ├── TransferService.java         # Orchestration complète des transferts
│   ├── SimulatorService.java        # Simulation des frais et taux de change
│   ├── BeneficiaryService.java      # CRUD bénéficiaires
│   ├── NotificationService.java     # Création et envoi de notifications
│   ├── SseEmitterService.java       # Gestion des connexions SSE
│   ├── DashboardService.java        # Statistiques utilisateur
│   ├── ProfileService.java          # Gestion du profil
│   ├── ReferralService.java         # Parrainage et coupons
│   ├── AdminService.java            # Logique métier admin
│   ├── AdminAuditLogService.java    # Consultation des logs d'audit
│   ├── ExchangeRateService.java     # Taux de change (cache + marge 2%)
│   ├── StripePaymentService.java    # Paiement Stripe (débit, confirmation, remboursement)
│   ├── StripeWebhookService.java    # Traitement des webhooks Stripe
│   ├── EmailService.java            # Envoi d'emails asynchrones (14 templates)
│   ├── ReceiptPdfService.java       # Génération de reçus PDF (iText7)
│   └── PaymentChannelService.java   # Gestion dynamique des canaux de paiement
├── strategy/            # Strategy Pattern pour les transferts
│   ├── TransferStrategy.java        # Interface commune
│   ├── BaseTransferStrategy.java    # Template Method (simulation, délais, masquage)
│   ├── TransferResult.java          # Objet résultat
│   ├── BankTransferStrategy.java    # Virement bancaire SEPA/SWIFT
│   ├── OrangeMoneyStrategy.java     # Orange Money
│   ├── MtnMobileMoneyStrategy.java  # MTN Mobile Money
│   ├── WaveStrategy.java            # Wave
│   ├── MoovMoneyStrategy.java       # Moov Money
│   └── AirtelMoneyStrategy.java     # Airtel Money
└── utils/               # 3 utilitaires
    ├── CurrencyByCountry.java       # Mapping pays → devise (ISO 3166-1 alpha-3 → ISO 4217)
    ├── DataMaskingUtils.java        # Masquage téléphone, IBAN, carte, email
    └── MessageUtils.java            # Accès simplifié aux messages i18n
```

---

## Ressources

```
api/src/main/resources/
├── db/migration/       # 22 migrations Flyway (V1__init.sql → V22__payment_channels.sql)
├── templates/          # 14 templates email Thymeleaf
│   ├── email-verification.html
│   ├── transfer-initiated.html
│   ├── transfer-completed.html
│   ├── transfer-failed.html
│   ├── transfer-refunded.html
│   ├── maintenance-notification.html
│   ├── welcome.html
│   ├── password-changed.html
│   ├── account-deleted.html
│   ├── account-suspended.html
│   ├── account-reactivated.html
│   ├── suspicious-login.html
│   ├── referral-coupon.html
│   └── test-email.html
├── static/             # Logos des opérateurs Mobile Money
├── messages_fr.properties   # Messages backend en français
├── messages_en.properties   # Messages backend en anglais
└── application.properties   # Configuration principale
```

---

## Endpoints API

### Authentification (`/api/auth`)

| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/auth/register` | Inscription | ❌ |
| POST | `/api/auth/login` | Connexion | ❌ |
| POST | `/api/auth/refresh` | Rafraîchir les tokens | ❌ |
| GET | `/api/auth/verify-email` | Vérifier l'email | ❌ |
| POST | `/api/auth/resend-verification` | Renvoyer l'email de vérification | ❌ |
| POST | `/api/auth/test-email` | Test de configuration email | ❌ |

### Transferts (`/api/transfers`)

| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/transfers/simulate` | Simuler les frais | ❌* |
| POST | `/api/transfers` | Initier un transfert | ✅ |

*\*Public mais détecte le JWT pour appliquer la réduction de parrainage*

### Transactions (`/api/transactions`)

| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/transactions` | Historique paginé | ✅ |
| GET | `/api/transactions/{id}` | Détail d'une transaction | ✅ |
| GET | `/api/transactions/{id}/receipt` | Télécharger le reçu PDF | ✅ |
| GET | `/api/transactions/{id}/receipt/preview` | Prévisualiser le reçu PDF | ✅ |

### Bénéficiaires (`/api/beneficiaries`)

| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/beneficiaries` | Liste paginée | ✅ |
| POST | `/api/beneficiaries` | Créer un bénéficiaire | ✅ |
| PUT | `/api/beneficiaries/{id}` | Modifier un bénéficiaire | ✅ |
| DELETE | `/api/beneficiaries/{id}` | Supprimer un bénéficiaire | ✅ |

### Notifications (`/api/notifications`)

| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/notifications/stream` | Flux SSE temps réel | ✅ |
| GET | `/api/notifications` | Historique paginé | ✅ |
| GET | `/api/notifications/unread-count` | Nombre de notifications non lues | ✅ |
| PUT | `/api/notifications/{id}/read` | Marquer comme lue | ✅ |
| PUT | `/api/notifications/read-all` | Tout marquer comme lu | ✅ |

### Dashboard (`/api/dashboard`)

| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/dashboard/stats` | Statistiques personnelles | ✅ |
| GET | `/api/dashboard/evolution` | Évolution des transferts | ✅ |
| GET | `/api/dashboard/exchange-rates` | Taux de change en direct | ✅ |
| GET | `/api/dashboard/exchange-rates/history/{from}/{to}` | Historique des taux (7 jours) | ✅ |

### Profil (`/api/profile`)

| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/profile` | Récupérer le profil | ✅ |
| PUT | `/api/profile` | Mettre à jour le profil | ✅ |
| PUT | `/api/profile/password` | Changer le mot de passe | ✅ |
| DELETE | `/api/profile` | Supprimer le compte | ✅ |
| GET | `/api/profile/login-history` | Historique des connexions | ✅ |

### Parrainage (`/api/referral`)

| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/referral/my-code` | Mon code de parrainage | ✅ |
| GET | `/api/referral/coupons` | Mes coupons de réduction | ✅ |
| GET | `/api/referral/discount` | Montant de réduction disponible | ✅ |
| GET | `/api/referral/verify` | Vérifier un code de parrainage | ❌ |

### Pays et Devises (`/api`)

| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/countries` | Liste des pays supportés | ❌ |
| GET | `/api/currencies` | Liste des devises supportées | ❌ |

### Administration (`/api/admin`)

| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/admin/dashboard` | Tableau de bord | ADMIN |
| GET | `/api/admin/users` | Liste des utilisateurs | ADMIN |
| GET | `/api/admin/users/{id}` | Détail d'un utilisateur | ADMIN |
| PUT | `/api/admin/users/{id}/toggle` | Activer/désactiver un compte | ADMIN |
| GET | `/api/admin/users/filter` | Utilisateurs filtrés | ADMIN |
| POST | `/api/admin/users/batch-toggle` | Actions en masse | ADMIN |
| GET | `/api/admin/transactions` | Toutes les transactions | ADMIN |
| PUT | `/api/admin/config/fees` | Modifier le taux de commission | ADMIN |
| POST | `/api/admin/notifications/maintenance` | Notifier une maintenance | ADMIN |
| GET | `/api/admin/logs` | Logs d'activité admin | ADMIN |
| GET | `/api/admin/analytics/volume` | Volume par jour | ADMIN |
| GET | `/api/admin/analytics/success-rate` | Taux de succès par jour | ADMIN |
| GET | `/api/admin/payment-channels` | Liste des canaux de paiement | ADMIN |
| POST | `/api/admin/payment-channels` | Ajouter un canal de paiement | ADMIN |
| PUT | `/api/admin/payment-channels/{id}/toggle` | Activer/désactiver un canal | ADMIN |
| GET | `/api/admin/exchange-rates/history` | Historique des taux de change | ADMIN |
| GET | `/api/admin/exchange-rates/history/{from}/{to}` | Historique filtré par devises | ADMIN |

### Debug (`/api/debug`)

| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/debug/rate` | Debug taux de change | ❌ |
| GET | `/api/debug/rates` | Debug tous les taux | ❌ |

### Webhooks (`/api/webhooks`)

| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/webhooks/stripe` | Webhook Stripe | Signature |

### Test (`/api/test`)

| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/test/email` | Test email asynchrone | ❌ |
| POST | `/api/test/email/diagnostic` | Test email synchrone | ❌ |

### Repair (`/api/repair`)

| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/repair/create-exchange-rates-table` | Créer la table exchange_rates | ❌ |

---

## Migrations Flyway

22 migrations de V1 à V22 :

| Migration | Description |
|---|---|
| V1 | Structure initiale (users, beneficiaries, transactions, notifications) |
| V2 | Ajout des colonnes de vérification email |
| V3 | Ajout des colonnes de parrainage |
| V4 | Ajout des colonnes de suppression de compte |
| V5 | Ajout des colonnes de notification |
| V6 | Ajout des colonnes de bénéficiaire |
| V7 | Ajout des colonnes de transaction |
| V8 | Ajout des colonnes de notification |
| V9 | Ajout des colonnes de parrainage |
| V10 | Table exchange_rates |
| V11 | Table blocked_ips + rate limiting |
| V12 | Ajout des colonnes de bénéficiaire |
| V13 | Ajout des colonnes de transaction |
| V14 | Ajout des colonnes de notification |
| V15 | Table beneficiary_channels (multi-canaux) |
| V16 | Ajout des colonnes de parrainage |
| V17 | Ajout des colonnes de transaction |
| V18 | Ajout des colonnes de notification |
| V19 | Table login_history |
| V20 | Ajout des colonnes de vérification email |
| V21 | Table admin_audit_log |
| V22 | Table payment_channels |

---

## Configuration

### Profils Spring

| Profil | Usage |
|---|---|
| `local` | Développement local (application-local.properties) |
| `dev` | Développement partagé |
| `prod` | Production |

### Propriétés principales

```properties
# Application
spring.application.name=sendme-api
server.port=8080

# JWT
app.jwt.secret=ta_cle_secrete_jwt
app.jwt.access-token-expiration=86400000    # 24h
app.jwt.refresh-token-expiration=604800000  # 7j

# Stripe
stripe.api-key=pk_test_...
stripe.webhook.secret=whsec_...

# OpenExchangeRates
openexchangerates.api-key=ta_cle
openexchangerates.base-url=https://openexchangerates.org/api
openexchangerates.margin-percent=2.0

# CORS
app.cors.allowed-origins=http://localhost:5173

# Simulation
strategy.simulation-mode=true
strategy.failure-rate=5

# Rate Limiting
rate-limiting.max-attempts=5
rate-limiting.window-minutes=15
rate-limiting.block-duration-minutes=60
```

---

## Tests

```powershell
# Tests unitaires
mvn test

# Tests avec couverture
mvn verify
```

---

## Documentation Swagger

Une fois l'API démarrée :

- **Swagger UI** : [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **Spécification OpenAPI** : [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## Fichiers de test HTTP

Le dossier `api/http/` contient des fichiers `.http` utilisables avec l'extension REST Client de VS Code pour tester tous les endpoints sans Swagger.

---

## Auteur

**Ibrahima Dickoya** — TFE 2024–2025
