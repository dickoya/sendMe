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
| Frontend | React 19 + TypeScript, Vite, Tailwind CSS v4 |
| Paiement | Stripe (mode Sandbox) |
| Emails | Spring Mail + Thymeleaf + Mailtrap/Brevo (Sandbox) |
| Temps réel | Server-Sent Events (SSE) |
| PDF | iText7 |
| Taux de change | OpenExchangeRates API |
| Rate Limiting | Bucket4j + Caffeine |
| Cache | Spring Cache + Caffeine |
| Documentation API | SpringDoc OpenAPI (Swagger UI) |
| Tests | JUnit 5, Mockito, AssertJ (backend) — Vitest, Testing Library (frontend) |
| Internationalisation | i18next (frontend, FR/EN) — Spring MessageSource (backend, FR/EN) |

---

## Structure du dépôt

```
sendMe/
├── api/              Backend Spring Boot — voir api/README.md
│   ├── src/main/java/ibrahima/dickoya/sendme/
│   │   ├── config/        # Configurations (Security, Stripe, Swagger, Currency, etc.)
│   │   ├── controller/    # Contrôleurs REST
│   │   ├── dto/           # Request/Response DTOs
│   │   ├── entity/        # Entités JPA
│   │   ├── enums/         # Énumérations (ChannelType, TransactionStatus, NotificationType, Role)
│   │   ├── exception/     # Exceptions personnalisées + GlobalExceptionHandler
│   │   ├── repository/    # Interfaces Spring Data JPA
│   │   ├── security/      # JWT, RateLimitingFilter, UserDetailsServiceImpl
│   │   ├── service/       # Services métier
│   │   ├── strategy/      # TransferStrategy + implémentations + BaseTransferStrategy
│   │   └── utils/         # Utilitaires (CurrencyByCountry, DataMaskingUtils, MessageUtils)
│   ├── src/main/resources/
│   │   ├── db/migration/  # Migrations Flyway (V1–V24)
│   │   ├── templates/     # Templates email Thymeleaf
│   │   ├── static/        # Logos des opérateurs Mobile Money
│   │   ├── messages_fr.properties / messages_en.properties
│   │   └── application.properties
│   └── http/              # Fichiers de test HTTP (REST Client)
├── web/              Frontend React + TypeScript — voir web/README.md
│   ├── src/
│   │   ├── components/   # Composants UI réutilisables
│   │   ├── pages/        # Pages (publiques, privées, admin)
│   │   ├── layouts/      # Layouts (Public, Private, Admin)
│   │   ├── routes/       # AppRoutes, PrivateRoute, AdminRoute
│   │   ├── services/     # Services API (Axios)
│   │   ├── hooks/        # Hooks personnalisés
│   │   ├── store/        # Context API (auth, notification, theme)
│   │   ├── i18n/         # Fichiers de traduction FR/EN
│   │   ├── types/        # Types TypeScript
│   │   └── utils/        # Helpers (dates, devises, notifications, export CSV)
│   └── public/           # favicon.svg, logo/
├── .env.example      # Variables d'environnement (copier vers .env)
└── api/README.md     # Guide d'installation du backend
```

---

## Documentation

| Document | Description |
|---|---|
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
copy .env.example .env
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

- **Authentification JWT** — inscription, connexion, refresh token, déconnexion, validation d'email
- **Bénéficiaires** — CRUD avec champs contextuels selon le canal, multi-canaux par bénéficiaire
- **Transferts** — simulation temps réel, tunnel 4 étapes, paiement Stripe, routage multi-canaux (Strategy Pattern)
- **Multi-devises** — devises d'envoi et de réception multiples
- **Dashboard utilisateur** — statistiques personnelles, évolution des transferts, taux de change en direct
- **Profil** — modification, changement de mot de passe, historique des connexions, suppression de compte
- **Parrainage** — code unique (format `SENDME-XXXXXXXXXXXX`), coupons de réduction, statistiques
- **Notifications** — temps réel (SSE) + emails, pagination, marquage lu/non lu
- **Administration** — dashboard, gestion utilisateurs (filtres, batch), transactions, configuration frais, maintenance, logs d'audit, gestion dynamique des canaux de paiement
- **Internationalisation** — interface bilingue français/anglais
- **Sécurité** — rate limiting (Bucket4j), exponential backoff, blocage IP persistant, JWT stateless, validation d'email
- **Onboarding** — tour guidé pour les nouveaux utilisateurs
- **Thème clair/sombre**
- **Reçus PDF** — génération de reçus professionnels avec iText7 (téléchargement + prévisualisation)
- **Pays et devises** — API REST publique pour la liste des pays/devises supportés

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

> **Note :** Ce compte est créé par la migration Flyway V9. En développement, vous pouvez modifier l'email et le mot de passe dans `application-local.properties`.

---

## Cartes de test Stripe (mode Sandbox)

| Carte | Résultat |
|---|---|
| `4242 4242 4242 4242` | Paiement réussi |
| `4000 0000 0000 0002` | Paiement refusé |
| `4000 0025 0000 3155` | 3D Secure requis |

---

## Auteur

**Ibrahima Dickoya**  
TFE - Institut des Carrières Commerciales  
Année académique 2025–2026

---

## Licence

Projet académique — tous droits réservés.
