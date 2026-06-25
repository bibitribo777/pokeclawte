#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

cd "$(dirname "$0")"
PID_FILE="bridge.pid"

if [ ! -f "$PID_FILE" ]; then
  echo "No bridge.pid found. Bridge may not be running."
  exit 0
fi

PID="$(cat "$PID_FILE" || true)"
if [ -z "${PID:-}" ]; then
  rm -f "$PID_FILE"
  echo "Empty PID file removed."
  exit 0
fi

if kill -0 "$PID" 2>/dev/null; then
  kill "$PID"
  echo "Stopped bridge PID $PID"
else
  echo "PID $PID is not running"
fi

rm -f "$PID_FILE"
