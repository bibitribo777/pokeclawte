# Curl examples

Health:

```bash
curl http://127.0.0.1:8787/health
```

Task:

```bash
curl -X POST http://127.0.0.1:8787/task \
  -H "Content-Type: application/json" \
  -d '{"task":"how much battery left"}'
```

Chat:

```bash
curl -X POST http://127.0.0.1:8787/chat \
  -H "Content-Type: application/json" \
  -d '{"chat":"what can you do?"}'
```

Run:

```bash
curl -X POST http://127.0.0.1:8787/run \
  -H "Content-Type: application/json" \
  -d '{"mode":"task","text":"summarize my notifications"}'
```

Last launches:

```bash
curl http://127.0.0.1:8787/last
```
