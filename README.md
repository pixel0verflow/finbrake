# finbrake

Spring Boot service that accepts or declines fund-load attempts against per-customer velocity limits.

## Velocity rules

Per customer:

- max **$5,000** loaded per UTC day
- max **$20,000** loaded per ISO week (Mon 00:00 UTC → next Mon 00:00 UTC)
- max **3** loads per UTC day, regardless of amount

Declined attempts do not consume window budget. Repeated `(customer_id, id)` pairs are silently ignored after the first occurrence.

## Architecture — ports & adapters (hexagonal)

```
src/main/java/net/bartcloud/finbrake/
├── FinbrakeApplication.java            # Spring Boot entry point
│
├── domain/                             # pure Java, no Spring/JPA
│   ├── LoadAttempt.java                # value object: id, customerId, amount, time
│   ├── LoadDecision.java               # value object: id, customerId, accepted
│   ├── VelocityPolicy.java             # static rules: accepts(...) → boolean
│   └── TimeWindows.java                # UTC day/week boundary helpers
│
├── application/
│   ├── LoadAttemptService.java         # @Service implements ProcessLoadAttempt
│   └── port/
│       ├── in/ProcessLoadAttempt.java  # inbound port (use case interface)
│       └── out/LoadAttemptStore.java   # outbound port (persistence contract)
│
└── adapter/
    ├── in/
    │   ├── web/                        # REST adapter
    │   │   ├── LoadAttemptController.java
    │   │   ├── LoadAttemptRequest.java     # JSON DTO + parsing ($-prefixed amount)
    │   │   ├── LoadDecisionResponse.java
    │   │   └── ApiExceptionHandler.java    # @RestControllerAdvice
    │   └── cli/
    │       └── InputFileRunner.java        # CommandLineRunner, gated on finbrake.input
    └── out/
        └── persistence/                # JPA outbound adapter
            ├── LoadAttemptEntity.java          # @Entity, unique (customer_id, attempt_id)
            ├── LoadAttemptJpaRepository.java   # Spring Data interface
            └── LoadAttemptStoreAdapter.java    # @Component implements LoadAttemptStore
```

### Dependency direction

```
adapter/in  ──▶  application (port.in)
                       │
                       ▼
                application service ──▶ domain
                       │
                       ▼
                application (port.out) ◀── adapter/out
```

Domain depends on nothing. Application depends on domain + its own ports. Adapters depend on application ports. Spring framework concerns (`@Service`, `@Component`, `@Entity`, `@RestController`) live at the application/adapter boundary, never in the domain.

### Why hexagonal here

- Swap H2/JPA for any other persistence by writing a new `LoadAttemptStore` impl. No service or domain change.
- Service unit-tested with an in-memory fake store — no Spring context, runs in milliseconds.
- New inbound channels (Kafka consumer, gRPC, batch) plug into the same `ProcessLoadAttempt` port.

## Domain logic flow

`LoadAttemptService.process(attempt)`:

1. dedup check: `store.exists(customerId, id)` → return `Optional.empty()` if seen
2. compute three accepted-only aggregates over `attempt.time()`:
   - daily amount sum
   - daily count
   - weekly amount sum
3. apply `VelocityPolicy.accepts(...)` → boolean
4. persist `(attempt, accepted)` via `store.save`
5. return `Optional.of(LoadDecision)`

Persisting both accepted and declined attempts gives a full audit trail; window queries filter to `accepted=true`.

## Persistence

H2 in-memory, configured in `application.properties`:

- composite uniqueness on `(customer_id, attempt_id)` enforces dedup at the DB layer
- index on `(customer_id, attempt_time)` for the window queries
- `ddl-auto=create-drop`

## Running

### REST mode (default)

```
./gradlew bootRun
curl -X POST http://localhost:8080/load-attempts \
  -H 'Content-Type: application/json' \
  -d '{"id":"1","customer_id":"c","load_amount":"$10.00","time":"2024-01-01T00:00:00Z"}'
```

Responses:
- `200 OK` with `{"id","customer_id","accepted"}` for processed attempts
- `204 No Content` for duplicates
- `400 Bad Request` for malformed/missing fields

### CLI mode

Activates only when `finbrake.input` is set. Output to file or stdout.

```
./gradlew bootRun --args="--finbrake.input=src/test/resources/input.txt --finbrake.output=/tmp/out.txt"
```

## Tests

```
./gradlew test
```

| File | Scope | Notes |
|---|---|---|
| `domain/VelocityPolicyTest` | pure unit | rule boundaries (limits exact / +ε) |
| `domain/TimeWindowsTest` | pure unit | UTC day, ISO week, Sun→Mon transition |
| `application/LoadAttemptServiceTest` | unit + in-memory fake store | dedup, day/week limits, count limit, declined-doesn't-consume, customer isolation |
| `adapter/out/persistence/LoadAttemptStoreAdapterTest` | `@DataJpaTest` slice | JPA queries against real H2 |
| `adapter/in/web/LoadAttemptControllerTest` | `@WebMvcTest` slice + Mockito | 200 / 204 / 400 paths |
| `integration/SampleFixtureParityTest` | `@SpringBootTest` | replays `sample-input.txt` (5 lines), asserts equal to `sample-output.txt` |
| `integration/FullFixtureParityTest` | `@SpringBootTest`, `assumeTrue` gated | replays `input.txt` (full 1000-line provided fixture) if present in `src/test/resources/`, skips otherwise |

`input.txt`/`output.txt` are gitignored (private grader fixtures). `sample-input.txt`/`sample-output.txt` are committed minimal fixtures.

## Build / formatting / static analysis

- Spring Boot 4 on Java 25 (uses Jackson 3 — `tools.jackson.*` package, not `com.fasterxml.jackson.databind.*`)
- **Spotless** with Palantir Java Format — formatting
- **Error Prone** — javac-time bug patterns; runs on `compileJava` only
- **SpotBugs** — bytecode analysis on main + test sources, `effort=MAX`, `reportLevel=HIGH`. HTML reports at `build/reports/spotbugs/`

All four wired into `check`:

```
./gradlew check          # spotless + compile (errorprone) + tests + spotbugs
./gradlew spotlessApply  # autoformat
```

Error Prone is disabled for `compileTestJava` to keep test code free of warning churn from idioms like Mockito stubbing.
