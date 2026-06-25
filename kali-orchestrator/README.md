# Kali Security Orchestrator

A policy-based orchestrator for authorized security labs and your own systems.

```text
Termux / Telegram / PokeClaw
        ↓
Kali Orchestrator API
        ↓
Allowed security inventory actions only
        ↓
JSON + Markdown report
```

## What it does now

Allowed actions:

```text
ping
DNS resolve: dns_check / dns
HTTP check: web_check / web / web_headers
TLS certificate check: tls_check / tls
limited service inventory: scan_host
combined inventory report: service_inventory / inventory
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

This version does not run exploit chains, password attacks, phishing, Wi-Fi disruption, malware, payload generation, stealth, or arbitrary shell commands.

The design is intentionally boring: action allowlist, target allowlist, reports, and no free shell endpoint.

## Install on Kali

```bash
cd kali-orchestrator
chmod +x *.sh *.py
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
  "127.0.0.1/32",
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
./test.sh 127.0.0.1 service_inventory
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

## Termux / Telegram integration

Termux has `kali_client.py`, and the Telegram bot supports `/kali` commands.

Examples:

```text
/kali ping 192.168.1.20
/kali dns_check scanme.nmap.org
/kali web_check http://192.168.1.20
/kali tls_check example.com
/kali scan_host 192.168.1.20 50
/kali service_inventory 192.168.1.20 50
```

## First build goal

```text
Telegram/Termux says: scan 192.168.1.20
Kali validates target allowlist
Kali runs safe service inventory
Kali returns JSON + report id
Termux/Telegram shows short result
```
