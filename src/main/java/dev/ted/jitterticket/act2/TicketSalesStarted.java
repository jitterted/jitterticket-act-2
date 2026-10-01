package dev.ted.jitterticket.act2;

public record TicketSalesStarted(String concertId,
                                 String reason) implements Event {
}
