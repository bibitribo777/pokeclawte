# Background run

Start bridge:

```bash
./start.sh
```

Start Telegram bot in background:

```bash
export TELEGRAM_BOT_TOKEN="PUT_TOKEN_HERE"
./start-telegram-background.sh
```

Status:

```bash
./status.sh
```

Stop all:

```bash
./stop-all.sh
```

Keep Termux alive:

```bash
termux-wake-lock
```

Android battery settings still matter. Set Termux and PokeClaw to unrestricted.
