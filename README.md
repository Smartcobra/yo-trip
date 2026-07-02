# MakeMyTrip

Flight search and booking — **Spring Boot 4** API + **React TypeScript** frontend.

## Run

### Backend (port 8080)

```bash
cd backend
cp .env.example .env
mvn spring-boot:run
```

### Frontend (port 5173)

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173 — API calls proxy to `http://localhost:8080`.

## Demo login

| Field | Value |
|-------|-------|
| Username | `jitu` |
| Email | `jitu.learn@gmail.com` |
| Password | `Password@123` |

Sign in with **username or email** and password. Insert the user into PostgreSQL first:

```sql
INSERT INTO research.users (name, email, mobile, password_hash, created_at)
VALUES (
  'jitu',
  'jitu.learn@gmail.com',
  '9876543210',
  '$2a$10$0QJC3rLhez9fhFKFZa7Uwue5dbkNtNpI2B3PCAGKUKOr9N8BYL8HC',
  NOW()
);
```

## Stack

| Layer | Tech |
|-------|------|
| Backend | Spring Boot 4, PostgreSQL, JWT |
| Frontend | React 19, TypeScript, Vite, React Router |

## Notes

- PostgreSQL must be running with `flight_schedule` data for search results
- Login: `POST /api/auth/login` with `{ "username": "jitu", "password": "Password@123" }`
- Coupons: `MMTSTANC`, `MMTZEST`

## Swagger

- UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

Use **Authorize** with `Bearer <token>` from `POST /api/auth/login` for protected endpoints.
Username    jitu
Email       iamjitu.pradhan@gmail.com
Password   Password@123

razorpay test card: 4100 2800 0000 1007