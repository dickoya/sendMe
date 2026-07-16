# SendMe — Passerelle de Transfert d'Argent Direct Multi-Canaux

L'objectif principal est de concevoir et développer une application web Full-Stack
permettant à un utilisateur authentifié d'initier un transfert d'argent par carte bancaire.
Le système route instantanément les fonds vers le canal final choisi par
l'utilisateur (virement bancaire ou opérateur Mobile Money) sans stocker d'argent sur la
plateforme.

Projet réalisé dans le cadre d'un Travail de Fin d'Études (TFE).

---

## Stack technique

| Couche | Technologie |
|---|---|
| Backend | Java 21, Spring Boot 3.2.5, Maven, Flyway, MySQL |
| Frontend | React 19 + TypeScript 6, Vite 8, Tailwind CSS v4 |
| Paiement | Stripe (mode Sandbox) — SDK Java 25.3.0 |
| Emails | Spring Mail + Thymeleaf + Mailtrap/Brevo (Sandbox) |
| Temps réel | Server-Sent Events (SSE) |
| PDF | iText7 (itext7-core 7.2.5) |
| Taux de change | OpenExchangeRates API |
| Rate Limiting | Bucket4j 8.7.0 + Caffeine |
| Cache | Spring Cache + Caffeine |
| Documentation API | SpringDoc OpenAPI (Swagger UI) |
| Tests | JUnit 5, Mockito, AssertJ (backend) — Vitest, Testing Library (frontend) |
| Internationalisation | i18next (frontend, 13 namespaces FR/EN) — Spring MessageSource (backend, FR/EN) |

---

## Structure du dépôt

```
sendMe/
├── api/              Backend Spring Boot — voir api/README.md
│   ├── src/main/java/ibrahima/dickoya/sendme/
│   │   ├── config/        # 8 configurations (Security, Stripe, Swagger, Currency, etc.)
│   │   ├── controller/    # 14 contrôleurs REST
│   │   ├── dto/           # Request/Response DTOs
│   │   ├── entity/        # 10 entités JPA
│   │   ├── enums/         # 4 énumérations (ChannelType, TransactionStatus, NotificationType, Role)
│   │   ├── exception/     # 6 exceptions personnalisées + GlobalExceptionHandler
│   │   ├── repository/    # 10 interfaces Spring Data JPA
│   │   ├── security/      # JWT, RateLimitingFilter, UserDetailsServiceImpl
│   │   ├── service/       # 17 services métier
│   │   ├── strategy/      # TransferStrategy + 6 implémentations + BaseTransferStrategy
│   │   └── utils/         # 3 utilitaires (CurrencyByCountry, DataMaskingUtils, MessageUtils)
│   ├── src/main/resources/
│   │   ├── db/migration/  # 22 migrations Flyway (V1–V22)
│   │   ├── templates/     # 14 templates email Thymeleaf
│   │   ├── static/        # Logos des opérateurs Mobile Money
│   │   ├── messages_fr.properties / messages_en.properties
│   │   └── application.properties
│   └── http/              # Fichiers de test HTTP (REST Client)
├── web/              Frontend React + TypeScript — voir web/README.md
│   ├── src/
│   │   ├── components/   # 50+ composants UI réutilisables
│   │   ├── pages/        # 17 pages (publiques, privées, admin)
│   │   ├── layouts/      # 3 layouts (Public, Private, Admin)
│   │   ├── routes/       # AppRoutes, PrivateRoute, AdminRoute
│   │   ├── services/     # 12 services API (Axios)
│   │   ├── hooks/        # 12 hooks personnalisés
│   │   ├── store/        # 4 Context API (auth, notification, theme, transfer)
│   │   ├── i18n/         # 26 fichiers de traduction FR/EN (13 namespaces)
│   │   ├── types/        # Types TypeScript complets
│   │   └── utils/        # Helpers (dates, devises, notifications, export CSV)
│   └── public/           # favicon.svg, icons.svg
├── docs/             Documentation technique détaillée
│   ├── architecture.md   # Architecture générale, Strategy Pattern, flux, sécurité
│   ├── database.md       # Schéma BDD, migrations Flyway V1–V22
│   ├── diagrams/         # 13 diagrammes techniques (Mermaid + PlantUML)
│   │   ├── context-diagram.md
│   │   ├── component-architecture.md
│   │   ├── deployment-diagram.md
│   │   ├── navigation-flow.md
│   │   ├── use-case-complete.md
│   │   ├── use-case-diagram.md
│   │   ├── sequence-transfer-flow.md
│   │   ├── sequence-auth-flow.md
│   │   ├── sequence-referral-flow.md
│   │   ├── activity-transfer-lifecycle.md
│   │   ├── class-diagram.md
│   │   ├── business-logic.md
│   │   └── transaction-flow.mmd / .puml
│   ├── Rapport_TFE_SendMe.docx
│   └── SendMe_Presentation_Soutenance.pptx
└── mobile/           Application mobile (à venir)
```

---

## Documentation

