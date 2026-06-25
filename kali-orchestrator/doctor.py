#!/usr/bin/env python3
from __future__ import annotations

import json
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parent
CONFIG = ROOT / "config.json"


def main() -> int:
    print("Kali Orchestrator Doctor")
    ok = True

    if CONFIG.exists():
        print("[ok] config.json exists")
        cfg = json.loads(CONFIG.read_text(encoding="utf-8"))
        token = str(cfg.get("api_token", ""))
        if not token or token == "CHANGE_ME":
            print("[fail] api_token is not configured")
            ok = False
        else:
            print("[ok] api_token configured")

        targets = cfg.get("policy", {}).get("allowed_targets", [])
        if targets:
            print(f"[ok] allowed_targets: {targets}")
        else:
            print("[fail] allowed_targets empty")
            ok = False
    else:
        print("[fail] config.json missing. Copy config.example.json first.")
        ok = False

    for tool in ["python3", "curl"]:
        if shutil.which(tool):
            print(f"[ok] {tool} found")
        else:
            print(f"[fail] {tool} missing")
            ok = False

    if shutil.which("nmap"):
        print("[ok] nmap found")
    else:
        print("[warn] nmap missing; scan_host/service_inventory will fail until installed")

    print("doctor result:", "OK" if ok else "NEEDS FIX")
    return 0 if ok else 1


if __name__ == "__main__":
    raise SystemExit(main())
