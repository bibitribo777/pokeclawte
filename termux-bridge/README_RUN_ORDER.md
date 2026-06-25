# Run order

```bash
./chmod-all.sh
./install.sh
./direct-am-test.sh "how much battery left"
./start.sh
./test.sh
```

Telegram after that:

```bash
export TELEGRAM_BOT_TOKEN="PUT_TOKEN_HERE"
./start-telegram-background.sh
```
