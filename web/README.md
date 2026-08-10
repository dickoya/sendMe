# SendMe — Frontend Web

Application React + TypeScript pour la passerelle de transfert d'argent SendMe.

---

## Stack technique

| Composant | Technologie |
|---|---|
| Framework | React 19 |
| Langage | TypeScript |
| Build | Vite |
| CSS | Tailwind CSS v4 |
| Routing | React Router DOM |
| État global | Context API (auth, notification, theme) |
| API | Axios (intercepteurs JWT + refresh automatique) |
| Paiement | Stripe Elements + Stripe.js |
| Internationalisation | i18next + react-i18next (FR/EN) |
| Temps réel | Server-Sent Events (fetch + ReadableStream) |
| Tests | Vitest + Testing Library |
| Linting | ESLint + TypeScript-ESLint |
| Graphiques | Recharts |

---

## Prérequis

- **Node.js 20+**
- **npm 10+**
- **Backend SendMe** démarré sur `http://localhost:8080`

---

## Installation

```powershell
cd web
npm install
```

### Configuration

Copier le fichier d'exemple du dépôt racine :

```powershell
copy ..\.env.example .env
```

Contenu du `.env` :

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_STRIPE_PUBLIC_KEY=pk_test_ta_cle_publishable
```

### Lancer le serveur de développement

```powershell
npm run dev
```

L'application est disponible sur `http://localhost:5173`.

---

## Structure du projet

