# PhoneWallet Full Stack — Single Server :8090

This ZIP contains the hardened Spring Boot backend and React source in one project.
React is built into Spring Boot's `src/main/resources/static` directory.

## Run everything

```bash
./build-and-run.sh
```

Then open:

```text
http://localhost:8090
```

Only Spring Boot serves HTTP at runtime. There is no separate frontend server.

For details, read `ONE_SERVER_README.md`.
