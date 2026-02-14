# KC Questionnaire MVP — Spring Boot backend port

This is a **Java (Spring Boot) port** of the original Python/FastAPI backend from `xieTG/kc`.

It keeps the **same HTTP API** so the existing React frontend (proxied through Nginx) continues to work:

- `POST /auth/login`
- `GET /questionnaires`
- `GET /questionnaires/{id}/template`
- `POST /questionnaires/{id}/submissions` (multipart upload, requires `Authorization: Bearer <token>`)
- `GET /me/submissions` (requires token)
- `GET /health`

## Run with Docker

1) Create a `.env` file (or copy `.env.example`):

```bash
cp .env.example .env
```

2) Start everything:

```bash
docker compose up --build
```

- Frontend: http://localhost:3000
- Backend API: http://localhost:8000
- Swagger UI: http://localhost:8000/docs
- Postgres exposed on: localhost:5432 (user `qst_app`, password `qst_app_pwd`, db `qst`)

## Notes

- DB schema is initialized by `infra/postgres/schema.sql` (same as the Python repo).
- Uploaded XLSX files are stored in a Docker volume mounted at `/data/uploads`.
- XLSX parsing/template generation uses Apache POI.
- JWT auth uses HS256 (shared secret `JWT_SECRET`) and a 1-hour access token TTL (matches the Python backend).