```
web/src/
├── components/         # Composants UI réutilisables
│   ├── ui/             # Composants atomiques (Button, Input, Modal, Badge, Card, etc.)
│   ├── forms/          # Formulaires complexes (FeeConfigForm, MaintenanceForm, BankDetailsForm, etc.)
│   ├── layout/         # Header, Sidebar, LanguageSwitcher, ThemeToggle
│   ├── admin/          # Composants admin (StatCard, ChannelChart, UserTable, etc.)
│   ├── beneficiary/    # Bénéficiaires (BeneficiaryList, BeneficiaryForm, etc.)
│   ├── transfer/       # Tunnel de transfert (FeeSimulator, TransferSummary, StripePaymentForm, etc.)
│   ├── notification/   # NotificationBell, NotificationList, NotificationItem
│   ├── onboarding/     # OnboardingTour
│   ├── referral/       # ReferralCard
│   └── dashboard/      # StatsCard, QuickActions, ExchangeRateWidget, TransferEvolutionChart
├── pages/              # Pages
│   ├── public/         # Pages accessibles sans authentification
│   │   ├── Home.tsx              # Page d'accueil publique avec simulateur
│   │   ├── Login.tsx             # Connexion
│   │   ├── Register.tsx          # Inscription avec code de parrainage
│   │   ├── EmailVerification.tsx # Vérification d'email
│   │   ├── VerificationSent.tsx  # Confirmation d'envoi de vérification
│   │   └── LegalNotice.tsx       # Mentions légales
│   ├── private/        # Pages nécessitant authentification
│   │   ├── Dashboard.tsx         # Tableau de bord utilisateur
│   │   ├── Transfer.tsx          # Tunnel de transfert (4 étapes)
│   │   ├── Beneficiaries.tsx     # Gestion des bénéficiaires
│   │   ├── History.tsx           # Historique des transactions
│   │   ├── Notifications.tsx     # Centre de notifications
│   │   ├── Profile.tsx           # Profil utilisateur
│   │   ├── ReferralPage.tsx      # Parrainage
│   │   └── Faq.tsx               # FAQ
│   └── admin/          # Pages réservées aux administrateurs
│       ├── AdminDashboard.tsx        # Dashboard admin
│       ├── AdminUsers.tsx            # Gestion des utilisateurs
│       ├── AdminTransactions.tsx     # Transactions
│       ├── AdminLogs.tsx             # Logs d'audit
│       ├── AdminSettings.tsx         # Configuration (frais, maintenance)
│       └── AdminPaymentChannels.tsx  # Canaux de paiement
├── layouts/            # Layouts
│   ├── PublicLayout.tsx     # Layout pour pages publiques (Login, Register)
│   ├── PrivateLayout.tsx    # Layout pour pages privées (navbar, sidebar)
│   └── AdminLayout.tsx      # Layout pour pages admin (navbar, sidebar admin)
├── routes/             # Routing
│   ├── AppRoutes.tsx        # Arborescence complète des routes
│   ├── PrivateRoute.tsx     # Guard d'authentification
│   └── AdminRoute.tsx       # Guard de rôle ADMIN
├── services/           # Services API (Axios)
│   ├── api.ts               # Instance Axios centrale + intercepteurs JWT/refresh
│   ├── authService.ts       # Authentification (login, register, refresh, verify-email)
│   ├── transferService.ts   # Transferts et transactions
│   ├── beneficiaryService.ts # Bénéficiaires
│   ├── notificationService.ts # Notifications + SSE
│   ├── dashboardService.ts  # Dashboard utilisateur
│   ├── profileService.ts    # Profil
│   ├── referralService.ts   # Parrainage
│   ├── adminService.ts      # Administration
│   ├── auditLogService.ts   # Logs d'audit admin
│   ├── channelService.ts    # Canaux de paiement
│   ├── paymentChannelService.ts # Gestion des canaux de paiement (admin)
│   └── stripeService.ts     # Stripe (payment intent)
├── hooks/              # Hooks personnalisés
│   ├── useSSE.ts            # Connexion SSE avec reconnexion automatique
│   ├── useDebounce.ts       # Debounce pour la recherche
│   ├── useOnboarding.ts     # Tour guidé onboarding
│   ├── useDashboard.ts      # Données du dashboard
│   ├── useBeneficiary.ts    # Gestion des bénéficiaires
│   ├── useReferral.ts       # Parrainage
│   ├── useTransfer.ts       # Tunnel de transfert
│   ├── useAdminDashboard.ts # Dashboard admin
│   ├── useAdminUsers.ts     # Gestion utilisateurs admin
│   ├── useAdminTransactions.ts # Transactions admin
│   ├── useAdminAnalytics.ts # Analytics admin
│   └── usePaymentChannels.ts # Canaux de paiement admin
├── store/              # Context API
│   ├── auth/               # AuthContext (user, login, register, logout)
│   ├── notification/       # NotificationContext (SSE, historique, compteur) + toastContext
│   └── theme/              # ThemeContext (clair/sombre)
├── i18n/               # Internationalisation
│   ├── index.ts            # Configuration i18next
│   └── locales/            # Fichiers de traduction FR/EN (14 namespaces)
│       ├── fr/             # common, auth, dashboard, beneficiaries, transfer, history,
│       │                   # notifications, admin, home, profile, referral, onboarding, faq, legal
│       └── en/             # Mêmes namespaces en anglais
├── types/              # Types TypeScript (miroir des DTOs backend)
│   ├── index.ts
│   └── paymentChannel.ts
├── utils/              # Helpers
│   ├── constants.ts        # Constantes (canaux, statuts, pays, devises, langues)
│   ├── formatCurrency.ts   # Formatage monétaire
│   ├── formatDate.ts       # Formatage des dates
│   ├── notificationDisplay.ts # Messages de notification traduits
│   └── exportCSV.ts        # Export CSV des transactions
├── lib/                # Fonctions utilitaires
│   └── utils.ts            # getErrorMessage, cn (classnames)
├── assets/             # Images (hero.png, vite.svg)
├── App.tsx             # Composant racine (providers)
├── main.tsx            # Point d'entrée
└── index.css           # Styles globaux + Tailwind
```

---

## Pages et routes

### Pages publiques (sans authentification)

| Route | Page | Description |
|---|---|---|
| `/` | Home | Page d'accueil avec simulateur de transfert |
| `/login` | Login | Connexion |
| `/register` | Register | Inscription avec code de parrainage optionnel |
| `/verify-email` | EmailVerification | Vérification d'email |
| `/verify-email-sent` | VerificationSent | Confirmation d'envoi |
| `/legal` | LegalNotice | Mentions légales |

### Pages privées (authentification requise)

