package dev.ted.jitterticket.act2.command;

import dev.ted.jitterticket.act2.EventLog;
import dev.ted.jitterticket.act2.readmodel.AvailableConcerts;

/**
 * Executes a command. For each command it:
 * <ol>
 *     <li>asks the read model the decision needs for a snapshot, and for its last sequence number,</li>
 *     <li>hands the command and the snapshot to the command's decider,</li>
 *     <li>appends the decided event, but only if the log's last sequence number
 *         is still the one the snapshot was built from.</li>
 * </ol>
 * Booked dates come from a {@link BookedDatesSource}: pass the BookedDates read model,
 * or a {@link LiveBookedDatesQuery} to decide from the log itself.
 */
public class CommandExecutor {

    private final EventLog eventLog;
    private final BookedDatesSource bookedDates;
    private final AvailableConcerts availableConcerts;

    public CommandExecutor(EventLog eventLog,
                           BookedDatesSource bookedDates,
                           AvailableConcerts availableConcerts) {
        this.eventLog = eventLog;
        this.bookedDates = bookedDates;
        this.availableConcerts = availableConcerts;
    }

    public ExecutionResult execute(ScheduleConcertCommand command) {
        BookedDatesSnapshot snapshot = bookedDates.snapshot();
        long lastSequenceNumber = bookedDates.lastSequenceNumber();
        CommandResult result = ScheduleConcertDecider.decide(command, snapshot);
        return append(result, lastSequenceNumber);
    }

    public ExecutionResult execute(RescheduleConcertCommand command) {
        BookedDatesSnapshot snapshot = bookedDates.snapshot();
        long lastSequenceNumber = bookedDates.lastSequenceNumber();
        CommandResult result = RescheduleConcertDecider.decide(command, snapshot);
        return append(result, lastSequenceNumber);
    }

    public ExecutionResult execute(PurchaseTicketsCommand command) {
        TicketPriceSnapshot snapshot = availableConcerts.ticketPriceFor(command.concertId()).orElseThrow();
        long lastSequenceNumber = availableConcerts.lastSequenceNumber();
        CommandResult result = PurchaseTicketsDecider.decide(command, snapshot);
        return append(result, lastSequenceNumber);
    }

    /**
     * Registering needs no snapshot, so there's no read model to ask:
     * the decision is as of the log itself.
     */
    public ExecutionResult execute(RegisterCustomerCommand command) {
        long lastSequenceNumber = eventLog.lastSequenceNumber();
        CommandResult result = RegisterCustomerDecider.decide(command);
        return append(result, lastSequenceNumber);
    }

    private ExecutionResult append(CommandResult result, long expectedLastSequenceNumber) {
        return eventLog.append(result.event(), expectedLastSequenceNumber)
                       .<ExecutionResult>map(ExecutionResult.Appended::new)
                       .orElseGet(() -> new ExecutionResult.Conflict(
                               expectedLastSequenceNumber,
                               eventLog.lastSequenceNumber()));
    }
}
