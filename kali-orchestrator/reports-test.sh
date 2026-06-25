#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

TOKEN="${KALI_ORCH_TOKEN:-CHANGE_ME}"
URL="${KALI_ORCH_URL:-http://127.0.0.1:8899}"
LIMIT="${1:-10}"

echo "Recent reports:"
curl -s "$URL/reports?limit=$LIMIT" \
  -H "X-Orchestrator-Token: $TOKEN" | jq .
