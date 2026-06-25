#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail
cd "$(dirname "$0")"
chmod +x *.sh *.py 2>/dev/null || true
echo "Scripts marked executable."
