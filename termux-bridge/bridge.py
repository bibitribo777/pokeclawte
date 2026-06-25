#!/usr/bin/env python3
"""
Termux -> PokeClaw bridge.

Runs a small local HTTP API inside Termux and launches PokeClaw through
Android intents. APK build is intentionally not required.
"""

from __future__ import annotations

import json
import os
import shutil
import subprocess
import time
import uuid
from pathlib import Path
from typing import Any, Dict, Optional

from flask import Flask, jsonify, request

APP = Flask(__name__)
ROOT = Path(__file__).resolve().parent
CONFIG_PATH = ROOT / "config.json"
STATE_PATH = ROOT / "state.json"

DEFAULT_CONFIG: Dict[str, Any] = {
    "host": "127.0.0.1",
    "port": 8787,
    "bridge_token": "",
    "pokeclaw_package": "io.agents.pokeclaw",
    "pokeclaw_external_activity": "io.agents.pokeclaw/.automation.ExternalAutomationActivity",
    "run_task_action": "io.agents.pokeclaw.RUN_TASK",
    "run_chat_action": "io.agents.pokeclaw.RUN_CHAT",
    "bring_to_front": True,
    "default_mode": "task",
}


def load_config() -> Dict[str, Any]:
    config = dict(DEFAULT_CONFIG)
    if CONFIG_PATH.exists():
        with CONFIG_PATH.open("r", encoding="utf-8") as f:
            config.update(json.load(f))

    # Environment overrides are useful for Termux:Boot or temporary testing.
    config["host"] = os.getenv("POKECLAW_BRIDGE_HOST", str(config["host"]))
    config["port"] = int(os.getenv("POKECLAW_BRIDGE_PORT", str(config["port"])))
    config["bridge_token"] = os.getenv("POKECLAW_BRIDGE_TOKEN", str(config["bridge_token"]))
    config["pokeclaw_package"] = os.getenv("POKECLAW_PACKAGE", str(config["pokeclaw_package"]))
    config["pokeclaw_external_activity"] = os.getenv(
        "POKECLAW_EXTERNAL_ACTIVITY", str(config["pokeclaw_external_activity"])
    )
    return config


CONFIG = load_config()


def now_ms() -> int:
    return int(time.time() * 1000)


def am_bin() -> str:
    return shutil.which("am") or "/system/bin/am"


def save_state(entry: Dict[str, Any]) -> None:
    try:
        state = []
        if STATE_PATH.exists():
            state = json.loads(STATE_PATH.read_text(encoding="utf-8"))
            if not isinstance(state, list):
                state = []
        state.append(entry)
        # Keep last 100 requests only.
        STATE_PATH.write_text(json.dumps(state[-100:], ensure_ascii=False, indent=2), encoding="utf-8")
    except Exception:
        # State logging must never break task execution.
        pass


def require_token() -> Optional[Any]:
    expected = str(CONFIG.get("bridge_token", "")).strip()
    if not expected:
        return None
    got = request.headers.get("X-Bridge-Token", "").strip()
    if got != expected:
        return jsonify({"ok": False, "error": "unauthorized"}), 401
    return None


def launch_pokeclaw(mode: str, text: str, request_id: Optional[str] = None) -> Dict[str, Any]:
    mode = (mode or "task").lower().strip()
    if mode not in {"task", "chat"}:
        raise ValueError("mode must be task or chat")

    extra_name = "task" if mode == "task" else "chat"
    action = CONFIG["run_task_action"] if mode == "task" else CONFIG["run_chat_action"]
    request_id = request_id or str(uuid.uuid4())

    cmd = [
        am_bin(),
        "start",
        "-n",
        str(CONFIG["pokeclaw_external_activity"]),
        "-a",
        str(action),
        "--es",
        extra_name,
        text,
        "--es",
        "request_id",
        request_id,
    ]

    proc = subprocess.run(cmd, capture_output=True, text=True, timeout=20)
    entry = {
        "id": request_id,
        "ts": now_ms(),
        "mode": mode,
        "text": text,
        "cmd": cmd,
        "returncode": proc.returncode,
        "stdout": proc.stdout.strip(),
        "stderr": proc.stderr.strip(),
        "status": "launched" if proc.returncode == 0 else "failed_to_launch",
    }
    save_state(entry)
    return entry


@APP.get("/health")
def health():
    return jsonify(
        {
            "ok": True,
            "service": "pokeclaw-termux-bridge",
            "package": CONFIG["pokeclaw_package"],
            "activity": CONFIG["pokeclaw_external_activity"],
            "time_ms": now_ms(),
        }
    )


@APP.get("/last")
def last():
    deny = require_token()
    if deny:
        return deny
    if not STATE_PATH.exists():
        return jsonify({"ok": True, "items": []})
    items = json.loads(STATE_PATH.read_text(encoding="utf-8"))
    return jsonify({"ok": True, "items": items[-20:]})


@APP.post("/task")
def task():
    deny = require_token()
    if deny:
        return deny
    data = request.get_json(force=True, silent=False) or {}
    text = str(data.get("task") or data.get("text") or "").strip()
    if not text:
        return jsonify({"ok": False, "error": "missing task/text"}), 400
    result = launch_pokeclaw("task", text, data.get("request_id"))
    return jsonify({"ok": result["returncode"] == 0, **result})


@APP.post("/chat")
def chat():
    deny = require_token()
    if deny:
        return deny
    data = request.get_json(force=True, silent=False) or {}
    text = str(data.get("chat") or data.get("text") or "").strip()
    if not text:
        return jsonify({"ok": False, "error": "missing chat/text"}), 400
    result = launch_pokeclaw("chat", text, data.get("request_id"))
    return jsonify({"ok": result["returncode"] == 0, **result})


@APP.post("/run")
def run():
    deny = require_token()
    if deny:
        return deny
    data = request.get_json(force=True, silent=False) or {}
    mode = str(data.get("mode") or CONFIG.get("default_mode") or "task").lower().strip()
    text = str(data.get("text") or data.get("task") or data.get("chat") or "").strip()
    if not text:
        return jsonify({"ok": False, "error": "missing text"}), 400
    try:
        result = launch_pokeclaw(mode, text, data.get("request_id"))
    except ValueError as exc:
        return jsonify({"ok": False, "error": str(exc)}), 400
    return jsonify({"ok": result["returncode"] == 0, **result})


if __name__ == "__main__":
    APP.run(host=str(CONFIG["host"]), port=int(CONFIG["port"]))
