# JitterTicket: Act 2 in code

The Miro board's Act 2, Round 1 in code: an event log with no subscriptions, where **every read model is handed every event**.

```
./mvnw test
```

Needs Java 25 or later.

## What's here

Events, commands and read models each have their own package: `event`, `command` and `readmodel`. `EventLog` is in the root package. In IntelliJ they're coloured like the Miro stickies: events orange, commands blue, read models green.

- **Events.** `Event` and its seven records: the event types from the board.
- **`EventLog`.** Append-only and ordered. It gives each appended event its sequence number (from 1) as a `SequencedEvent`, and hands it to every registered `ReadModel`.
- **`ReadModel`.** `apply(SequencedEvent)`, and `lastSequenceNumber()`: the station's LSN.
- **Two finished read models.** `AvailableConcerts` and `BookedDates`, each with tests.
- **Four commands.** `ScheduleConcertCommand`, `RescheduleConcertCommand`, `RegisterCustomerCommand` and `PurchaseTicketsCommand`: the data a form would POST. Each has a `...Decider` that takes the command and a decision context (`BookedDatesDecisionContext` or `TicketPriceDecisionContext`; registering needs none) and returns a `CommandResult` holding the event it decided on. Scheduling or rescheduling onto a date another concert holds throws `DateAlreadyBookedException`.
- **`CommandExecutor`.** Executes a command: it asks the read model this command depends on for its decision context and `lastSequenceNumber()`, calls the decider, then appends the event, but only if the log's last sequence number is still the one the decision context was built from. It returns `ExecutionResult.Appended` or `ExecutionResult.Conflict`.
- **`EndToEndTest`.** The whole loop: run a command through the `CommandExecutor`, and check what the read models show. One test plays seq 1 to 8 as commands and checks that the log comes out as the Scripted Run.
- **`EventBuilders`** (in `src/test`). Builders for events, so a test spells out only the values its assertions depend on: `concertScheduled("ct-1").withCapacity(20)`.
- **`ScriptedRun`** (in `src/test`). Seq 1 to 15 from the board, for tests that check the board's checkpoints (the guide's 3.7).

## The `default` branch

Every read model's `switch` ends with `default -> { }`. Each event that lands there is one the station counted as READ but not APPLIED.

## Test shape

Read models: GIVEN events, THEN the query returns the expected state.

Commands: GIVEN a decision context, WHEN the decider decides, THEN the result holds the expected event.

A read model test:

```java
BookedDates bookedDates = given(ScriptedRun.upToSeq(8));

assertThat(bookedDates.bookedDates())
        .containsExactly(...);
```

## Your turn

Write `ConcertSales` and `MyTickets`, test first. Their test classes are waiting, empty, in `src/test`.
