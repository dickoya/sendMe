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
| Paiement | Stripe (mode Sandbox) |
| Emails | Spring Mail + Thymeleaf + Mailtrap (Sandbox) |
| Temps réel | Server-Sent Events (SSE) |
| PDF | iText7 |

---

## Structure du dépôt

```
sendMe/
├── api/         Backend Spring Boot — voir api/README.md
├── web/         Frontend React + TypeScript — voir web/README.md
├── docs/        Documentation technique détaillée
└── mobile/      Application mobile (à venir)
```

---

## Documentation

| Document | Description |
|---|---|
| [`docs/README.md`](./docs/README.md) | Index de la documentation technique |
| [`docs/architecture.md`](./docs/architecture.md) | Architecture générale, Strategy Pattern, flux de transfert |
| [`docs/api-reference.md`](./docs/api-reference.md) | Référence complète des endpoints REST |
| [`docs/database.md`](./docs/database.md) | Schéma de base de données, migrations Flyway |
| [`docs/notifications.md`](./docs/notifications.md) | Système de notifications (SSE + emails) |
| [`docs/i18n.md`](./docs/i18n.md) | Internationalisation FR/EN |
| [`docs/progress.md`](./docs/progress.md) | État d'avancement détaillé |
| [`docs/web-architecture.md`](./docs/web-architecture.md) | Architecture frontend (React, composants, hooks) |
| [`docs/web-setup.md`](./docs/web-setup.md) | Installation et configuration du frontend |
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

- Virement bancaire (SEPA/SWIFT)
- Orange Money
- MTN Mobile Money
- Wave
- Moov Money
- Airtel Money

---

## Règles de gestion principales

| Règle | Description |
|---|---|
| RG-01 | Aucun solde stocké — tout débit déclenche un routage immédiat |
| RG-02 | Champs contextuels selon le canal (IBAN/BIC vs téléphone) |
| RG-03 | Application automatique des frais et du taux de change |
| RG-04 | Échec opérateur → remboursement automatique Stripe |

---

## Comptes par défaut

| Rôle | Email | Mot de passe |
|---|---|---|
| Admin | `admin@sendme.com` | `Admin@sendme2025` |

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
TFE — [Nom de ton école]  
Année académique 2024–2025

---

## Licence

Projet académique — tous droits réservés.
