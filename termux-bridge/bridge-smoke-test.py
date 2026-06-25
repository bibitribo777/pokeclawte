#!/usr/bin/env python3
"""Tiny import-level smoke test for bridge.py."""

import importlib.util
from pathlib import Path

path = Path(__file__).resolve().parent / "bridge.py"
spec = importlib.util.spec_from_file_location("bridge", path)
module = importlib.util.module_from_spec(spec)
assert spec and spec.loader
spec.loader.exec_module(module)

assert hasattr(module, "APP")
assert hasattr(module, "launch_pokeclaw")
print("bridge.py import smoke test OK")
