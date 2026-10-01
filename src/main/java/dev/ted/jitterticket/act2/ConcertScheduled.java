package dev.ted.jitterticket.act2;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record ConcertScheduled(String concertId,
                               String artist,
                               LocalDateTime showDateTime,
                               LocalTime doorsTime,
                               int capacity,
                               int ticketPrice,
                               int maxTicketsPerPurchase) implements Event {
}
