package dev.ted.jitterticket.act2.command;

import dev.ted.jitterticket.act2.EventLog;
import dev.ted.jitterticket.act2.event.TicketsSold;
import dev.ted.jitterticket.act2.readmodel.AvailableConcerts;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static dev.ted.jitterticket.act2.EventBuilders.*;
import static org.assertj.core.api.Assertions.assertThat;

class PurchaseTicketsTest {

    @Test
    void purchaseTickets_GeneratesTicketsSoldAtConcertTicketPrice() {
        // GIVEN: tickets for ct-1 cost 45
        AvailableConcerts availableConcerts = given(
                concertScheduled("ct-1").withTicketPrice(45));
        PurchaseTickets purchaseTickets = new PurchaseTickets("t-1");

        // WHEN: cu-1 buys 6
        CommandResult result = purchaseTickets.execute(
                new PurchaseTicketsRequest("ct-1", "cu-1", 6),
                availableConcerts);

        // THEN: 6 x 45 = 270
        assertThat(result.event())
                .isEqualTo(new TicketsSold("ct-1", "cu-1", "t-1", 6, 270));
    }

    @Disabled("Demo: remove @Disabled to show what a stale read model does to a command")
    @Test
    void readModelBehindTheLog_StillSellsTicketsForJustScheduledConcert() {
        // GIVEN: the log has ct-1 scheduled, at 45 a ticket...
        EventLog eventLog = new EventLog();
        eventLog.append(concertScheduled("ct-1").withTicketPrice(45).build());
        // ...but Available Concerts hasn't applied it yet: it's LAGGING
        AvailableConcerts availableConcerts = new AvailableConcerts();
        PurchaseTickets purchaseTickets = new PurchaseTickets("t-1");

        // WHEN
        CommandResult result = purchaseTickets.execute(
                new PurchaseTicketsRequest("ct-1", "cu-1", 6),
                availableConcerts);

        // THEN
        assertThat(result.event())
                .isEqualTo(new TicketsSold("ct-1", "cu-1", "t-1", 6, 270));
    }

    private static AvailableConcerts given(EventBuilder... events) {
        AvailableConcerts availableConcerts = new AvailableConcerts();
        EventLog eventLog = new EventLog();
        eventLog.register(availableConcerts);
        eventLog.appendAll(events(events));
        return availableConcerts;
    }
}
