package dev.ted.jitterticket.act2.command;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * The data a "Schedule Concert" form would POST.
 */
public record ScheduleConcertRequest(String artist,
                                     LocalDateTime showDateTime,
                                     LocalTime doorsTime,
                                     int capacity,
                                     int ticketPrice,
                                     int maxTicketsPerPurchase) {
}
