my e-comers website

ReadMe in development

Need to have a redis server running, on port 6379

need to have a postgresql server running, on port 5432

for testing with postgresql

```bash
sudo -u postgres psql

CREATE DATABASE store;

CREATE ROLE storedev WITH LOGIN PASSWORD 'storedev';

ALTER DATABASE store OWNER TO storedev;
```