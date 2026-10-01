package dev.ted.jitterticket.act2;

public record TicketsSold(String concertId,
                          String customerId,
                          String ticketId,
                          int quantity,
                          int totalCost) implements Event {
}
