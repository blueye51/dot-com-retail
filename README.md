# E-Commerce Website

**README in development**

## Quick Start

The entire application stack can be started with Docker Compose.

```bash
docker compose --profile full up -d
```

This command starts all required services and runs the project in the background.

## Cleanup

To completely remove the project after testing (containers, networks, volumes, and images created by the stack):

```bash
docker compose --profile full down -v --rmi all
```

This returns the system to a clean state after testing.
