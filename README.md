# JitterTicket: Act 2 in code

The Miro board's Act 2, Round 1 in code: an event log with no subscriptions, where **every read model is handed every event**.

```
mvn test
```

Needs Java 21 or later.

## What's here

- **Events.** `Event` and its seven records: the event types from the board.
- **`EventLog`.** Append-only and ordered. It hands each appended event to every registered `ReadModel`.
- **`ReadModel`.** One method: `apply(Event)`.
- **Two finished read models.** `AvailableConcerts` and `BookedDates`, each with tests.
- **`ScriptedRun`** (in `src/test`). Seq 1 to 15 from the board, for use as test fixtures.

## The `default` branch

Every read model's `switch` ends with `default -> { }`. Each event that lands there is one the station counted as READ but not APPLIED.

## Test shape

GIVEN events, THEN the query returns the expected state:

```java
BookedDates bookedDates = given(ScriptedRun.upToSeq(8));

assertThat(bookedDates.bookedDates())
        .containsExactly(...);
```

## Your turn

Write `ConcertSales` and `MyTickets`, test first. The `solution` branch has one version of each.