| Document | Description |
|---|---|
| [`docs/architecture.md`](./docs/architecture.md) | Architecture générale, Strategy Pattern, flux de transfert, sécurité, multi-devises, parrainage, audit |
| [`docs/database.md`](./docs/database.md) | Schéma de base de données, migrations Flyway V1–V22, entités, devises supportées |
| [`docs/diagrams/README.md`](./docs/diagrams/README.md) | Index des diagrammes techniques |
| [`api/README.md`](./api/README.md) | Guide d'installation et de démarrage du backend |
| [`web/README.md`](./web/README.md) | Guide d'installation et de démarrage du frontend |

---

## Démarrage rapide

### 1. Backend

```powershell
cd api
# Configurer application-local.properties (voir api/README.md)
mvn spring-boot:run
```

L'API est disponible sur `http://localhost:8080`.

### 2. Frontend

```powershell
cd web
npm install
copy .env.exemple .env
npm run dev
```

L'application est disponible sur `http://localhost:5173`.

---

## Canaux supportés

| Canal | Type | Icône |
|---|---|---|
| Virement bancaire (SEPA/SWIFT) | `BANK_TRANSFER` | 🏦 |
| Orange Money | `ORANGE_MONEY` | 🟠 |
| MTN Mobile Money | `MTN_MOBILE_MONEY` | 🟡 |
| Wave | `WAVE` | 🔵 |
| Moov Money | `MOOV_MONEY` | 🟢 |
| Airtel Money | `AIRTEL_MONEY` | 🔴 |

---

## Fonctionnalités principales

- **Authentification JWT** — inscription, connexion, refresh token, déconnexion, validation d'email (V20)
- **Bénéficiaires** — CRUD avec champs contextuels selon le canal (RG-02), multi-canaux par bénéficiaire (V15)
- **Transferts** — simulation temps réel, tunnel 4 étapes, paiement Stripe, routage multi-canaux (Strategy Pattern)
- **Multi-devises** — 5 devises d'envoi (EUR, USD, GBP, CAD, CHF), 13 devises de réception (XOF, XAF, GNF, EUR, CHF, GBP, USD, CAD, MAD, DZD, TND, EGP, ZAR)
- **Dashboard utilisateur** — statistiques personnelles, évolution des transferts, taux de change en direct
- **Profil** — modification, changement de mot de passe, historique des connexions (V19), suppression de compte
- **Parrainage** — code unique (format `SENDME-XXXXXXXXXXXX`), coupons de réduction (5€), statistiques
- **Notifications** — temps réel (SSE) + emails (14 templates), pagination, marquage lu/non lu
- **Administration** — dashboard, gestion utilisateurs (filtres, batch), transactions, configuration frais, maintenance, logs d'audit (V21), gestion dynamique des canaux de paiement (V22)
- **Internationalisation** — interface entièrement bilingue français/anglais (13 namespaces frontend + backend Spring MessageSource)
- **Sécurité** — rate limiting (Bucket4j), exponential backoff, blocage IP persistant (V11), JWT stateless, validation d'email
- **Onboarding** — tour guidé pour les nouveaux utilisateurs
- **Thème clair/sombre**
- **Reçus PDF** — génération de reçus professionnels avec iText7 (téléchargement + prévisualisation)
- **Pays et devises** — API REST publique pour la liste des pays/devises supportés
- **Logs d'audit admin** — traçage AOP de toutes les actions sensibles (V21)

---

## Règles de gestion principales

| Règle | Description |
|---|---|
| RG-01 | Aucun solde stocké — tout débit déclenche un routage immédiat |
| RG-02 | Champs contextuels selon le canal (IBAN/BIC vs téléphone) |
| RG-03 | Application automatique des frais et du taux de change |
| RG-04 | Échec opérateur → remboursement automatique Stripe |
| RG-05 | Référence unique et immuable (format `TXN-YYYYMMDD-XXXX`) |
| RG-06 | Protection idempotence via `Idempotency-Key` (évite les doubles débits) |

---

## Comptes par défaut

| Rôle | Email | Mot de passe |
|---|---|---|
| Admin | `admin@sendme.com` | `Admin@sendme2025` |

> **Note :** Ce compte est créé par la migration Flyway V1. En développement, vous pouvez modifier l'email et le mot de passe dans `application-local.properties`.

---

## Cartes de test Stripe (mode Sandbox)

| Carte | Résultat |
|---|---|
| `4242 4242 4242 4242` | Paiement réussi |
| `4000 0000 0000 0002` | Paiement refusé |
| `4000 0025 0000 3155` | 3D Secure requis |

---

## Branches Git

| Branche | Description |
|---|---|
| `main` | Version stable et livrée |
| `develop` | Intégration continue |
| `feature/*` | Branches de fonctionnalités |
| `fix/*` | Branches de correction |

---

## Auteur

**Ibrahima Dickoya**  
TFE - Institut des Carrières Commerciales 
Année académique 2025–2026

---

## Licence

Projet académique — tous droits réservés.
