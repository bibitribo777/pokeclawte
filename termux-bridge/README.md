# PokeClaw Termux Bridge

This folder prepares the non-APK side of the setup:

```text
Telegram / local HTTP / scripts
        ↓
Termux bridge
        ↓
Android intent
        ↓
PokeClaw foreground task runner
```

The APK can be built later. This bridge only needs Termux and an installed PokeClaw app with External Automation enabled.

## What this gives you

- Local HTTP bridge inside Termux
- `/task`, `/chat`, `/run`, `/health`, `/last` endpoints
- Starts PokeClaw with `am start`, not fragile background broadcast
- Optional Telegram polling bot
- Start/stop/test scripts
- No public server required
- No APK rebuild required for first tests

## Required PokeClaw setting

Inside PokeClaw enable:

```text
Settings -> Remote Control -> External Automation
```

Without this, PokeClaw rejects external automation requests by design.

## Install in Termux

```bash
cd pokeclawte/termux-bridge
chmod +x install.sh
./install.sh
```

Recommended Android settings:

```text
Battery -> Termux -> Unrestricted
Battery -> PokeClaw -> Unrestricted
Allow autostart if your ROM has that setting
```

Then keep Termux awake:

```bash
termux-wake-lock
```

## Start bridge

```bash
./start.sh
```

Health check:

```bash
curl http://127.0.0.1:8787/health
```

## Test PokeClaw launch

```bash
./test.sh
```

Manual test:

```bash
curl -X POST http://127.0.0.1:8787/task \
  -H "Content-Type: application/json" \
  -d '{"task":"how much battery left"}'
```

Expected result:

```text
PokeClaw opens in the foreground and runs the task visibly.
```

## API

### POST `/task`

```json
{"task":"summarize my notifications"}
```

### POST `/chat`

```json
{"chat":"what can you do?"}
```

### POST `/run`

```json
{"mode":"task","text":"open settings and tell me battery percent"}
```

### GET `/last`

Returns the last local bridge launches from `state.json`.

## Optional token

Copy config:

```bash
cp config.example.json config.json
nano config.json
```

Set:

```json
"bridge_token": "CHANGE_ME"
```

Then send requests with:

```bash
-H "X-Bridge-Token: CHANGE_ME"
```

For env-based start:

```bash
export POKECLAW_BRIDGE_TOKEN="CHANGE_ME"
./start.sh
```

## Telegram bot

Create a bot with BotFather and export the token:

```bash
export TELEGRAM_BOT_TOKEN="123456:ABC..."
```

Optional: lock to your own chat id:

```bash
export TELEGRAM_ALLOWED_CHAT_ID="123456789"
```

Start the bridge first:

```bash
./start.sh
```

Then run the bot:

```bash
python telegram_bot.py
```

Commands:

```text
/task how much battery left
/chat what can you do?
```

Normal messages are treated as `/task`.

## Why `am start` and not only `am broadcast`

Modern Android versions can block background receivers from opening an activity. This bridge launches PokeClaw's exported `ExternalAutomationActivity` directly, so PokeClaw can come to the foreground and run the task.

## Current limitation

This bridge can confirm that PokeClaw was launched. It does not yet receive terminal task callbacks from PokeClaw inside Termux. PokeClaw supports callback broadcasts, but Termux needs a dedicated Android receiver or a small companion receiver to collect them cleanly.

For now, use `/last` for local launch history and read final results inside the PokeClaw UI.

## Next build step later

When the APK work starts, the useful next addition is a tiny callback receiver or in-app bridge endpoint so Termux/Telegram can receive:

```text
accepted
completed
failed
cancelled
blocked
rejected
```

Until then, the clean stack is:

```text
Termux triggers.
PokeClaw executes visibly.
User checks result in PokeClaw.
```
