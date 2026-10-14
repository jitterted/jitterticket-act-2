package dev.ted.jitterticket.act2.command;

import dev.ted.jitterticket.act2.EventLog;
import dev.ted.jitterticket.act2.SequencedEvent;
import dev.ted.jitterticket.act2.event.CustomerRegistered;
import dev.ted.jitterticket.act2.event.TicketsSold;
import dev.ted.jitterticket.act2.readmodel.AvailableConcerts;
import dev.ted.jitterticket.act2.readmodel.BookedDates;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static dev.ted.jitterticket.act2.EventBuilders.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommandExecutorTest {

    @Test
    void emptyLog_EventAppendedAsSeq1() {
        // GIVEN: an empty log
        EventLog eventLog = new EventLog();
        CommandExecutor commandExecutor = new CommandExecutor(eventLog, new BookedDates(), new AvailableConcerts());

        // WHEN
        ExecutionResult result = commandExecutor.execute(
                new RegisterCustomerCommand("cu-1", "kit@example.com"));

        // THEN
        assertThat(result)
                .isEqualTo(new ExecutionResult.Appended(
                        new SequencedEvent(1, new CustomerRegistered("cu-1", "kit@example.com"))));
    }

    @Test
    void readModelUpToDate_EventAppendedAfterTheLastOne() {
        // GIVEN: two events, and Available Concerts has applied both
        EventLog eventLog = new EventLog();
        AvailableConcerts availableConcerts = new AvailableConcerts();
        eventLog.register(availableConcerts);
        eventLog.appendAll(events(
                concertScheduled("ct-1").withTicketPrice(45),
                customerRegistered("cu-1")));
        CommandExecutor commandExecutor = new CommandExecutor(eventLog, new BookedDates(), availableConcerts);

        // WHEN
        ExecutionResult result = commandExecutor.execute(
                new PurchaseTicketsCommand("ct-1", "cu-1", "t-1", 6));

        // THEN: the price came from the Available Concerts decision context
        assertThat(result)
                .isEqualTo(new ExecutionResult.Appended(
                        new SequencedEvent(3, new TicketsSold("ct-1", "cu-1", "t-1", 6, 270))));
    }

    @Test
    void readModelBehindTheLog_ConflictAndNothingAppended() {
        // GIVEN: the log has two events...
        EventLog eventLog = new EventLog();
        eventLog.appendAll(events(
                concertScheduled("ct-1"),
                customerRegistered("cu-1")));
        // ...but Available Concerts has only caught up to the first
        AvailableConcerts availableConcerts = new AvailableConcerts();
        availableConcerts.apply(eventLog.events().getFirst());
        CommandExecutor commandExecutor = new CommandExecutor(eventLog, new BookedDates(), availableConcerts);

        // WHEN
        ExecutionResult result = commandExecutor.execute(
                new PurchaseTicketsCommand("ct-1", "cu-1", "t-1", 6));

        // THEN
        assertThat(result)
                .isEqualTo(new ExecutionResult.Conflict(1, 2));
        assertThat(eventLog.lastSequenceNumber())
                .isEqualTo(2);
    }

    // Same stale situation, two BookedDatesSources: see LiveBookedDatesQuery's workshop note

    @Test
    void scheduleOnDateTheReadModelMissed_FromStaleReadModel_Conflict() {
        // GIVEN: 2026-12-05 is booked in the log...
        EventLog eventLog = logWithTwoConcertsScheduled();
        // ...but Booked Dates has only caught up to the first
        BookedDates bookedDates = new BookedDates();
        bookedDates.apply(eventLog.events().getFirst());
        CommandExecutor commandExecutor = new CommandExecutor(eventLog, bookedDates, new AvailableConcerts());

        // WHEN
        ExecutionResult result = commandExecutor.execute(scheduleOn20261205());

        // THEN: the decider approved, only the sequence-number check stopped it
        assertThat(result)
                .isEqualTo(new ExecutionResult.Conflict(1, 2));
        assertThat(eventLog.lastSequenceNumber())
                .isEqualTo(2);
    }

    @Test
    void scheduleOnDateTheReadModelMissed_FromLiveQuery_DateAlreadyBooked() {
        // GIVEN: 2026-12-05 is booked in the log, and the live query reads the log itself
        EventLog eventLog = logWithTwoConcertsScheduled();
        CommandExecutor commandExecutor = new CommandExecutor(
                eventLog, new LiveBookedDatesQuery(eventLog), new AvailableConcerts());

        // WHEN / THEN: the decider itself rejects it
        assertThatThrownBy(() -> commandExecutor.execute(scheduleOn20261205()))
                .isInstanceOf(DateAlreadyBookedException.class);
        assertThat(eventLog.lastSequenceNumber())
                .isEqualTo(2);
    }

    private static EventLog logWithTwoConcertsScheduled() {
        EventLog eventLog = new EventLog();
        eventLog.appendAll(events(
                concertScheduled("ct-1")
                        .withShowDateTime(LocalDateTime.of(2026, 11, 14, 20, 0)),
                concertScheduled("ct-2")
                        .withShowDateTime(LocalDateTime.of(2026, 12, 5, 21, 0))));
        return eventLog;
    }

    private static ScheduleConcertCommand scheduleOn20261205() {
        return new ScheduleConcertCommand(
                "ct-3", "Cosmic Drift", LocalDateTime.of(2026, 12, 5, 19, 0), LocalTime.of(18, 0),
                100, 40, 4);
    }
}