| Route | Page | Description |
|---|---|---|
| `/dashboard` | Dashboard | Statistiques personnelles, évolution, taux de change |
| `/transfer` | Transfer | Tunnel de transfert en 4 étapes |
| `/beneficiaries` | Beneficiaries | CRUD bénéficiaires |
| `/history` | History | Historique des transactions |
| `/notifications` | Notifications | Centre de notifications |
| `/profile` | Profile | Modification profil, mot de passe, historique connexions |
| `/referral` | ReferralPage | Code de parrainage, coupons, statistiques |
| `/faq` | Faq | Questions fréquentes |

### Pages admin (rôle ADMIN requis)

| Route | Page | Description |
|---|---|---|
| `/admin` | AdminDashboard | Dashboard admin (statistiques, graphiques) |
| `/admin/users` | AdminUsers | Gestion des utilisateurs (filtres, batch) |
| `/admin/transactions` | AdminTransactions | Toutes les transactions |
| `/admin/logs` | AdminLogs | Logs d'audit admin |
| `/admin/settings` | AdminSettings | Configuration (frais, maintenance) |
| `/admin/payment-channels` | AdminPaymentChannels | Gestion des canaux de paiement |

---

## Tunnel de transfert (4 étapes)

Le transfert se déroule en 4 étapes dans un tunnel dédié :

1. **Étape 1 — Formulaire** : sélection du bénéficiaire et du canal, saisie du montant, simulation en temps réel
2. **Étape 2 — Récapitulatif** : résumé de la transaction (montant, frais, taux de change)
3. **Étape 3 — Paiement** : formulaire de carte Stripe (Elements)
4. **Étape 4 — Succès** : confirmation et récapitulatif final

---

## Fonctionnalités frontend

- **Simulation temps réel** — calcul des frais à chaque modification du montant
- **Pagination** — historique, bénéficiaires, notifications, admin
- **Recherche et filtres** — utilisateurs admin (texte, statut, rôle, dates)
- **Actions en masse** — suspension/activation de plusieurs comptes
- **Graphiques** — volume par jour, taux de succès, répartition par canal (Recharts)
- **Thème clair/sombre** — persistance en localStorage
- **Internationalisation** — bascule FR/EN avec détection navigateur
- **Notifications temps réel** — SSE avec reconnexion automatique
- **Badge de notifications** — compteur non lues dans la navbar
- **Onboarding** — tour guidé pour les nouveaux utilisateurs
- **Export CSV** — export de l'historique des transactions
- **Reçus PDF** — téléchargement et prévisualisation dans un nouvel onglet
- **Responsive** — adaptation mobile/tablette/desktop
- **Gestion d'erreurs** — messages d'erreur traduits, états vides, états de chargement
- **Refresh automatique JWT** — intercepteur Axios avec file d'attente

---

## Déploiement (Railway / Vercel)

Le frontend (React + Vite) est un **site statique** : après `npm run build`, il peut être hébergé sur **Railway** (même projet que le backend) ou sur **Vercel** (gratuit).

- **Root Directory** : `web`
- **Builder** : Railpack / Nixpacks (détection Vite) — ou Vercel avec `npm run build` / `dist`.

**Variables d'environnement (au moment du build) :**

```env
VITE_API_BASE_URL=https://<backend-url>/api
VITE_STRIPE_PUBLIC_KEY=pk_test_...
```

> ⚠️ **`VITE_API_BASE_URL` est figée au moment du build** (Vite l'injecte via `import.meta.env`). Après toute modification de cette variable, il faut **Redéployer** le frontend, sinon l'ancien bundle (avec l'ancienne URL) reste servi.
>
> `VITE_API_BASE_URL` doit pointer vers le **backend** (`https://<backend-url>/api`), pas vers le frontend — sinon erreur `405 Method Not Allowed` à la connexion.

**Routage SPA :** pour les URLs profondes (`/login`, `/dashboard`, …), un fichier de routage est fourni :
- Vercel : `vercel.json` (rewrites vers `index.html`).

---

## Scripts disponibles

```powershell
npm run dev        # Serveur de développement (Vite)
npm run build      # Build de production
npm run preview    # Prévisualisation du build
npm run test       # Tests unitaires (Vitest)
npm run lint       # Linting ESLint
```

---

## Auteur

**Ibrahima Dickoya** — TFE 2025–2026
