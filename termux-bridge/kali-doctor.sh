#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

URL="${KALI_ORCH_URL:-http://127.0.0.1:8899}"
TOKEN="${KALI_ORCH_TOKEN:-}"

echo "Kali Orchestrator connection doctor"
echo "URL: $URL"

if [ -z "$TOKEN" ]; then
  echo "[fail] KALI_ORCH_TOKEN missing"
  exit 1
fi

echo "[ok] KALI_ORCH_TOKEN set"

echo "Health:"
curl -s "$URL/health" || true
echo ""

echo "Actions:"
curl -s "$URL/actions" -H "X-Orchestrator-Token: $TOKEN" || true
echo ""
