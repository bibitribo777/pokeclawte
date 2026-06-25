#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

TOKEN="${KALI_ORCH_TOKEN:-CHANGE_ME}"
URL="${KALI_ORCH_URL:-http://127.0.0.1:8899}"
NAME="${1:-home-router}"
TARGET="${2:-127.0.0.1}"
TYPE="${3:-host}"

echo "Add/update target: $NAME -> $TARGET"
curl -s -X POST "$URL/targets" \
  -H "Content-Type: application/json" \
  -H "X-Orchestrator-Token: $TOKEN" \
  -d "{\"name\":\"$NAME\",\"value\":\"$TARGET\",\"type\":\"$TYPE\"}" | jq .

echo "Targets:"
curl -s "$URL/targets" \
  -H "X-Orchestrator-Token: $TOKEN" | jq .
