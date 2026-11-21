#!/usr/bin/env bash
set -euo pipefail

APP_DIR="${APP_DIR:-$HOME/projek}"
cd "$APP_DIR"

if [ ! -f "$APP_DIR/.env" ]; then
  echo "ERROR: $APP_DIR/.env not found. Create it with DB, Sumopod, and Neon S3 variables."
  exit 1
fi

if [ -f "$APP_DIR/webapps/ROOT.war.new" ]; then
  mv -f "$APP_DIR/webapps/ROOT.war.new" "$APP_DIR/webapps/ROOT.war"
fi

if [ ! -f "$APP_DIR/webapps/ROOT.war" ]; then
  echo "ERROR: $APP_DIR/webapps/ROOT.war not found."
  exit 1
fi

docker compose pull zstore caddy
docker compose up -d --force-recreate zstore

echo "Waiting for Z-Store health check..."
READY=0
for i in $(seq 1 30); do
  if curl -fsS http://127.0.0.1:8080/health | grep -q '"ok"'; then
    echo "Tomcat healthy: $(curl -fsS http://127.0.0.1:8080/health)"
    READY=1
    break
  fi
  sleep 2
done

if [ "$READY" -ne 1 ]; then
  echo "ERROR: Health check failed. Recent Tomcat logs:"
  docker compose logs --tail=80 zstore
  exit 1
fi

docker compose up -d caddy
docker compose exec -T caddy caddy reload --config /etc/caddy/Caddyfile --adapter caddyfile >/dev/null 2>&1 || true

# Start Cloudflare Tunnel automatically if CLOUDFLARE_TUNNEL_TOKEN is configured in .env
if grep -qE '^CLOUDFLARE_TUNNEL_TOKEN=.+' "$APP_DIR/.env"; then
  docker compose --profile tunnel pull cloudflared
  docker compose --profile tunnel up -d cloudflared
fi

docker image prune -f >/dev/null 2>&1 || true

echo "Deploy succeeded."
docker compose ps
