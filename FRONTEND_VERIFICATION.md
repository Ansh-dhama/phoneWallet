# Frontend verification

- React source parse check: PASS (22 JS/JSX modules)
- Relative import resolution check: PASS (22 source modules)
- Java backend source comparison against the working Runtime-Fixed package: UNCHANGED
- No new npm dependencies were added
- `build-and-run.sh` remains one-server mode and builds React into Spring Boot `static` before starting port 8090

A fresh Vite bundle is intentionally generated on the target Mac by `npm ci && npm run build`, because Vite/Rollup contains platform-specific native binaries.
