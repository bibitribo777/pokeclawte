# Doctor

Run this after install:

```bash
cd kali-orchestrator
./doctor.sh
```

Checks:

```text
config.json exists
api_token configured
allowed_targets configured
python3 available
curl available
nmap available or warning
```

If `nmap` is missing, `ping`, `dns_check`, `web_check`, and `tls_check` can still work, but `scan_host` and `service_inventory` need `nmap`.
