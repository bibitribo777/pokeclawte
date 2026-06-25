#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail
cd "$(dirname "$0")"
./stop-telegram.sh || true
./stop.sh || true
