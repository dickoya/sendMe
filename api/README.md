# SendMe — Backend (Spring Boot)

API RESTful de la passerelle de transfert d'argent multi-canaux SendMe.

**Localisation** : `C:\xamp\htdocs\sendMe\api`

## Stack technique

| Technologie | Usage |
|---|---|
| Java 21 | Langage |
| Spring Boot 3.2.5 | Framework |
| Maven | Build |
| MySQL | Base de données |
| Flyway | Migrations DB (V1–V14) |
| Spring Security + JWT | Authentification |
| Stripe SDK 25.3.0 | Paiement (Sandbox) |
| Spring Mail + Thymeleaf | Emails (Mailtrap) |
| SSE (Server-Sent Events) | Temps réel |
| iText7 | Génération PDF |
| OpenExchangeRates API | Taux de change |
| Bucket4j + Caffeine | Rate Limiting |
| Swagger/OpenAPI | Documentation interactive |

## Prérequis

- **Java 21** (JDK)
- **Maven 3.9+**
- **MySQL 8+**
- Compte **Stripe** (mode Sandbox)
- Compte **Mailtrap** (Sandbox emails)
- Clé API **OpenExchangeRates**

## Installation rapide

### 1. Configurer la base de données

Créer la base MySQL :

```sql
CREATE DATABASE sendme_db;
```

### 2. Configurer les propriétés

Copier le fichier d'exemple :

```powershell
copy .env.dev .env
```

Configurer `application-local.properties` (ignoré par Git) :

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sendme_db
spring.datasource.username=root
spring.datasource.password=votre_mot_de_passe

stripe.secret-key=sk_test_votre_cle_stripe
stripe.webhook-secret=whsec_votre_secret_webhook

spring.mail.username=votre_mailtrap_username
spring.mail.password=votre_mailtrap_password

app.openexchangerates.app-id=votre_cle_openexchangerates
```

### 3. Lancer l'application

```powershell
mvn spring-boot:run
```

L'API est disponible sur `http://localhost:8080`.

## Documentation interactive (Swagger UI)

Une fois le backend lancé :

```
http://localhost:8080/swagger-ui/index.html
```

## Migrations Flyway

Les migrations sont exécutées automatiquement au démarrage. Historique complet :

| Version | Contenu |
|---|---|
| V1 | Table `users` |
| V2 | Table `beneficiaries` |
| V3 | Tables `bank_details` et `mobile_money_details` |
| V4 | Table `transactions` |
| V5 | Table `notifications` |
| V6 | Compte admin + config frais |
| V7 | Champs Stripe sur transactions |
| V8 | Ajustements NotificationType |
| V9 | Contraintes et index |
| V10 | Table `exchange_rates` |
| V11 | Table `blocked_ips` |
| V12 | Table `referral_coupons` + colonnes parrainage sur users |
| V13 | Support multi-devises |
| V14 | Augmentation colonne stripe_payment_status |

## Structure du projet

```
api/src/main/java/ibrahima/dickoya/sendme/
├── SendMeApplication.java
├── config/           # Configurations (Security, Stripe, Swagger, Currency, Scheduling, FlywayRepair)
├── controller/       # Endpoints REST (Auth, Beneficiary, Transfer, Transaction, Notification,
│                     #   Dashboard, Profile, Referral, Admin, Debug, StripeWebhook)
├── dto/              # Request/Response DTOs
├── entity/           # Entités JPA (User, Beneficiary, BankDetails, MobileMoneyDetails,
│                     #   Transaction, Notification, ExchangeRate, BlockedIp, ReferralCoupon, LoginHistory)
├── repository/       # Interfaces Spring Data JPA
├── security/         # JWT, RateLimitingFilter
├── service/          # Logique métier (Auth, Beneficiary, Transfer, Simulator, StripePayment,
│                     #   Notification, SseEmitter, Email, ReceiptPdf, Dashboard, Profile,
│                     #   Referral, ExchangeRate, TransactionPersistence, Admin)
└── strategy/         # TransferStrategy + 6 implémentations
```

## Scripts Maven

| Commande | Description |
|---|---|
| `mvn spring-boot:run` | Lance le serveur de développement (port 8080) |
| `mvn clean install` | Build complet (tests inclus) |
| `mvn test` | Exécute les tests unitaires |

## Variables d'environnement

| Variable | Description |
|---|---|
| `spring.datasource.*` | Connexion MySQL |
| `stripe.secret-key` | Clé secrète Stripe (Sandbox ou production) |
| `stripe.webhook-secret` | Secret webhook Stripe |
| `spring.mail.*` | Configuration Mailtrap |
| `app.openexchangerates.app-id` | Clé API OpenExchangeRates |

## Documentation

- Architecture détaillée : [`docs/architecture.md`](../docs/architecture.md)
- Référence API : [`docs/api-reference.md`](../docs/api-reference.md)
- Base de données : [`docs/database.md`](../docs/database.md)
- Notifications : [`docs/notifications.md`](../docs/notifications.md)
- Documentation technique complète : [`docs/`](../docs/)
