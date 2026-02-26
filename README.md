# sendMe

Projet en reconstruction (API + Web). Pour l'instant, seul le backend est initialise dans `api/`.

## Structure

- `api/` : backend Spring Boot
- `web/` : frontend (a venir)

## API (dev)

### Prerequis

- Java 21
- MySQL

### Configuration

Fichier: `api/src/main/resources/application-dev.properties`

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `jwt.secret`
- `jwt.expiration`

### Lancer l'API

```bash
cd api
./mvnw spring-boot:run
```

### Endpoints

- `GET /health` -> OK
- `POST /auth/register`
- `POST /auth/login`
