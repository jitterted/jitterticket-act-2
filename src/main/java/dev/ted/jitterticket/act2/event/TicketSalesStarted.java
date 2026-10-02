package dev.ted.jitterticket.act2.event;

public record TicketSalesStarted(String concertId,
                                 String reason) implements Event {
}
