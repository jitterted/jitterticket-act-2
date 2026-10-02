package dev.ted.jitterticket.act2.event;

public record CustomerRegistered(String customerId,
                                 String email) implements Event {
}
