# sendMe
L'objectif principal est de concevoir et développer une application web Full-Stack 
permettant à un utilisateur authentifié d'initier un transfert d'argent par carte bancaire. 
Le système route instantanément les fonds vers le canal final choisi par 
l'utilisateur (virement bancaire ou opérateur Mobile Money) sans stocker d'argent sur la 
plateforme.

# SendMe — Backend API

> Passerelle de transfert d'argent direct multi-canaux  
> Travail de Fin d'Études (TFE) — Java Spring Boot

---

##  Présentation

**SendMe** est une application web Full-Stack permettant à un utilisateur
authentifié d'envoyer de l'argent depuis sa carte bancaire vers :

- Un compte bancaire (virement SEPA/SWIFT)
- Orange Money
- MTN Mobile Money
- Wave
- Moov Money
- Airtel Money

> **Principe fondamental :** la plateforme ne stocke aucun argent.
> Chaque débit validé déclenche immédiatement un ordre d'envoi vers
> l'opérateur cible.

---

##  Stack technique

| Couche        | Technologie                  |
|---------------|------------------------------|
| Langage       | Java 21                      |
| Framework     | Spring Boot 4.0.6            |
| Sécurité      | Spring Security + JWT (jjwt) |
| Persistance   | Spring Data JPA + Hibernate  |
| Base de données | MySQL 8+                     |
| Migrations    | Flyway                       |
| Build         | Maven                        |
| Tests         | JUnit 5 + Mockito            |
| Documentation | Swagger / OpenAPI 3          |

---

##  Structure du projet
sendme
|
api
├── src/
│   ├── main/
│   │   ├── java/com/tfe/sendme/
│   │   │   ├── config/          # SecurityConfig, SwaggerConfig
│   │   │   ├── controller/      # AuthController, TransferController...
│   │   │   ├── service/         # AuthService, TransferService...
│   │   │   ├── repository/      # UserRepository, TransactionRepository...
│   │   │   ├── entity/          # User, Transaction, Beneficiary...
│   │   │   ├── dto/             # request/ et response/
│   │   │   ├── enums/           # ChannelType, TransactionStatus
│   │   │   ├── exception/       # GlobalExceptionHandler
│   │   │   ├── security/        # JwtFilter, JwtService
│   │   │   └── strategy/        # TransferStrategy et implémentations
│   │   └── resources/
│   │       ├── db/migration/    # Scripts Flyway (V1__, V2__...)
│   │       └── application.properties
│   └── test/
│   ├── .env.dev
│   └── pom.xml
│
├── README.md   
├── mobile
├── web
└── .gitignore

---

##  Prérequis

Avant de lancer le projet, assure-toi d'avoir installé :

- [Java 21+](https://adoptium.net/)
- [Maven 3.8+](https://maven.apache.org/)
- [MySQL 8+](https://dev.mysql.com/downloads/)
- [Git](https://git-scm.com/)

---

##  Installation & Lancement

### 1. Cloner le dépôt

```bash
git clone https://github.com/dickoya/sendMe.git
cd sendme\api
```

### 2. Créer la base de données MySQL

```sql
CREATE DATABASE sendme_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Configurer les variables d'environnement

Copie le fichier exemple et remplis tes valeurs :

```bash
cp .env.example .env
```

Contenu de `.env` à remplir :

```env
DB_URL=jdbc:mysql://localhost:3306/sendme_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USER=root
DB_PASSWORD=ton_mot_de_passe
JWT_SECRET=une_cle_secrete_tres_longue_min_32_caracteres
```

### 4. Lancer l'application

```bash
mvn spring-boot:run
```

L'API est disponible sur : `http://localhost:8080`  
La documentation Swagger sera sur : `http://localhost:8080/swagger-ui.html`

---

##  Variables d'environnement

| Variable       | Description                              | Exemple                        |
|----------------|------------------------------------------|--------------------------------|
| `DB_URL`       | URL de connexion MySQL                   | `jdbc:mysql://localhost:3306/sendme_db` |
| `DB_USER`      | Utilisateur MySQL                        | `root`                         |
| `DB_PASSWORD`  | Mot de passe MySQL                       | `monmotdepasse`                |
| `JWT_SECRET`   | Clé secrète pour signer les tokens JWT   | `cle_min_32_caracteres`        |

>  Ne jamais commiter le fichier `.env` — il est dans le `.gitignore`

---

##  Endpoints API (aperçu)

| Méthode | Endpoint                        | Description                  | Auth       |
|---------|---------------------------------|------------------------------|------------|
| POST    | `/api/auth/register`            | Inscription                  | Public     |
| POST    | `/api/auth/login`               | Connexion + JWT              | Public     |
| POST    | `/api/transfers/simulate`       | Simuler frais + taux         | JWT requis |
| POST    | `/api/transfers`                | Initier un transfert         | JWT requis |
| GET     | `/api/transactions`             | Historique paginé            | JWT requis |
| GET     | `/api/transactions/{id}/receipt`| Télécharger reçu PDF         | JWT requis |
| GET     | `/api/admin/dashboard`          | Stats globales               | ADMIN      |

> Documentation complète disponible sur Swagger après lancement.

---

##  Branches Git

| Branche                         | Description                        |
|---------------------------------|------------------------------------|
| `main`                          | Version stable et livrée           |
| `develop`                       | Intégration continue               |
| `feature/01-project-setup`      | Initialisation du projet           |
| `feature/02-database`           | Entités JPA + migrations Flyway    |
| `feature/03-auth`               | Authentification JWT               |
| `feature/04-beneficiaries`      | Gestion des bénéficiaires          |
| `feature/05-transfer-strategy`  | Strategy Pattern + transferts      |
| `feature/06-notifications`      | Notifications temps réel (SSE)     |
| `feature/07-history-pdf`        | Historique + reçus PDF             |
| `feature/08-admin`              | Module administration              |
| `feature/09-swagger-docs`       | Documentation OpenAPI              |

---

##  Tests

```bash
# Lancer tous les tests
mvn test

# Lancer les tests d'un module spécifique
mvn test -Dtest=AuthServiceTest
```

---

##  Auteur

**[Ton Nom Complet]**  
TFE — [Nom de ton école]  
Année académique 2024–2025  
Promoteur : [Nom du promoteur]

---

##  Licence

Projet académique — tous droits réservés.
