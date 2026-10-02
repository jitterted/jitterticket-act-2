package dev.ted.jitterticket.act2.command;

public record RegisterCustomerCommand(
        String customerId,
        String email
) {
}
