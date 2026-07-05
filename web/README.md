# SendMe — Frontend Web

Application React + TypeScript pour la passerelle de transfert d'argent SendMe.

---

## Stack technique

| Composant | Technologie |
|---|---|
| Framework | React 19 |
| Langage | TypeScript 6 |
| Build | Vite 8 |
| CSS | Tailwind CSS v4 |
| Routing | React Router DOM 7 |
| État global | Context API (4 providers) |
| API | Axios (intercepteurs JWT + refresh automatique) |
| Paiement | Stripe Elements + Stripe.js |
| Internationalisation | i18next + react-i18next (13 namespaces FR/EN) |
| Temps réel | Server-Sent Events (fetch + ReadableStream) |
| Tests | Vitest + Testing Library |
| Linting | ESLint + TypeScript-ESLint |

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

Copier le fichier d'exemple :

```powershell
copy .env.exemple .env
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
├── components/         # 50+ composants UI réutilisables
│   ├── ui/             # Composants atomiques (Button, Input, Modal, Badge, Card, etc.)
│   ├── forms/          # Formulaires complexes (TransferForm, BeneficiaryForm, etc.)
│   ├── charts/         # Graphiques (VolumeChart, SuccessRateChart, PieChart)
│   ├── layout/         # Navbar, Sidebar, Footer, Breadcrumb
│   └── shared/         # Composants partagés (LoadingSpinner, EmptyState, ErrorState, Pagination)
├── pages/              # 17 pages
│   ├── public/         # Pages accessibles sans authentification
│   │   ├── Home.tsx              # Page d'accueil publique avec simulateur
│   │   ├── Login.tsx             # Connexion
│   │   ├── Register.tsx          # Inscription avec code de parrainage
│   │   ├── EmailVerification.tsx # Vérification d'email
│   │   └── VerificationSent.tsx  # Confirmation d'envoi de vérification
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
├── layouts/            # 3 layouts
│   ├── PublicLayout.tsx     # Layout pour pages publiques (Login, Register)
│   ├── PrivateLayout.tsx    # Layout pour pages privées (navbar, sidebar)
│   └── AdminLayout.tsx      # Layout pour pages admin (navbar, sidebar admin)
├── routes/             # Routing
│   ├── AppRoutes.tsx        # Arborescence complète des routes
│   ├── PrivateRoute.tsx     # Guard d'authentification
│   └── AdminRoute.tsx       # Guard de rôle ADMIN
├── services/           # 11 services API (Axios)
│   ├── api.ts               # Instance Axios centrale + intercepteurs JWT/refresh
│   ├── authService.ts       # Authentification (login, register, refresh, verify-email)
│   ├── transferService.ts   # Transferts et transactions
│   ├── beneficiaryService.ts # Bénéficiaires
│   ├── notificationService.ts # Notifications + SSE
│   ├── dashboardService.ts  # Dashboard utilisateur
│   ├── profileService.ts    # Profil
│   ├── referralService.ts   # Parrainage
│   ├── adminService.ts      # Administration
│   ├── countryService.ts    # Pays et devises
│   └── paymentChannelService.ts # Canaux de paiement
├── hooks/              # 10 hooks personnalisés
│   ├── useSSE.ts            # Connexion SSE avec reconnexion automatique
│   ├── usePagination.ts     # Pagination générique
│   ├── useDebounce.ts       # Debounce pour la recherche
│   ├── useLocalStorage.ts   # Persistance localStorage typée
│   ├── useMediaQuery.ts     # Media queries responsive
│   ├── useOnClickOutside.ts # Détection clic extérieur
│   ├── useScrollToTop.ts    # Scroll to top on navigation
│   ├── useDocumentTitle.ts  # Mise à jour du titre de page
│   ├── useFormValidation.ts # Validation de formulaire
│   └── useCountdown.ts      # Compte à rebours
├── store/              # 4 Context API
│   ├── auth/               # AuthContext (user, login, register, logout)
│   ├── notification/       # NotificationContext (SSE, historique, compteur)
│   │   ├── notificationContext.tsx
│   │   └── toastContext.tsx
│   ├── theme/              # ThemeContext (clair/sombre)
│   └── transfer/           # TransferContext (état du tunnel 4 étapes)
├── i18n/               # Internationalisation
│   ├── index.ts            # Configuration i18next
│   └── locales/            # 26 fichiers de traduction
│       ├── fr/             # 13 namespaces français
│       │   ├── common.json
│       │   ├── auth.json
│       │   ├── dashboard.json
│       │   ├── beneficiaries.json
│       │   ├── transfer.json
│       │   ├── history.json
│       │   ├── notifications.json
│       │   ├── admin.json
│       │   ├── home.json
│       │   ├── profile.json
│       │   ├── referral.json
│       │   ├── onboarding.json
│       │   └── faq.json
│       └── en/             # 13 namespaces anglais (mêmes clés)
│           ├── common.json
│           ├── auth.json
│           ├── ...
│           └── faq.json
├── types/              # Types TypeScript
│   └── index.ts            # Tous les types (miroir des DTOs backend)
├── utils/              # Helpers
│   ├── constants.ts        # Constantes (canaux, statuts, pays, devises, langues)
│   ├── format.ts           # Formatage (monnaie, dates, pourcentages)
│   ├── validation.ts       # Règles de validation
│   ├── notificationDisplay.ts # Messages de notification traduits
│   ├── exportCsv.ts        # Export CSV des transactions
│   └── countries.ts        # Données des pays
├── lib/                # Fonctions utilitaires
│   └── utils.ts            # getErrorMessage, cn (classnames)
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

1. **Étape 1 — Montant** : Saisie du montant, sélection des devises, simulation en temps réel
2. **Étape 2 — Bénéficiaire** : Sélection ou création d'un bénéficiaire
3. **Étape 3 — Paiement** : Formulaire de carte Stripe (Elements)
4. **Étape 4 — Confirmation** : Récapitulatif et statut en temps réel

---

## Fonctionnalités frontend

- **Simulation temps réel** — calcul des frais à chaque modification du montant
- **Pagination** — historique, bénéficiaires, notifications, admin
- **Recherche et filtres** — utilisateurs admin (texte, statut, rôle, dates)
- **Actions en masse** — suspension/activation de plusieurs comptes
- **Graphiques** — volume par jour, taux de succès, répartition par canal (Chart.js)
- **Thème clair/sombre** — persistance en localStorage
- **Internationalisation** — bascule FR/EN avec détection navigateur
- **Notifications temps réel** — SSE avec reconnexion automatique (5s)
- **Badge de notifications** — compteur non lues dans la navbar
- **Onboarding** — tour guidé pour les nouveaux utilisateurs
- **Export CSV** — export de l'historique des transactions
- **Reçus PDF** — téléchargement et prévisualisation dans un nouvel onglet
- **Responsive** — adaptation mobile/tablette/desktop
- **Gestion d'erreurs** — messages d'erreur traduits, états vides, états de chargement
- **Refresh automatique JWT** — intercepteur Axios avec file d'attente

---

## Scripts disponibles

```powershell
npm run dev        # Serveur de développement (Vite)
npm run build      # Build de production
npm run preview    # Prévisualisation du build
npm run test       # Tests unitaires (Vitest)
npm run lint       # Linting ESLint
npm run typecheck  # Vérification TypeScript
```

---

## Dépendances principales

```json
{
  "react": "^19.0.0",
  "react-router-dom": "^7.0.0",
  "axios": "^1.7.0",
  "@stripe/react-stripe-js": "^3.0.0",
  "@stripe/stripe-js": "^5.0.0",
  "i18next": "^24.0.0",
  "react-i18next": "^15.0.0",
  "i18next-browser-languagedetector": "^8.0.0",
  "chart.js": "^4.4.0",
  "react-chartjs-2": "^5.2.0",
  "tailwindcss": "^4.0.0",
  "vite": "^8.0.0",
  "vitest": "^3.0.0"
}
```

---

## Auteur

**Ibrahima Dickoya** — TFE 2024–2025
