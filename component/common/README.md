# Common component

The common component is a runnable platform service reserved for shared platform
capabilities that have a clear operational boundary. It currently owns no business API or
tables. Its database configuration uses the `common` schema of `kelarus_platform`, ready
for additive Liquibase migrations when a concrete common capability is introduced.

## Local setup

Set these environment variables without committing their values:

| Variable | Meaning |
| --- | --- |
| `KELARUS_DATABASE_URL` | Defaults to `jdbc:postgresql://localhost:5434/kelarus_platform` |
| `KELARUS_DATABASE_USERNAME` | Required database username |
| `KELARUS_DATABASE_PASSWORD` | Required database password |
| `KELARUS_EUREKA_URL` | Defaults to `http://localhost:50001/eureka/` |

Run from the repository root:

```powershell
.\gradlew.bat :component:common:bootRun
```

The component listens on port `50004` and registers in Eureka as `common-component`.
