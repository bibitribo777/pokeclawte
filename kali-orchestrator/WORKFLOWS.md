# Workflows

Workflows run multiple safe, policy-checked actions in sequence.

The API still enforces:

```text
X-Orchestrator-Token
allowed_targets
action allowlist
blocked command keywords
reports
```

## Endpoints

```text
GET  /workflows
POST /workflow
```

## Available workflows

### quick_host

```text
dns_check
ping
web_check
tls_check
scan_host
```

Run:

```bash
./workflow-test.sh 192.168.1.20 quick_host 50
```

PokeClaw Chat:

```text
/kali workflow quick_host 192.168.1.20 50
```

### web_audit

```text
web_check
tls_check
scan_host
```

Run:

```bash
./workflow-test.sh http://192.168.1.20 web_audit 50
```

PokeClaw Chat:

```text
/kali workflow web_audit http://192.168.1.20 50
```

## Custom workflow file

Create:

```text
workflows/name.json
```

Format:

```json
{
  "name": "name",
  "description": "Safe workflow description",
  "default_args": {
    "top_ports": 50
  },
  "steps": [
    {"id": "dns", "action": "dns_check"},
    {"id": "ping", "action": "ping"}
  ]
}
```

Only actions already registered in the orchestrator can run.
