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
| Emails | API Brevo (HTTPS) + Thymeleaf |
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
│   │   ├── db/migration/  # Migrations Flyway (V1–V26)
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
## Déploiement (Production)

L'application est déployée sur **Railway** (backend + frontend + base de données MySQL).

### Architecture

| Service | Plateforme | Root Directory | Description |
|---|---|---|---|
| **Backend** (`api`) | Railway | `api` | API Spring Boot (Java 21 / Maven) |
| **Frontend** (`web`) | Railway | `web` | React + Vite (build statique) |
| **Base MySQL** | Railway | — | Base de données relationnelle (service MySQL de Railway) |

### 1. Créer la base MySQL sur Railway

1. Dans Railway → **New → Database → MySQL** (le service MySQL fournit automatiquement les variables `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, `MYSQLPASSWORD`).
2. Relie le service **backend** à cette base : les variables `MYSQL*` sont injectées automatiquement.

### 2. Services Railway (2 services dans un même projet)

**Backend** — `Settings` :
- **Root Directory** : `api`
- **Builder** : Railpack / Nixpacks
- **Start Command** : `java -jar target/sendme-0.0.1-SNAPSHOT.jar`

**Frontend** — `Settings` :
- **Root Directory** : `web`
- **Builder** : Railpack / Nixpacks (Vite détecté automatiquement)

### 3. Variables d'environnement

**Backend → Variables :**

```env
# Base de données (Railway MySQL — injectées automatiquement par le plugin MySQL)
# MYSQLHOST, MYSQLPORT, MYSQLDATABASE, MYSQLUSER, MYSQLPASSWORD

# URLs
FRONTEND_URL=https://<frontend-url>
CORS_ALLOWED_ORIGINS=https://<frontend-url>
STRIPE_RETURN_URL=https://<frontend-url>/payment/callback

# Sécurité / services
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

**Frontend → Variables :**

```env
VITE_API_BASE_URL=https://<backend-url>/api
VITE_STRIPE_PUBLIC_KEY=pk_test_...
```

> `VITE_API_BASE_URL` est figée au build (Vite). Il faut redéployer le frontend après toute modification de cette variable.

### 4. Générer les domaines + CORS

- **Backend → Networking → Generate Domain** → note l'URL.
- **Frontend → Networking → Generate Domain** → note l'URL.
- Mets l'URL du **frontend** dans `CORS_ALLOWED_ORIGINS` du backend (sans slash final).
- Vérifie que `VITE_API_BASE_URL` pointe vers le **backend** (`https://<backend-url>/api`).

### 5. Redéploiement

- Railway redéploie automatiquement à chaque `git push` sur `main`.
- Au premier démarrage, **Flyway** applique les migrations (V1 → V26) : création des tables + compte admin.

### 6. Vérifications

- API / Swagger : `https://<backend-url>/swagger-ui/index.html`
- Frontend : `https://<frontend-url>` → page de connexion
- Admin : `admin@sendme.com` / `Admin@sendme2025`

---


## Canaux supportés

| Canal | Type |
|---|---|
| Virement bancaire (SEPA/SWIFT) | `BANK_TRANSFER` |
| Orange Money | `ORANGE_MONEY` |
| MTN Mobile Money | `MTN_MOBILE_MONEY` |
| Wave | `WAVE` |
| Moov Money | `MOOV_MONEY` |
| Airtel Money | `AIRTEL_MONEY` |

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
