package dev.ted.jitterticket.act2.command;

import dev.ted.jitterticket.act2.event.CustomerRegistered;

public final class RegisterCustomerDecider {

    public static CommandResult decide(RegisterCustomerCommand command) {
        return new CommandResult(new CustomerRegistered(
                command.customerId(),
                command.email()));
    }

    private RegisterCustomerDecider() {
    }
}
