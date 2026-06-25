# Known limits

This folder prepares the Termux side only.

Working now:

```text
Termux -> Android intent -> PokeClaw foreground task
Telegram polling -> Termux -> PokeClaw foreground task
```

Not solved yet:

```text
Final PokeClaw task result back into Telegram
Full locked-screen automation
Reliable OEM background survival on every ROM
APK-side callback sender
```

Reason:

PokeClaw can send callback broadcasts, but Termux is not a normal Android broadcast receiver. The clean later fix is APK-side HTTP callback to the local bridge.
