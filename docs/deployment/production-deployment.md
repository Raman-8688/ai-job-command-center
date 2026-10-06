# Private Production Deployment

## 1. Hosting Architecture
For personal 24/7 availability (allowing background Gmail polling and scheduled reminders while the laptop is closed), the platform is deployed on a private VPS (e.g., Hetzner, DigitalOcean) or a home server.

## 2. Security Hardening
- **Reverse Proxy:** NGINX or Caddy handling automated Let's Encrypt TLS certificates.
- **Firewall:** Only ports 80/443 exposed publicly; PostgreSQL port 5432 bound exclusively to localhost / internal Docker bridge.
- **Process Management:** Systemd service unit or Docker Compose service with `restart: unless-stopped`.
