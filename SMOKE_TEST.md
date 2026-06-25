# Smoke Test — PhoneAgent Lab v0.7.1-fork.1
**Datum:** 2026-06-25 | **Device:** Pixel 8a (41021JEKB12613) | **Build:** CI-Artifact assembleDebug

---

## 🔴 KRITISCHE BUGS — App-Flow blockiert

### BUG-01 — Zahnrad-Icon öffnet Settings nicht
- **Pfad:** Chat-Screen → TopAppBar → Zahnrad (oben rechts)
- **Erwartet:** SettingsActivity öffnet sich
- **Tatsächlich:** Kein Effekt, kein Screen-Wechsel
- **Datei:** `ui/chat/ChatScreen.kt:459` — `IconButton(onClick = onSettings)` vorhanden, aber Touch landet nicht dort
- **Vermutung:** Compose-Hitbox-Problem, Scaffold oder benachbarte Surface-Elemente konsumieren den Touch

### BUG-02 — Local/Cloud-Tabs reagieren nicht
- **Pfad:** Chat-Screen → TopAppBar → "Local" / "Cloud" Buttons
- **Erwartet:** Tab-Wechsel, Modell-Dropdown zeigt Local- bzw. Cloud-Modelle
- **Tatsächlich:** Kein visuelles Feedback, kein Wechsel
- **Datei:** `ui/chat/ChatScreen.kt:432–458` — Surface-onClick vorhanden, Touch landet nicht

### BUG-03 — "No model selected" öffnet Hamburger-Drawer statt Modell-Dropdown
- **Pfad:** Chat-Screen → "No model selected ↕" oben links tippen
- **Erwartet:** Modell-Auswahl-Dropdown öffnet sich
- **Tatsächlich:** Hamburger-Menü öffnet sich
- **Ursache:** Hamburger-Button-Hitbox überlappt den Modell-Chip

### BUG-04 — LLM-Chip unten links setzt Quick-Template ins Eingabefeld
- **Pfad:** Chat-Screen → "LLM · OpenAI · Cloud ↓" Chip unten tippen
- **Erwartet:** Provider/Modell-Dropdown öffnet sich
- **Tatsächlich:** Das letzte Quick-Template-Item wird ins Eingabefeld übernommen
- **Ursache:** LLM-Chip überlappt die Template-Liste; falsches Z-Order oder Scroll-Position

### BUG-05 — NVIDIA Provider nicht auswählbar
- **Pfad:** Settings → kein Provider-Switcher vorhanden
- **Problem:** `CloudProvider.NVIDIA` ist im Code definiert mit 7 Modellen, aber:
  - Settings hat keine UI um NVIDIA als Provider zu wählen
  - Es gibt kein Eingabefeld für den NVIDIA API-Key
  - Das Modell-Dropdown zeigt nur den Provider, dessen `defaultBaseUrl` im KVStore steht
  - Es gibt keinen Weg für den User, die baseUrl auf `https://integrate.api.nvidia.com/v1` zu setzen
- **Auswirkung:** Die komplette NVIDIA-Integration ist für den User nicht zugänglich
- **Dateien:** `ui/settings/SettingsActivity.kt` — kein Provider-Switcher; `utils/KVUtils.kt:349` — `getDefaultCloudProvider()` bleibt immer leer

### BUG-06 — Drawer "Models" macht nichts
- **Pfad:** Hamburger-Menü → "Models" tippen
- **Erwartet:** Model-Auswahl oder Download-Screen
- **Tatsächlich:** Drawer bleibt offen, kein Screen-Wechsel, kein Feedback
- **Datei:** `ui/chat/ComposeChatActivity.kt` — `Models`-Eintrag hat keinen funktionierenden onClick

---

## 🟡 FEHLENDE FEATURES — Funktional wichtig

### FEAT-01 — Kein Provider-Switcher in Settings
- User kann OpenAI-Key eingeben, aber nicht zwischen OpenAI / Anthropic / Google / NVIDIA / Custom wechseln
- Kein UI-Element zum Auswählen des Providers
- Workaround: Nur wer die App-Internals kennt und die Custom-Base-URL manuell eingibt

### FEAT-02 — Kein NVIDIA API-Key-Feld in Settings
- `KVUtils.getApiKeyForProvider("NVIDIA")` existiert, wird aber nie befüllt
- Braucht ein dediziertes Eingabefeld in den Cloud-Settings

### FEAT-03 — Kali Lab hat keinen Einstiegspunkt im UI
- `KaliLabActivity` ist im Manifest registriert, wird aber von keiner anderen Activity aufgerufen
- **Fix läuft:** Commit `b8981e6` fügt Eintrag in Settings → Remote Control hinzu, CI baut gerade

### FEAT-04 — Quick-Task-Templates nicht editierbar
- Die Templates ("What time is it in Tokyo?" etc.) sind hardcoded in `ChatScreen.kt`
- User kann keine eigenen Templates anlegen oder vorhandene löschen

### FEAT-05 — Kein visuelles Feedback wenn kein API-Key konfiguriert
- App startet mit "No model selected" ohne Erklärung was zu tun ist
- Kein Onboarding-Hint, kein direkter "Set up API key" Button sichtbar
- User muss selbst Settings → Cloud-Provider finden

### FEAT-06 — WhatsApp und Web Dashboard als "Coming soon" ohne Funktion
- Settings → Remote Control → WhatsApp: Toast "coming later"
- Settings → Remote Control → Web Dashboard: Toast "coming later"
- Kein Hinweis wann oder ob diese Features kommen

