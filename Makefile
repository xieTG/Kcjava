# Use '>' instead of TAB for recipe lines (avoids "missing separator" errors)
.RECIPEPREFIX := >

# -----------------------------
# Makefile for Docker workflow
# -----------------------------

# Default profile: dev or prod
PROFILE ?= dev

# Project name (namespace). Override per run: make start PROJECT=kcjava_dev
PROJECT ?= kcjava

# Path to Docker container logs (Linux default)
LOGS_DIR ?= /var/lib/docker/containers

# Use Docker Compose v2 by default. Override if needed:
# make start DC="docker-compose"
DC ?= docker compose

# --- Auto-detect compose file(s) ---
ifeq (,$(wildcard compose.yml))
  ifeq (,$(wildcard docker-compose.yml))
    $(error No compose file found. Create compose.yml or docker-compose.yml, or set COMPOSE_FILES)
  else
    COMPOSE_FILES ?= -f docker-compose.yml
  endif
else
  COMPOSE_FILES ?= -f compose.yml
endif

# Use sudo only when not root
SUDO := $(shell [ "$$(id -u)" -eq 0 ] || echo sudo)

.PHONY: help start stop up ps logs down restart clean-logs clean-volumes reset-db preclean

help:
> @echo "Usage:"
> @echo "  make start PROFILE=dev           # Clean Docker logs and start containers for a profile"
> @echo "  make start PROFILE=prod          # Same for prod"
> @echo "  make stop PROFILE=dev            # Stop containers for profile"
> @echo "  make down PROFILE=dev            # Stop and remove containers for profile (keeps volumes)"
> @echo "  make restart PROFILE=dev         # Restart (down + start)"
> @echo "  make reset-db PROFILE=dev        # Drop Postgres data volume and restart (re-runs schema.sql)"
> @echo "  make clean-logs                  # Truncate all container logs on this host"
> @echo "  make clean-volumes PROFILE=dev   # Remove volumes of this compose project (ALL data!)"
> @echo "  make ps                          # Show status"
> @echo "  make logs SERVICE=postgres       # Tail logs for a service"
> @echo ""
> @echo "Variables:"
> @echo "  PROFILE=dev|prod            # Compose profile (default: dev)"
> @echo "  PROJECT=kcjava              # Compose project name (namespace)"
> @echo "  DC='docker compose'         # Override if you use docker-compose legacy"
> @echo "  COMPOSE_FILES='-f docker-compose.yml'  # Auto-detected; override to add more"
> @echo "  VOL=<volume-name>           # Override DB volume used by reset-db"

# Optional: remove stray singletons before up (harmless if not present)
preclean:
> @echo ">> Removing stray containers if they exist (metabase, qst_postgres, qst_backend, qst_frontend)..."
> -docker rm -f metabase qst_postgres qst_backend qst_frontend 2>/dev/null || true

# 1) Clean logs + start containers (for a given profile)
start: clean-logs up

# 2) Stop containers (does not remove)
stop:
> @echo ">> Stopping containers (profile: $(PROFILE))..."
> $(DC) $(COMPOSE_FILES) -p $(PROJECT) --profile $(PROFILE) stop

# Down = stop and remove containers, networks (keeps volumes)
down:
> @echo ">> Removing containers (profile: $(PROFILE))..."
> $(DC) $(COMPOSE_FILES) -p $(PROJECT) --profile $(PROFILE) down

# Restart (down + start)
restart: down start

# Internal: start with profile
up:
> @echo ">> Starting containers with profile: $(PROFILE)"
> $(DC) $(COMPOSE_FILES) -p $(PROJECT) --profile $(PROFILE) up -d --remove-orphans
> @$(MAKE) ps

# Show running status
ps:
> $(DC) $(COMPOSE_FILES) -p $(PROJECT) ps

# Tail logs for a specific service: make logs SERVICE=postgres
logs:
ifndef SERVICE
> $(error Please provide SERVICE=<service-name>, e.g., make logs SERVICE=postgres)
endif
> $(DC) $(COMPOSE_FILES) -p $(PROJECT) logs -f $(SERVICE)

# Safely truncate Docker logs (host-wide)
clean-logs:
> @echo ">> Truncating Docker container logs in $(LOGS_DIR) (may require sudo)..."
> @$(SUDO) /bin/sh -c 'if [ -d "$(LOGS_DIR)" -a -r "$(LOGS_DIR)" ]; then \
>   find "$(LOGS_DIR)" -type f -name "*-json.log" -print -exec truncate -s 0 {} \; ; \
> else \
>   echo "Directory $(LOGS_DIR) not found or not readable. Skipping."; \
> fi'
> @echo ">> Done."

# WARNING: remove named volumes of this project (data loss!)
clean-volumes:
> @echo ">> WARNING: This will remove ALL named volumes of this compose project ($(PROJECT))!"
> @read -p "Type YES to proceed: " ans; if [ "$$ans" = "YES" ]; then \
>   $(DC) $(COMPOSE_FILES) -p $(PROJECT) --profile $(PROFILE) down -v; \
> else \
>   echo "Aborted."; \
> fi

# -----------------------------
# Reset DB: remove Postgres data volume and restart (so /docker-entrypoint-initdb.d runs again)
# You can override the detected volume with: make reset-db VOL=<your_volume_name>
# -----------------------------
reset-db:
> @echo ">> Resetting Postgres data volume for project '$(PROJECT)' (profile: $(PROFILE))"
> @echo ">> Step 1/3: Stopping and removing containers (keeps volumes for now)..."
> $(DC) $(COMPOSE_FILES) -p $(PROJECT) --profile $(PROFILE) down
> @echo ">> Step 2/3: Locating Postgres data volume to remove..."
> @VOL_NAME="$${VOL}"; \
> if [ -z "$${VOL_NAME}" ]; then \
>   VOL_NAME="$$(docker volume ls -q | grep -E '^$(PROJECT)_qst_pgdata$$|qst_pgdata$$' | head -n 1)"; \
> fi; \
> if [ -z "$${VOL_NAME}" ]; then \
>   echo "!! Could not auto-detect the Postgres volume."; \
>   echo "Available volumes:"; docker volume ls; echo ""; \
>   echo "Hint: it is usually named '$(PROJECT)_qst_pgdata' or 'qst_pgdata'."; \
>   echo "Try: make reset-db VOL=<volume-name>"; \
>   exit 2; \
> else \
>   echo ">> Candidate volume: $${VOL_NAME}"; \
>   read -p "Delete volume '$${VOL_NAME}' (this ERASES DB data)? Type YES to confirm: " ans; \
>   if [ "$${ans}" != "YES" ]; then echo "Aborted."; exit 1; fi; \
>   docker volume rm "$${VOL_NAME}"; \
> fi
> @echo ">> Step 3/3: Starting stack again so init scripts (schema.sql) run..."
> @$(MAKE) start PROFILE=$(PROFILE)
