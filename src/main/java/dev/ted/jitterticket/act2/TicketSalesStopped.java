package dev.ted.jitterticket.act2;

public record TicketSalesStopped(String concertId,
                                 String reason) implements Event {
}
