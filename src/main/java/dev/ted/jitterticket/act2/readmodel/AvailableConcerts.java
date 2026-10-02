package dev.ted.jitterticket.act2.readmodel;

import dev.ted.jitterticket.act2.SequencedEvent;
import dev.ted.jitterticket.act2.command.TicketPriceSnapshot;
import dev.ted.jitterticket.act2.event.ConcertRescheduled;
import dev.ted.jitterticket.act2.event.ConcertScheduled;
import dev.ted.jitterticket.act2.event.TicketsSold;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Feeds the "Available Concerts" screen: the concerts you can still buy tickets for.
 * <p>
 * The screen doesn't show how many tickets are left, so the query doesn't return it.
 * But this read model has to track tickets left internally: it's the only way to know
 * when a concert sells out and should drop off the list. (Once a processor emits
 * Ticket Sales Stopped, that event can do this job instead, and the count can go.)
 */
public class AvailableConcerts implements ReadModel {

    /**
     * What the screen shows for each concert.
     */
    public record Concert(String concertId,
                          String artist,
                          LocalDateTime showDateTime,
                          LocalTime doorsTime,
                          int ticketPrice) {
    }

    private final Map<String, Concert> concerts = new LinkedHashMap<>();
    private final Map<String, Integer> ticketsLeftByConcertId = new HashMap<>();
    private long lastSequenceNumber = 0;

    @Override
    public void apply(SequencedEvent sequencedEvent) {
        lastSequenceNumber = sequencedEvent.sequenceNumber();
        switch (sequencedEvent.event()) {
            case ConcertScheduled scheduled -> {
                concerts.put(scheduled.concertId(),
                             new Concert(scheduled.concertId(),
                                         scheduled.artist(),
                                         scheduled.showDateTime(),
                                         scheduled.doorsTime(),
                                         scheduled.ticketPrice()));
                ticketsLeftByConcertId.put(scheduled.concertId(), scheduled.capacity());
            }
            case ConcertRescheduled rescheduled -> concerts.computeIfPresent(
                    rescheduled.concertId(),
                    (concertId, concert) -> new Concert(concert.concertId(),
                                                        concert.artist(),
                                                        rescheduled.newShowDateTime(),
                                                        rescheduled.newDoorsTime(),
                                                        concert.ticketPrice()));
            case TicketsSold sold -> ticketsLeftByConcertId.computeIfPresent(
                    sold.concertId(),
                    (concertId, ticketsLeft) -> ticketsLeft - sold.quantity());
            default -> {
                // Read, but not applied: this read model doesn't care about it.
            }
        }
    }

    /**
     * "Read model, give me the list of available concerts."
     * Only concerts that still have tickets left.
     */
    public List<Concert> availableConcerts() {
        return concerts.values().stream()
                       .filter(this::hasTicketsLeft)
                       .sorted(Comparator.comparing(Concert::showDateTime))
                       .toList();
    }

    /**
     * Look up one available concert, for example to find its ticket price.
     * Empty if this read model hasn't heard of it (yet), or if it's sold out.
     */
    public Optional<Concert> findConcert(String concertId) {
        return Optional.ofNullable(concerts.get(concertId))
                       .filter(this::hasTicketsLeft);
    }

    public Optional<TicketPriceSnapshot> ticketPriceFor(String concertId) {
        return findConcert(concertId)
                .map(concert -> new TicketPriceSnapshot(concert.ticketPrice()));
    }

    @Override
    public long lastSequenceNumber() {
        return lastSequenceNumber;
    }

    private boolean hasTicketsLeft(Concert concert) {
        return ticketsLeftByConcertId.get(concert.concertId()) > 0;
    }
}
