package dev.ted.jitterticket.act2.event;

import java.time.LocalDateTime;

public record TicketSalesScheduled(String concertId,
                                   LocalDateTime salesStartDateTime) implements Event {
}
