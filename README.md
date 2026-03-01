# KC Questionnaire MVP — Spring Boot backend port

This is a **Java (Spring Boot) port** of the original Python/FastAPI backend from `xieTG/kc`.

It keeps the **same HTTP API** so the existing React frontend (proxied through Nginx) continues to work:

- `POST /auth/login`
- `GET /questionnaires`
- `GET /questionnaires/{id}/template`
- `POST /questionnaires/{id}/submissions` (multipart upload, requires `Authorization: Bearer <token>`)
- `GET /me/submissions` (requires token)
- `GET /health`

# Dev: clean logs and start postgres + metabase (with your profiles)
make start PROFILE=dev

# Prod: clean logs and start postgres + backend + frontend
make start PROFILE=prod

# Stop containers for the selected profile
make stop PROFILE=dev
# or
make stop PROFILE=prod


# Reset DB in dev (drops volume, re-runs schema.sql, restarts)
make reset-db PROFILE=dev

# Reset DB in prod (be careful!)
make reset-db PROFILE=prod

# If auto-detection fails (custom names), pass the exact volume name:
make reset-db PROFILE=dev VOL=kcjava_qst_pgdata


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
