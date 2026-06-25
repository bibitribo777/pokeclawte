#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

cd "$(dirname "$0")"

LOG_FILE="bridge.log"
PID_FILE="bridge.pid"

if [ -f "$PID_FILE" ]; then
  OLD_PID="$(cat "$PID_FILE" || true)"
  if [ -n "${OLD_PID:-}" ] && kill -0 "$OLD_PID" 2>/dev/null; then
    echo "Bridge already running with PID $OLD_PID"
    exit 0
  fi
fi

if command -v termux-wake-lock >/dev/null 2>&1; then
  termux-wake-lock || true
fi

nohup python bridge.py >> "$LOG_FILE" 2>&1 &
echo $! > "$PID_FILE"

echo "Bridge started with PID $(cat "$PID_FILE")"
echo "Log: $PWD/$LOG_FILE"
echo "Health: curl http://127.0.0.1:8787/health"
