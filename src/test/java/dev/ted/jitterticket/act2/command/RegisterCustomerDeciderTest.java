package dev.ted.jitterticket.act2.command;

import dev.ted.jitterticket.act2.event.CustomerRegistered;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterCustomerDeciderTest {

    @Test
    void newCustomer_GeneratesCustomerRegistered() {
        // GIVEN
        RegisterCustomerCommand command = new RegisterCustomerCommand("cu-1", "kit@example.com");

        // WHEN
        CommandResult result = RegisterCustomerDecider.decide(command);

        // THEN
        assertThat(result.event())
                .isEqualTo(new CustomerRegistered("cu-1", "kit@example.com"));
    }
}
