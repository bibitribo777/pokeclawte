# Kali Security Orchestrator

A safe, policy-based orchestrator for authorized security labs and your own systems.

```text
Termux / Telegram / PokeClaw
        ↓
Kali Orchestrator API
        ↓
Allowed security actions only
        ↓
JSON + Markdown report
```

## What it does now

Allowed actions:

```text
ping
DNS resolve
HTTP header check
limited service inventory with nmap
combined inventory report
```

API endpoints:

```text
GET  /health
GET  /actions
POST /run
GET  /reports/<id>.json
GET  /reports/<id>.md
```

## What it intentionally does not do

This first version does not run exploit chains, password brute-force, phishing, deauth/Wi-Fi disruption, malware, payload generation, or arbitrary shell commands.

Those actions are blocked in policy by default. Build lab-only workflows later with explicit allowlist, proof-of-ownership, confirmation, and logging.

## Install on Kali

```bash
cd kali-orchestrator
chmod +x install.sh
./install.sh
```

Edit config:

```bash
nano config.json
```

Set:

```json
"api_token": "YOUR_LONG_RANDOM_TOKEN"
```

Add only your own lab/authorized ranges:

```json
"allowed_targets": [
  "192.168.1.0/24",
  "10.0.0.0/24",
  "scanme.nmap.org"
]
```

## Start

```bash
./start.sh
```

## Test

```bash
export KALI_ORCH_TOKEN="YOUR_LONG_RANDOM_TOKEN"
./test.sh 127.0.0.1 inventory
```

Example direct API call:

```bash
curl -X POST http://127.0.0.1:8899/run \
  -H "Content-Type: application/json" \
  -H "X-Orchestrator-Token: YOUR_LONG_RANDOM_TOKEN" \
  -d '{"action":"scan_host","target":"127.0.0.1","args":{"top_ports":50}}'
```

## Run modes

Default:

```json
"runner_mode": "local"
```

This means the orchestrator runs directly on the Kali machine.

SSH mode exists for later:

```json
"runner_mode": "ssh"
```

But the recommended first setup is local on Kali.

## Reports

Every run writes:

```text
reports/<id>.json
reports/<id>.md
```

## First build goal

```text
Telegram/Termux says: scan 192.168.1.20
Kali validates target allowlist
Kali runs safe service inventory
Kali returns JSON + report id
Termux/Telegram shows short result
```
