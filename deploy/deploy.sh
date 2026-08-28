#!/usr/bin/env bash
set -Eeuo pipefail

APP_ROOT=/home/ubuntu/app
INCOMING_DIR="$APP_ROOT/incoming"
BACKUP_DIR="$APP_ROOT/.rollback"
HEALTH_URL=http://127.0.0.1:8080/api/v1/global/health-check

require_inputs() {
  : "${APP_IMAGE:?APP_IMAGE is required}"
  : "${COMPOSE_FILE:?COMPOSE_FILE is required}"
  : "${APPLICATION_YML:?APPLICATION_YML is required}"
  : "${FIREBASE_SERVICE_ACCOUNT_JSON:?FIREBASE_SERVICE_ACCOUNT_JSON is required}"
  test -f "$INCOMING_DIR/$COMPOSE_FILE"
  test -f "$APP_ROOT/.env"
}

backup_current_release() {
  rm -rf "$BACKUP_DIR"
  mkdir -p "$BACKUP_DIR/config" "$BACKUP_DIR/secrets"

  if [ -f "$APP_ROOT/$COMPOSE_FILE" ]; then
    cp "$APP_ROOT/$COMPOSE_FILE" "$BACKUP_DIR/$COMPOSE_FILE"
  elif [ -f "$APP_ROOT/docker-compose.yml" ]; then
    cp "$APP_ROOT/docker-compose.yml" "$BACKUP_DIR/$COMPOSE_FILE"
  fi

  if [ -d "$APP_ROOT/config" ]; then
    sudo cp -a "$APP_ROOT/config/." "$BACKUP_DIR/config/"
  fi
  if [ -d "$APP_ROOT/secrets" ]; then
    sudo cp -a "$APP_ROOT/secrets/." "$BACKUP_DIR/secrets/"
  fi
  docker inspect --format '{{.Config.Image}}' chungbazi-app > "$BACKUP_DIR/image" 2>/dev/null || true
}

install_new_release() {
  umask 077
  mkdir -p "$APP_ROOT/config" "$APP_ROOT/secrets"
  printf '%s' "$APPLICATION_YML" > "$APP_ROOT/.application.yml.tmp"
  printf '%s' "$FIREBASE_SERVICE_ACCOUNT_JSON" > "$APP_ROOT/.firebase.json.tmp"
  test -s "$APP_ROOT/.application.yml.tmp"
  test -s "$APP_ROOT/.firebase.json.tmp"

  cp "$INCOMING_DIR/$COMPOSE_FILE" "$APP_ROOT/$COMPOSE_FILE"
  sudo install -o 10001 -g 10001 -m 0400 "$APP_ROOT/.application.yml.tmp" "$APP_ROOT/config/application.yml"
  sudo install -o 10001 -g 10001 -m 0400 "$APP_ROOT/.firebase.json.tmp" "$APP_ROOT/secrets/firebase-service-account.json"
  rm -f "$APP_ROOT/.application.yml.tmp" "$APP_ROOT/.firebase.json.tmp"
}

compose() {
  APP_IMAGE="$APP_IMAGE" docker compose -f "$APP_ROOT/$COMPOSE_FILE" "$@"
}

rollback() {
  trap - ERR
  set +e
  compose logs --tail=100 chungbazi-app

  if [ -f "$BACKUP_DIR/$COMPOSE_FILE" ]; then
    cp "$BACKUP_DIR/$COMPOSE_FILE" "$APP_ROOT/$COMPOSE_FILE"
    sudo cp -a "$BACKUP_DIR/config/." "$APP_ROOT/config/"
    sudo cp -a "$BACKUP_DIR/secrets/." "$APP_ROOT/secrets/"
    PREVIOUS_IMAGE=$(cat "$BACKUP_DIR/image")
    if [ -n "$PREVIOUS_IMAGE" ]; then
      APP_IMAGE="$PREVIOUS_IMAGE" compose up -d
    fi
  fi

  exit 1
}

wait_until_healthy() {
  for attempt in $(seq 1 30); do
    if curl --connect-timeout 2 --max-time 5 --fail --silent --output /dev/null "$HEALTH_URL"; then
      return 0
    fi
    sleep 5
  done
  return 1
}

require_inputs
cd "$APP_ROOT"
backup_current_release
trap rollback ERR
install_new_release
compose config --quiet
compose pull
compose up -d
wait_until_healthy
trap - ERR
docker image prune -f
rm -rf "$INCOMING_DIR" "$BACKUP_DIR"
