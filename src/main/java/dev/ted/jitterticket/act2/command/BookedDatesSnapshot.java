package dev.ted.jitterticket.act2.command;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * What the schedule and reschedule deciders need to know:
 * which concert holds each booked date.
 */
public record BookedDatesSnapshot(Map<String, LocalDate> bookedDatesByConcertId) {

    public BookedDatesSnapshot {
        bookedDatesByConcertId = Map.copyOf(bookedDatesByConcertId);
    }

    public Set<LocalDate> bookedDates() {
        return bookedDatesByConcertId.values().stream()
                                     .collect(Collectors.toUnmodifiableSet());
    }

    public boolean isBooked(LocalDate date) {
        return bookedDatesByConcertId.containsValue(date);
    }

    /**
     * A concert's own date doesn't count: it can move to another time on the same day.
     */
    public boolean isBookedByAnotherConcert(LocalDate date, String concertId) {
        return bookedDatesByConcertId.entrySet().stream()
                                     .anyMatch(booking -> !booking.getKey().equals(concertId)
                                                          && booking.getValue().equals(date));
    }
}
