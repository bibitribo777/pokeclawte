# First test checklist

1. Install APK later.
2. Open PokeClaw once.
3. Grant Accessibility.
4. Grant Notification Access if monitor flows are needed.
5. Enable External Automation.
6. Open Termux.
7. Run:

```bash
cd $HOME/pokeclawte/termux-bridge
./direct-am-test.sh "how much battery left"
```

Success means:

```text
PokeClaw opens in foreground and starts task.
```

Then run:

```bash
./start.sh
./test.sh
```
