#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import os
import sys
from typing import Any, Dict

import requests


ACTIONS = "ping, dns_check/dns, web_check/web/web_headers, tls_check/tls, scan_host, service_inventory/inventory"


def main() -> int:
    parser = argparse.ArgumentParser(description="Send approved security inventory actions to Kali Orchestrator")
    parser.add_argument("action", help=ACTIONS)
    parser.add_argument("target", help="target from orchestrator allowlist")
    parser.add_argument("--top-ports", type=int, default=50)
    parser.add_argument("--url", default=os.getenv("KALI_ORCH_URL", "http://127.0.0.1:8899"))
    parser.add_argument("--token", default=os.getenv("KALI_ORCH_TOKEN", ""))
    args = parser.parse_args()

    if not args.token:
        print("Missing KALI_ORCH_TOKEN", file=sys.stderr)
        return 2

    payload: Dict[str, Any] = {
        "action": args.action,
        "target": args.target,
        "args": {"top_ports": args.top_ports},
    }
    response = requests.post(
        args.url.rstrip("/") + "/run",
        json=payload,
        headers={"X-Orchestrator-Token": args.token},
        timeout=140,
    )
    try:
        data = response.json()
    except Exception:
        print(response.text)
        return 1

    print(json.dumps(data, ensure_ascii=False, indent=2))
    return 0 if data.get("ok") else 1


if __name__ == "__main__":
    raise SystemExit(main())
