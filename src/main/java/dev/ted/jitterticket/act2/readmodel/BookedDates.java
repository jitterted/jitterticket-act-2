package dev.ted.jitterticket.act2.readmodel;

import dev.ted.jitterticket.act2.SequencedEvent;
import dev.ted.jitterticket.act2.command.BookedDatesDecisionContext;
import dev.ted.jitterticket.act2.command.BookedDatesSource;
import dev.ted.jitterticket.act2.event.ConcertRescheduled;
import dev.ted.jitterticket.act2.event.ConcertScheduled;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Feeds the venue calendar: which dates are booked, and by whom.
 */
public class BookedDates implements ReadModel, BookedDatesSource {

    public record BookedDate(LocalDate date, String artist) {
    }

    private final Map<String, BookedDate> bookedDatesByConcertId = new LinkedHashMap<>();
    private long lastSequenceNumber = 0;

    @Override
    public void apply(SequencedEvent sequencedEvent) {
        lastSequenceNumber = sequencedEvent.sequenceNumber();
        switch (sequencedEvent.event()) {
            case ConcertScheduled scheduled -> bookedDatesByConcertId.put(
                    scheduled.concertId(),
                    new BookedDate(scheduled.showDateTime().toLocalDate(),
                                   scheduled.artist()));
            case ConcertRescheduled rescheduled -> bookedDatesByConcertId.computeIfPresent(
                    rescheduled.concertId(),
                    (concertId, booked) -> new BookedDate(rescheduled.newShowDateTime().toLocalDate(),
                                                  booked.artist()));
            default -> {
                // Seen, but not applied: most events end up here.
            }
        }
    }

    /**
     * "Booked Dates, show me the venue calendar."
     */
    public List<BookedDate> bookedDates() {
        return bookedDatesByConcertId.values().stream()
                                     .sorted(Comparator.comparing(BookedDate::date))
                                     .toList();
    }

    @Override
    public BookedDatesDecisionContext decisionContext() {
        return new BookedDatesDecisionContext(
                bookedDatesByConcertId.entrySet().stream()
                                      .collect(Collectors.toMap(Map.Entry::getKey,
                                                                entry -> entry.getValue().date())));
    }

    @Override
    public long lastSequenceNumber() {
        return lastSequenceNumber;
    }
}
