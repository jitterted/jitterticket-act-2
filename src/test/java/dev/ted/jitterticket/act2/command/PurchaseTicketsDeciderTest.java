package dev.ted.jitterticket.act2.command;

import dev.ted.jitterticket.act2.event.TicketsSold;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PurchaseTicketsDeciderTest {

    @Test
    void ticketPriceKnown_GeneratesTicketsSoldAtThatPrice() {
        // GIVEN: tickets for ct-1 cost 45
        TicketPriceSnapshot snapshot = new TicketPriceSnapshot(45);
        PurchaseTicketsCommand command = new PurchaseTicketsCommand("ct-1", "cu-1", "t-1", 6);

        // WHEN: cu-1 buys 6
        CommandResult result = PurchaseTicketsDecider.decide(command, snapshot);

        // THEN: 6 x 45 = 270
        assertThat(result.event())
                .isEqualTo(new TicketsSold("ct-1", "cu-1", "t-1", 6, 270));
    }
}
