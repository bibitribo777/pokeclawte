#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"
LOG_FILE="orchestrator.log"
PID_FILE="orchestrator.pid"

if [ -f "$PID_FILE" ]; then
  OLD_PID="$(cat "$PID_FILE" || true)"
  if [ -n "${OLD_PID:-}" ] && kill -0 "$OLD_PID" 2>/dev/null; then
    echo "Kali orchestrator already running with PID $OLD_PID"
    exit 0
  fi
fi

if [ -d .venv ]; then
  # shellcheck disable=SC1091
  source .venv/bin/activate
fi

nohup python3 orchestrator.py >> "$LOG_FILE" 2>&1 &
echo $! > "$PID_FILE"

echo "Kali orchestrator started with PID $(cat "$PID_FILE")"
echo "Log: $PWD/$LOG_FILE"
echo "Health: curl http://127.0.0.1:8899/health"
