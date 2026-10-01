package dev.ted.jitterticket.act2;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Feeds the "Available Concerts" screen: what's on, when, and how many tickets are left.
 */
public class AvailableConcerts implements ReadModel {

    public record Concert(String concertId,
                          String artist,
                          LocalDateTime showDateTime,
                          LocalTime doorsTime,
                          int ticketPrice,
                          int ticketsLeft) {
    }

    private final Map<String, Concert> concerts = new LinkedHashMap<>();

    @Override
    public void apply(Event event) {
        switch (event) {
            case ConcertScheduled scheduled -> concerts.put(
                    scheduled.concertId(),
                    new Concert(scheduled.concertId(),
                                scheduled.artist(),
                                scheduled.showDateTime(),
                                scheduled.doorsTime(),
                                scheduled.ticketPrice(),
                                scheduled.capacity()));
            case ConcertRescheduled rescheduled -> concerts.computeIfPresent(
                    rescheduled.concertId(),
                    (concertId, concert) -> new Concert(concert.concertId(),
                                                concert.artist(),
                                                rescheduled.newShowDateTime(),
                                                rescheduled.newDoorsTime(),
                                                concert.ticketPrice(),
                                                concert.ticketsLeft()));
            case TicketsSold sold -> concerts.computeIfPresent(
                    sold.concertId(),
                    (concertId, concert) -> new Concert(concert.concertId(),
                                                concert.artist(),
                                                concert.showDateTime(),
                                                concert.doorsTime(),
                                                concert.ticketPrice(),
                                                concert.ticketsLeft() - sold.quantity()));
            default -> {
                // Read, but not applied: this read model doesn't care about it.
            }
        }
    }

    /**
     * "Read model, give me the list of available concerts."
     */
    public List<Concert> availableConcerts() {
        return concerts.values().stream()
                       .sorted(Comparator.comparing(Concert::showDateTime))
                       .toList();
    }
}
