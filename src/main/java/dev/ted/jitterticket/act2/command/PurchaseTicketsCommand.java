package dev.ted.jitterticket.act2.command;

public record PurchaseTicketsCommand(String concertId, String customerId, String ticketId, int quantity) { }
