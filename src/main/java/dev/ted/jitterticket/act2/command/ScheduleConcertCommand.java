package dev.ted.jitterticket.act2.command;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record ScheduleConcertCommand(
        String concertId,
        String artist,
        LocalDateTime showDateTime,
        LocalTime doorsTime,
        int capacity,
        int ticketPrice,
        int maxTicketsPerPurchase
) {
}
