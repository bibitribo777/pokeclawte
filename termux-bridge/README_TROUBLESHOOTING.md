# Troubleshooting

## PokeClaw does not open

Check:

```text
PokeClaw APK installed
Package is io.agents.pokeclaw
External Automation enabled
Termux command has am available
```

Try direct:

```bash
./direct-am-test.sh "how much battery left"
```

## Bridge does not answer

```bash
./status.sh
cat bridge.log
curl http://127.0.0.1:8787/health
```

## Telegram says launched but nothing happens

Test without Telegram first:

```bash
./test.sh
```

Then test direct:

```bash
./direct-am-test.sh "how much battery left"
```

## Android kills Termux

Run:

```bash
termux-wake-lock
```

Then set Termux and PokeClaw battery mode to unrestricted.
