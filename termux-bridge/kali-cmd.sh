#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

cd "$(dirname "$0")"

if [ "$#" -lt 2 ]; then
  echo "Usage: ./kali-cmd.sh <action> <target> [top_ports]"
  echo "Actions: ping dns web_headers scan_host inventory"
  exit 2
fi

ACTION="$1"
TARGET="$2"
TOP_PORTS="${3:-50}"

python kali_client.py "$ACTION" "$TARGET" --top-ports "$TOP_PORTS"
