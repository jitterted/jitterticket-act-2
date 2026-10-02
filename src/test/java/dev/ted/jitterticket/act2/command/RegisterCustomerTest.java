package dev.ted.jitterticket.act2.command;

import dev.ted.jitterticket.act2.event.CustomerRegistered;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterCustomerTest {

    @Test
    void registerCustomer_GeneratesCustomerRegistered() {
        // GIVEN: no events
        RegisterCustomer registerCustomer = new RegisterCustomer("cu-1");

        // WHEN
        CommandResult result = registerCustomer.execute(
                new RegisterCustomerRequest("kit@example.com"),
                NoDecisionContext.NONE);

        // THEN
        assertThat(result.event())
                .isEqualTo(new CustomerRegistered("cu-1", "kit@example.com"));
    }
}