### FEAT-07 — Monitor & Auto-Reply ohne Status-Feedback
- Card auf Chat-Screen zeigt immer gleich aus — kein "aktiv/inaktiv"-Indikator sichtbar ohne reinzuklicken

### FEAT-08 — Kein Conversation-History-Indikator
- Drawer zeigt "No conversations yet" auch nach Chats (falls nicht persistiert)
- Unklar ob Chats überhaupt gespeichert werden

---

## 🟠 UX-PROBLEME — Verwirrend oder schlechte Erfahrung

### UX-01 — TopAppBar zu überladen, Touch-Targets zu klein
- 5 interaktive Elemente (Hamburger, Titel/Modell-Chip, Local, Cloud, Zahnrad) in einer Zeile
- Auf einem 6-Zoll-Screen zu eng → Touch-Fehler unvermeidbar
- Local/Cloud und Zahnrad brauchen mehr Abstand oder einen anderen Platz

### UX-02 — "PokeClaw / Cloud AI" Branding falsch
- App heißt jetzt "PhoneAgent Lab" (v0.7.1-fork.1 in About sichtbar)
- Chat-Screen zeigt aber noch "PokeClaw" als Titel und "Cloud AI" als Subtitle
- Inkonsistent mit Fork-Identität

### UX-03 — Hamburger-Drawer "Recent: No conversations yet" immer leer
- Kein Hinweis was der User tun soll
- Kein "+ New Chat" deutlich erklärt
- Drawer-Footer hat "Settings" und "Models" — beide mit Problemen (BUG-01, BUG-06)

### UX-04 — Kali Lab nutzt MaterialTheme statt App-Theme
- `KaliLabActivity.kt:106` — `MaterialTheme { ... }` hardcoded
- Sieht komplett anders aus als der Rest der App (helles Theme vs. dunkles App-Theme)
- Icons fehlen im Kali Lab TopAppBar

### UX-05 — LLM-Chip-Label "LLM · OpenAI · Cloud" irreführend
- Zeigt immer "OpenAI" auch wenn kein Key konfiguriert ist
- Sollte den tatsächlich ausgewählten Provider und das ausgewählte Modell zeigen

### UX-06 — Quick-Task-Template-Tap kopiert Text ins Eingabefeld ohne Bestätigung
- Ein versehentlicher Tap startet sofort den Task — kein "Abbrechen" mehr möglich ohne Text zu löschen

---

## 🔵 TECHNISCHE SCHULDEN

### TECH-01 — KaliLabActivity hat kein Logging
- Null XLog-Calls in `KaliLabActivity.kt` — verstößt gegen CLAUDE.md Debug-Logging-Regel
- Fehler beim Kali-Orchestrator-Call sind nicht traceable

### TECH-02 — KaliLabActivity verwendet eigenen Executor
- `Executors.newSingleThreadExecutor()` direkt in der Activity statt ViewModel/Coroutine
- Kein Lifecycle-Scoping → potenzielle Leaks

### TECH-03 — Kali-Command-Parsing in der Activity
- `client.parse(command)` und `client.run(...)` werden direkt im UI-Thread-Callback aufgerufen
- Fehler-Handling nur als String-Catch, kein strukturiertes Error-Objekt

### TECH-04 — CloudProvider.NVIDIA hardcoded im Kotlin-Enum
- Modell-Liste (7 Modelle) direkt im Enum statt konfigurierbar
- Schwer zu aktualisieren wenn NVIDIA neue Modelle hinzufügt

### TECH-05 — Keine Tests für Kali-Integration
- `kali-orchestrator/` hat keine Unit-Tests
- `KaliOrchestratorClient.kt` hat keine Tests
- `KaliLabActivity` hat keine Instrumentierungstests

---

## ✅ WAS FUNKTIONIERT

| Feature | Status |
|---------|--------|
| App startet ohne Crash | ✓ |
| Chat-Screen lädt | ✓ |
| Quick-Task-Templates sichtbar | ✓ |
| Monitor & Auto-Reply Card | ✓ |
| Kali Lab alle Tabs rendern | ✓ (Run/Workflow/Targets/Findings/Reports/Settings) |
| v0.7.1-fork.1 Version in About | ✓ |
| Hamburger-Drawer öffnet sich | ✓ |
| Drawer "New Chat" Button | ✓ |
| Drawer "Settings" navigiert zu SettingsActivity | ✓ |
| network_security_config.xml Cleartext nur LAN | ✓ |
| Telegram-Bot Auth-Check beim Start | ✓ |
| Kali Token-Feld zeigt Punkte (Password-Masking) | ✓ |

---

## Prioritäts-Reihenfolge für Fixes

1. **BUG-05** — NVIDIA Provider-UI (ohne das ist das Hauptfeature tot)
2. **BUG-01/02** — TopAppBar Touch-Targets (Kernnavigation kaputt)
3. **BUG-03/04** — Modell/LLM-Chip Hitbox-Konflikte
4. **BUG-06** — Drawer Models reparieren oder entfernen
5. **FEAT-01/02** — Provider-Switcher + Key-Eingabe in Settings
6. **UX-02** — Branding vereinheitlichen (PokeClaw → PhoneAgent Lab)
7. **UX-04** — Kali Lab Theme angleichen
