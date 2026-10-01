package dev.ted.jitterticket.act2;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Feeds the venue calendar: which dates are booked, and by whom.
 */
public class BookedDates implements ReadModel {

    public record BookedDate(LocalDate date, String artist) {
    }

    private final Map<String, BookedDate> bookedDatesByConcertId = new LinkedHashMap<>();

    @Override
    public void apply(Event event) {
        switch (event) {
            case ConcertScheduled scheduled -> bookedDatesByConcertId.put(
                    scheduled.concertId(),
                    new BookedDate(scheduled.showDateTime().toLocalDate(),
                                   scheduled.artist()));
            case ConcertRescheduled rescheduled -> bookedDatesByConcertId.computeIfPresent(
                    rescheduled.concertId(),
                    (concertId, booked) -> new BookedDate(rescheduled.newShowDateTime().toLocalDate(),
                                                  booked.artist()));
            default -> {
                // Read, but not applied: most events end up here.
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
}
