# Notification component

The notification component is a runnable platform service reserved for notification
capabilities with a clear operational boundary. It currently owns no delivery API or
tables. Its database configuration uses the `notification` schema of `kelarus_platform`,
ready for additive Liquibase migrations when a concrete notification capability is
introduced.

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
.\gradlew.bat :component:notification:bootRun
```

The component listens on port `50005` and registers in Eureka as `notification-component`.
