# Launch flow

```text
HTTP /task
  -> bridge.py
  -> am start
  -> io.agents.pokeclaw/.automation.ExternalAutomationActivity
  -> PokeClaw validates External Automation setting
  -> ComposeChatActivity
  -> taskFlowController.sendTask(...)
  -> visible phone automation
```

Telegram flow:

```text
Telegram message
  -> telegram_bot.py polling
  -> local bridge /task
  -> same PokeClaw flow
```
