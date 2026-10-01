package dev.ted.jitterticket.act2;

public record CustomerRegistered(String customerId,
                                 String email) implements Event {
}
