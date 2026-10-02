package dev.ted.jitterticket.act2.command;

import dev.ted.jitterticket.act2.event.CustomerRegistered;

public class RegisterCustomer implements Command<RegisterCustomerRequest, NoDecisionContext> {

    private final String customerId;

    public RegisterCustomer(String customerId) {
        this.customerId = customerId;
    }

    @Override
    public CommandResult execute(RegisterCustomerRequest request,
                                 NoDecisionContext noDecisionContext) {
        return new CommandResult(new CustomerRegistered(
                customerId,
                request.email()));
    }
}
