package dev.ted.jitterticket.act2.command;

import dev.ted.jitterticket.act2.EventLog;
import dev.ted.jitterticket.act2.SequencedEvent;
import dev.ted.jitterticket.act2.event.ConcertRescheduled;
import dev.ted.jitterticket.act2.event.ConcertScheduled;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * The alternative to deciding from a read model.
 * <p>
 * A read model such as BookedDates can be behind the log, so a decision context taken from it
 * may be stale. This query instead reads the event log every time it's asked,
 * so the command always decides on the latest booked dates.
 * It applies events the same way the BookedDates read model does.
 * <p>
 * <b>Workshop: swapping the read model for this query.</b>
 * Both this query and BookedDates are a {@link BookedDatesSource}, so CommandExecutor
 * takes either one. The swap is only where the executor is created:
 * <pre>{@code
 * new CommandExecutor(eventLog, bookedDates, availableConcerts)                      // read model
 * new CommandExecutor(eventLog, new LiveBookedDatesQuery(eventLog), availableConcerts) // live query
 * }</pre>
 * To show the difference, let BookedDates fall behind (apply only the first event),
 * then schedule a concert on a date booked by the event it missed
 * (see CommandExecutorTest's {@code ..._FromStaleReadModel_...} and {@code ..._FromLiveQuery_...}):
 * <ul>
 *     <li>With BookedDates: the stale decision context says the date is free, the decider approves,
 *         and only the sequence-number check saves us &mdash; the result is a {@code Conflict}.</li>
 *     <li>With this query: the decision context is as of the latest event, so the decider itself
 *         rejects it with {@code DateAlreadyBookedException}, and there's no conflict to retry.</li>
 * </ul>
 * The trade-off: this query replays the whole log on every command, while the read model
 * keeps its answer ready.
 */
public class LiveBookedDatesQuery implements BookedDatesSource {

    private final EventLog eventLog;

    public LiveBookedDatesQuery(EventLog eventLog) {
        this.eventLog = eventLog;
    }

    @Override
    public BookedDatesDecisionContext decisionContext() {
        Map<String, LocalDate> datesByConcertId = new HashMap<>();
        for (SequencedEvent sequencedEvent : eventLog.events()) {
            switch (sequencedEvent.event()) {
                case ConcertScheduled scheduled ->
                        datesByConcertId.put(scheduled.concertId(), scheduled.showDateTime().toLocalDate());
                case ConcertRescheduled rescheduled ->
                        datesByConcertId.computeIfPresent(rescheduled.concertId(),
                                                          (concertId, oldDate) -> rescheduled.newShowDateTime().toLocalDate());
                default -> {
                    // this query doesn't care about it
                }
            }
        }
        return new BookedDatesDecisionContext(datesByConcertId);
    }

    @Override
    public long lastSequenceNumber() {
        return eventLog.lastSequenceNumber();
    }
}
