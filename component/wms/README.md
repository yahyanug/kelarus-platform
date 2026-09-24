# WMS component

The WMS component is a runnable service reserved for warehouse-management capabilities. It
currently owns no business API or tables. Its database configuration uses the `wms` schema
of `kelarus_platform`, ready for additive Liquibase migrations when a concrete WMS
capability is introduced.

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
.\gradlew.bat :component:wms:bootRun
```

The component listens on port `50006` and registers in Eureka as `wms-component`.
