# Quick Start

## 1. Start dependencies
```bash
brew services start mysql
brew services restart redis
brew services start kafka
redis-cli ping
kafka-topics --bootstrap-server localhost:9092 --list
```

## 2. Ensure database exists
```sql
CREATE DATABASE IF NOT EXISTS phoneWallet;
```

## 3. Run the complete one-server app
```bash
chmod +x build-and-run.sh build-frontend.sh
./build-and-run.sh
```

The script:
1. builds React,
2. copies `frontend/dist/*` into `src/main/resources/static`,
3. runs backend tests,
4. starts Spring Boot with the local profile.

Open only:
- App: `http://localhost:8090`
- Swagger: `http://localhost:8090/swagger-ui/index.html`
