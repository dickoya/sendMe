# SendMe — Frontend (React + TypeScript)

Interface utilisateur de la passerelle de transfert d'argent multi-canaux SendMe.

**Localisation** : `C:\xamp\htdocs\sendMe\web`

## Stack technique

| Technologie | Usage |
|---|---|
| React 19 + TypeScript 6 | Bibliothèque UI et typage statique |
| Vite 8 | Bundler et serveur de développement |
| Tailwind CSS v4 | Styling utilitaire |
| React Router DOM 7 | Routage SPA |
| Axios | Client HTTP |
| Stripe.js | Paiement par carte (Sandbox) |
| i18next | Internationalisation FR/EN (13 namespaces) |
| date-fns | Formatage dates localisé |
| Vitest | Tests unitaires |

## Prérequis

- Node.js 18+ (recommandé : 20 LTS)
- npm 9+
- Backend SendMe en cours d'exécution (port 8080)

## Installation rapide

```powershell
cd C:\xamp\htdocs\sendMe\web
npm install
copy .env.exemple .env
npm run dev
```

L'application est disponible sur `http://localhost:5173`.

## Scripts

| Commande | Description |
|---|---|
| `npm run dev` | Serveur de développement (port 5173) |
| `npm run build` | Build de production |
| `npm run preview` | Prévisualisation du build |
| `npm run lint` | ESLint |
| `npm run check:i18n` | Vérification des clés de traduction FR/EN |
| `npm run test` | Tests unitaires (Vitest) |

## Variables d'environnement

| Variable | Description |
|---|---|
| `VITE_API_BASE_URL` | URL de l'API backend (défaut: `http://localhost:8080/api`) |
| `VITE_STRIPE_PUBLIC_KEY` | Clé publique Stripe (Sandbox ou production) |

## Structure du projet

```
src/
├── App.tsx              # Composant racine (providers)
├── components/          # Composants UI réutilisables
│   ├── ui/              # Atomiques (Button, Input, Card, CurrencySelect...)
│   ├── layout/          # Header, Sidebar, LanguageSwitcher
│   ├── beneficiary/     # CRUD bénéficiaires
│   ├── transfer/        # Tunnel de transfert (FeeSimulator, TransferSummary, TransferTimeline...)
│   ├── dashboard/       # StatsCard, TransferEvolutionChart, ExchangeRateWidget, QuickActions
│   ├── referral/        # ReferralCard
│   ├── onboarding/      # OnboardingTour
│   └── notification/    # Cloche, centre de notifications
├── pages/               # Pages par domaine
│   ├── public/          # Home, Login, Register
│   ├── private/         # Dashboard, Transfer, History, Profile, Referral...
│   └── admin/           # AdminDashboard, AdminUsers...
├── layouts/             # PublicLayout, PrivateLayout, AdminLayout
├── routes/              # AppRoutes, PrivateRoute, AdminRoute
├── services/            # Couche API (Axios) — auth, beneficiary, transfer, dashboard, profile, referral...
├── hooks/               # Hooks personnalisés (useDashboard, useReferral, useOnboarding...)
├── store/               # Context API (auth, notification, transfer, theme)
├── i18n/                # Traductions FR/EN (13 namespaces)
├── types/               # Types TypeScript
├── utils/               # Helpers (dates, devises, notifications)
└── lib/                 # Utilitaires généraux
```

## Documentation

- Architecture frontend détaillée : [`docs/web-architecture.md`](../docs/web-architecture.md)
- Installation et configuration : [`docs/web-setup.md`](../docs/web-setup.md)
- Internationalisation : [`docs/i18n.md`](../docs/i18n.md)
- Documentation technique complète : [`docs/`](../docs/)
