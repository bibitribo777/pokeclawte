# Quickstart

## On Kali / lab machine

```bash
cd $HOME/pokeclawte/kali-orchestrator
chmod +x *.sh *.py
./install.sh
nano config.json
```

Set a long random token:

```json
"api_token": "CHANGE_THIS_TO_A_LONG_RANDOM_SECRET"
```

Set only your own authorized ranges:

```json
"allowed_targets": [
  "127.0.0.1/32",
  "192.168.1.0/24",
  "10.0.0.0/24"
]
```

Start:

```bash
./start.sh
```

Test locally:

```bash
export KALI_ORCH_TOKEN="CHANGE_THIS_TO_A_LONG_RANDOM_SECRET"
./test.sh 127.0.0.1 ping
./test.sh 127.0.0.1 service_inventory
```

## From Termux

```bash
cd $HOME/pokeclawte/termux-bridge
export KALI_ORCH_URL="http://KALI_IP:8899"
export KALI_ORCH_TOKEN="CHANGE_THIS_TO_A_LONG_RANDOM_SECRET"
python kali_client.py ping 192.168.1.20
python kali_client.py scan_host 192.168.1.20 --top-ports 50
```

## From Telegram

Start the Telegram bot from Termux with the same environment:

```bash
export TELEGRAM_BOT_TOKEN="PUT_TOKEN_HERE"
export TELEGRAM_ALLOWED_CHAT_ID="YOUR_CHAT_ID"
export KALI_ORCH_URL="http://KALI_IP:8899"
export KALI_ORCH_TOKEN="CHANGE_THIS_TO_A_LONG_RANDOM_SECRET"
./start-telegram-background.sh
```

Commands:

```text
/kali ping 192.168.1.20
/kali dns_check scanme.nmap.org
/kali web_check http://192.168.1.20
/kali tls_check example.com
/kali scan_host 192.168.1.20 50
/kali service_inventory 192.168.1.20 50
```

Aliases also work:

```text
dns
web
web_headers
tls
inventory
```
