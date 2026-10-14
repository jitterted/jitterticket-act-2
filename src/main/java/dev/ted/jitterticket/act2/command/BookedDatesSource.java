package dev.ted.jitterticket.act2.command;

/**
 * Where the schedule and reschedule decisions get their booked dates from.
 * <p>
 * Two implementations, interchangeable in CommandExecutor:
 * <ul>
 *     <li>BookedDates, the read model: answer is ready, but may be behind the log.</li>
 *     <li>LiveBookedDatesQuery: reads the log every time, so it's never behind.</li>
 * </ul>
 */
public interface BookedDatesSource {

    BookedDatesDecisionContext decisionContext();

    /**
     * The sequence number the decision context is as of.
     */
    long lastSequenceNumber();
}
