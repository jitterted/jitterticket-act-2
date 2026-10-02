package dev.ted.jitterticket.act2.command;

/**
 * The data a "Buy Tickets" form would POST. No price: the client doesn't get to set that.
 */
public record PurchaseTicketsRequest(String concertId,
                                     String customerId,
                                     int quantity) {
}
