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
| Migrations | Flyway (26 migrations V1–V26) |
| ORM | Spring Data JPA + Hibernate 6 |
| Sécurité | Spring Security 6 + JWT (jjwt 0.12.6) |
| Paiement | Stripe SDK Java |
| Emails | API Brevo (HTTPS) + Thymeleaf (templates sous `templates/emails/`) |
| PDF | iText7 (itext7-core 7.2.5) |
| Taux de change | OpenExchangeRates API |
| Rate Limiting | Bucket4j + Caffeine |
| Cache | Spring Cache + Caffeine |
| Documentation | SpringDoc OpenAPI (Swagger UI) |
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
- **Clé API Brevo** (envoi d'emails via l'API HTTPS) — [brevo.com](https://www.brevo.com)

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
stripe.secret.key=sk_test_ta_cle_secrete
stripe.publishable.key=pk_test_ta_cle_publishable
stripe.webhook.secret=whsec_ton_webhook_secret

# ── OpenExchangeRates ────────────────────────────────────────────────────
app.openexchangerates.app-id=ta_cle_api

# ── Email (API Brevo) ─────────────────────────────────────────────────────
# Railway bloque les ports SMTP sortants → envoi via l'API Brevo en HTTPS.
brevo.api-key=xkeysib-ta_cle_api_brevo
app.mail.from=ton_expediteur_verifie@example.com
app.mail.name=SendMe

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
├── config/              # Configurations (Security, Stripe, Swagger, Currency, Scheduling, MessageSource)
├── controller/          # Contrôleurs REST
│   ├── AuthController.java             # /api/auth (register, login, refresh, verify-email, resend-verification)
│   ├── TransferController.java         # /api/transfers, /api/transactions, /api/channels/active
│   ├── BeneficiaryController.java      # /api/beneficiaries (CRUD)
│   ├── NotificationController.java     # /api/notifications (SSE, historique, read, read-all, unread-count)
│   ├── DashboardController.java        # /api/dashboard (stats, evolution, exchange rates)
│   ├── ProfileController.java          # /api/profile (update, password, delete, login-history)
│   ├── ReferralController.java         # /api/referral (code, coupons, discount, verify)
│   ├── CountryController.java          # /api/countries, /api/currencies (public)
│   ├── AdminController.java            # /api/admin (dashboard, users, transactions, config, logs, channels, analytics)
│   ├── ExchangeRateHistoryController.java  # /api/admin/exchange-rates/history
│   ├── AuditLogController.java         # /api/admin/audit-logs
│   ├── StripeWebhookController.java    # /api/webhooks/stripe
│   └── ...
├── dto/                 # Request/Response DTOs
├── entity/              # Entités JPA (User, Beneficiary, BeneficiaryChannel, Transaction, Notification, ReferralCoupon, LoginHistory, AuditLog, PaymentChannel, ExchangeRate, BlockedIp)
├── enums/               # ChannelType, TransactionStatus, NotificationType, Role
├── exception/           # Exceptions personnalisées + GlobalExceptionHandler
├── repository/          # Interfaces Spring Data JPA
├── security/            # JwtService, JwtAuthenticationFilter, RateLimitingFilter, UserDetailsServiceImpl
├── service/             # Services métier (Auth, Transfer, Simulator, Beneficiary, Notification, SSE, Dashboard, Profile, Referral, Admin, Audit, ExchangeRate, StripePayment, StripeWebhook, Email, ReceiptPdf, PaymentChannel, LoginAttempt, IpBlocking, RefreshToken, TransactionPersistence)
├── strategy/            # TransferStrategy + BaseTransferStrategy + 6 implémentations (Bank, Orange, MTN, Wave, Moov, Airtel) + TransferResult
└── utils/               # CurrencyByCountry, DataMaskingUtils, MessageUtils
```

---

## Ressources

```
api/src/main/resources/
├── db/migration/       # 26 migrations Flyway (V1__... → V26__...)
├── templates/emails/   # Templates email Thymeleaf
├── static/             # Logos des opérateurs Mobile Money
├── messages_fr.properties   # Messages backend en français
├── messages_en.properties   # Messages backend en anglais
└── application.properties   # Configuration principale
```

---

## Migrations Flyway

26 migrations de V1 à V26 :

| Migration | Description |
|---|---|
| V1 | Table users |
| V2 | Table beneficiaries |
| V3 | Table bank_details |
| V4 | Table mobile_money_details |
| V5 | Table transactions |
| V6 | Table notifications |
| V7 | Colonnes Stripe sur transactions |
| V8 | Colonne type sur notifications |
| V9 | Insertion admin par défaut |
| V10 | Table exchange_rates |
| V11 | Table blocked_ips |
| V12 | Système de parrainage |
| V13 | Support multi-devises |
| V14 | Extension colonne statut Stripe |
| V15 | Table beneficiary_channels |
| V16 | Nettoyage tables obsolètes |
| V17 | Colonne discount sur transactions |
| V18 | Suppression colonne canal sur bénéficiaires |
| V19 | Table login_history |
| V20 | Vérification email |
| V21 | Table admin_audit_log |
| V22 | Table audit_logs |
| V23 | Table payment_channels |
| V24 | Suppression admin_audit_log |
| V25 | Réinitialisation du mot de passe |
| V26 | Correction du mot de passe admin par défaut |

---

## Endpoints API

### Authentification (`/api/auth`)

| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/auth/register` | Inscription | Non |
| POST | `/api/auth/login` | Connexion | Non |
| POST | `/api/auth/refresh` | Rafraîchir les tokens | Non |
| GET | `/api/auth/verify-email` | Vérifier l'email | Non |
| POST | `/api/auth/resend-verification` | Renvoyer l'email de vérification | Non |

### Transferts (`/api/transfers`)

| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/transfers/simulate` | Simuler les frais | Non* |
| POST | `/api/transfers` | Initier un transfert | Oui |

*\*Public mais détecte le JWT pour appliquer la réduction de parrainage*

### Transactions (`/api/transactions`)

| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/transactions` | Historique paginé | Oui |
| GET | `/api/transactions/{id}` | Détail d'une transaction | Oui |
| GET | `/api/transactions/{id}/receipt` | Télécharger le reçu PDF | Oui |
| GET | `/api/transactions/{id}/receipt/preview` | Prévisualiser le reçu PDF | Oui |

### Canaux de paiement (`/api/channels`)

| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/channels/active` | Canaux de paiement actifs | Non |

### Bénéficiaires (`/api/beneficiaries`)

| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/beneficiaries` | Liste paginée | Oui |
| POST | `/api/beneficiaries` | Créer un bénéficiaire | Oui |
| PUT | `/api/beneficiaries/{id}` | Modifier un bénéficiaire | Oui |
| DELETE | `/api/beneficiaries/{id}` | Supprimer un bénéficiaire | Oui |

### Notifications (`/api/notifications`)

| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/notifications/stream` | Flux SSE temps réel | Oui |
| GET | `/api/notifications` | Historique paginé | Oui |
| GET | `/api/notifications/unread-count` | Nombre de notifications non lues | Oui |
| PUT | `/api/notifications/{id}/read` | Marquer comme lue | Oui |
| PUT | `/api/notifications/read-all` | Tout marquer comme lu | Oui |

### Dashboard (`/api/dashboard`)

| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/dashboard/stats` | Statistiques personnelles | Oui |
| GET | `/api/dashboard/evolution` | Évolution des transferts | Oui |
| GET | `/api/dashboard/exchange-rates` | Taux de change en direct | Oui |
| GET | `/api/dashboard/exchange-rates/history/{from}/{to}` | Historique des taux | Oui |

### Profil (`/api/profile`)

| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/profile` | Récupérer le profil | Oui |
| PUT | `/api/profile` | Mettre à jour le profil | Oui |
| PUT | `/api/profile/password` | Changer le mot de passe | Oui |
| DELETE | `/api/profile` | Supprimer le compte | Oui |
| GET | `/api/profile/login-history` | Historique des connexions | Oui |

### Parrainage (`/api/referral`)

| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/referral/my-code` | Mon code de parrainage | Oui |
| GET | `/api/referral/coupons` | Mes coupons de réduction | Oui |
| GET | `/api/referral/discount` | Montant de réduction disponible | Oui |
| GET | `/api/referral/verify` | Vérifier un code de parrainage | Non |

### Pays et Devises (`/api`)

| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/countries` | Liste des pays supportés | Non |
| GET | `/api/currencies` | Liste des devises supportées | Non |

### Administration (`/api/admin`)

| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/admin/dashboard` | Tableau de bord | ADMIN |
| GET | `/api/admin/users` | Liste des utilisateurs | ADMIN |
| GET | `/api/admin/users/{id}` | Détail d'un utilisateur | ADMIN |
| PUT | `/api/admin/users/{id}/toggle` | Activer/désactiver un compte | ADMIN |
| PUT | `/api/admin/users/{id}/role` | Modifier le rôle (promouvoir/rétrograder admin) | ADMIN |
| GET | `/api/admin/users/filter` | Utilisateurs filtrés | ADMIN |
| POST | `/api/admin/users/batch-toggle` | Actions en masse | ADMIN |
| GET | `/api/admin/transactions` | Toutes les transactions | ADMIN |
| PUT | `/api/admin/config/fees` | Modifier le taux de commission | ADMIN |
| POST | `/api/admin/notifications/maintenance` | Notifier une maintenance | ADMIN |
| GET | `/api/admin/logs` | Logs d'activité admin | ADMIN |
| GET | `/api/admin/audit-logs` | Logs d'audit (AuditLog) | ADMIN |
| GET | `/api/admin/analytics/volume` | Volume par jour | ADMIN |
| GET | `/api/admin/analytics/success-rate` | Taux de succès par jour | ADMIN |
| GET | `/api/admin/payment-channels` | Liste des canaux de paiement | ADMIN |
| POST | `/api/admin/payment-channels` | Ajouter un canal de paiement | ADMIN |
| PUT | `/api/admin/payment-channels/{id}/toggle` | Activer/désactiver un canal | ADMIN |
| GET | `/api/admin/exchange-rates/history` | Historique des taux de change | ADMIN |
| GET | `/api/admin/exchange-rates/history/{from}/{to}` | Historique filtré par devises | ADMIN |

### Webhooks (`/api/webhooks`)

| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/webhooks/stripe` | Webhook Stripe | Signature |

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
spring.application.name=sendme
server.port=8080

# JWT
app.jwt.secret=ta_cle_secrete_jwt
app.jwt.expiration=86400000          # 24h
app.jwt.refresh-expiration=604800000 # 7j

# Stripe
stripe.secret.key=sk_test_...
stripe.publishable.key=pk_test_...
stripe.webhook.secret=whsec_...

# OpenExchangeRates
app.openexchangerates.app-id=ta_cle

# CORS
app.cors.allowed-origins=http://localhost:5173

# Simulation
strategy.simulation-mode=true
strategy.failure-rate=5
```

---

## Déploiement (Railway)

Le backend est déployé sur **Railway** avec la base **MySQL Railway** (service MySQL intégré).

- **Root Directory** : `api`
- **Builder** : Railpack / Nixpacks
- **Start Command** : `java -jar target/sendme-0.0.1-SNAPSHOT.jar`

**Variables Railway (Backend) :**

```env
# Base de données (Railway MySQL — injectées automatiquement par le plugin MySQL)
# MYSQLHOST, MYSQLPORT, MYSQLDATABASE, MYSQLUSER, MYSQLPASSWORD
FRONTEND_URL=https://<frontend-url>
CORS_ALLOWED_ORIGINS=https://<frontend-url>
STRIPE_RETURN_URL=https://<frontend-url>/payment/callback
JWT_SECRET=<une chaîne aléatoire ≥32 caractères>
STRIPE_SECRET_KEY=sk_test_...
STRIPE_PUBLISHABLE_KEY=pk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...
OPENEXCHANGE_APP_ID=...
# Email (API Brevo — Railway bloque les ports SMTP sortants, on envoie via l'API HTTPS)
BREVO_API_KEY=xkeysib-...
APP_MAIL_FROM=<expéditeur vérifié dans Brevo>
APP_MAIL_NAME=SendMe
```

Au premier démarrage, **Flyway** applique les migrations (V1 → V26). L'API écoute sur le port injecté par Railway (`server.port=${PORT:8080}` dans `application.properties`).

> **Base de données MySQL 8.4+** : les requêtes natives doivent utiliser `CAST(... AS SIGNED)` (MySQL ne supporte pas `AS INTEGER`).

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

**Ibrahima Dickoya** — TFE 2025–2026
