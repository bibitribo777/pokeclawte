# Telegram guide

This uses Telegram long polling from Termux. No webhook and no public server.

## Start

```bash
export TELEGRAM_BOT_TOKEN="PUT_TOKEN_HERE"
./start.sh
./run-telegram.sh
```

Background mode:

```bash
export TELEGRAM_BOT_TOKEN="PUT_TOKEN_HERE"
./start.sh
./start-telegram-background.sh
```

Optional lock to your own chat:

```bash
export TELEGRAM_ALLOWED_CHAT_ID="123456789"
```

## PokeClaw commands

```text
/task how much battery left
/chat what can you do?
```

Normal messages are treated as PokeClaw tasks.

## Kali Orchestrator commands

Kali is not sent as a loose PokeClaw chat prompt. It is routed separately through policy and allowlist checks:

```text
/kali ping 192.168.1.20
/kali dns scanme.nmap.org
/kali web_headers http://192.168.1.20
/kali scan_host 192.168.1.20
/kali inventory 192.168.1.20
```

Required Termux env for Kali routing:

```bash
export KALI_ORCH_URL="http://127.0.0.1:8899"
export KALI_ORCH_TOKEN="YOUR_LONG_RANDOM_TOKEN"
```

If Kali Orchestrator runs on another machine, use its LAN IP:

```bash
export KALI_ORCH_URL="http://192.168.1.50:8899"
```

## Current result behavior

Telegram receives Kali Orchestrator results directly because that path returns JSON.

Telegram receives PokeClaw launch confirmation. Final PokeClaw answer is currently visible inside PokeClaw, not automatically returned to Telegram. That needs the later APK callback work.
