package dev.ted.jitterticket.act2.command;

import dev.ted.jitterticket.act2.EventLog;
import dev.ted.jitterticket.act2.SequencedEvent;
import dev.ted.jitterticket.act2.event.CustomerRegistered;
import dev.ted.jitterticket.act2.event.TicketsSold;
import dev.ted.jitterticket.act2.readmodel.AvailableConcerts;
import org.junit.jupiter.api.Test;

import static dev.ted.jitterticket.act2.EventBuilders.*;
import static org.assertj.core.api.Assertions.assertThat;

class CommandExecutorTest {

    @Test
    void commandWithNoDecisionContext_EventAppendedAsSeq1() {
        // GIVEN: an empty log
        EventLog eventLog = new EventLog();
        CommandExecutor commandExecutor = new CommandExecutor(eventLog);

        // WHEN
        ExecutionResult result = commandExecutor.execute(
                new RegisterCustomer("cu-1"),
                new RegisterCustomerRequest("kit@example.com"),
                0);

        // THEN
        assertThat(result)
                .isEqualTo(new ExecutionResult.Appended(
                        new SequencedEvent(1, new CustomerRegistered("cu-1", "kit@example.com"))));
    }

    @Test
    void screenUpToDate_EventAppendedAfterTheLastOne() {
        // GIVEN: two events, and a screen built from Available Concerts at LSN 2
        EventLog eventLog = new EventLog();
        AvailableConcerts availableConcerts = new AvailableConcerts();
        eventLog.register(availableConcerts);
        eventLog.appendAll(events(
                concertScheduled("ct-1").withTicketPrice(45),
                customerRegistered("cu-1")));
        long screenBuiltAt = availableConcerts.lastSequenceNumber();
        CommandExecutor commandExecutor = new CommandExecutor(eventLog);

        // WHEN
        ExecutionResult result = commandExecutor.execute(
                new PurchaseTickets("t-1"),
                new PurchaseTicketsRequest("ct-1", "cu-1", 6),
                availableConcerts,
                screenBuiltAt);

        // THEN
        assertThat(result)
                .isEqualTo(new ExecutionResult.Appended(
                        new SequencedEvent(3, new TicketsSold("ct-1", "cu-1", "t-1", 6, 270))));
    }

    @Test
    void logMovedOnSinceScreenWasBuilt_ConflictAndNothingAppended() {
        // GIVEN: a screen built at LSN 2...
        EventLog eventLog = new EventLog();
        AvailableConcerts availableConcerts = new AvailableConcerts();
        eventLog.register(availableConcerts);
        eventLog.appendAll(events(
                concertScheduled("ct-1"),
                customerRegistered("cu-1")));
        long screenBuiltAt = availableConcerts.lastSequenceNumber();
        // ...then someone else registers before the purchase is submitted
        eventLog.append(customerRegistered("cu-2").build());
        CommandExecutor commandExecutor = new CommandExecutor(eventLog);

        // WHEN
        ExecutionResult result = commandExecutor.execute(
                new PurchaseTickets("t-1"),
                new PurchaseTicketsRequest("ct-1", "cu-1", 6),
                availableConcerts,
                screenBuiltAt);

        // THEN
        assertThat(result)
                .isEqualTo(new ExecutionResult.Conflict(2, 3));
        assertThat(eventLog.lastSequenceNumber())
                .isEqualTo(3);
    }
}
