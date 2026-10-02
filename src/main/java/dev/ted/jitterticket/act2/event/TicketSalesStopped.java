package dev.ted.jitterticket.act2.event;

public record TicketSalesStopped(String concertId,
                                 String reason) implements Event {
}
