package dev.ted.jitterticket.act2.readmodel;

import dev.ted.jitterticket.act2.EventBuilders;
import dev.ted.jitterticket.act2.EventLog;
import dev.ted.jitterticket.act2.event.ConcertScheduled;
import dev.ted.jitterticket.act2.event.Event;
import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.ted.jitterticket.act2.EventBuilders.*;

import static org.assertj.core.api.Assertions.assertThat;

class ConcertSalesTest {

    @Test
    void noConcertsScheduled_SalesIsEmpty() {
        ConcertSales concertSales = new ConcertSales();

        assertThat(concertSales.sales())
                .isEmpty();
        assertThat(concertSales.lastSequenceNumber())
                .isZero();
    }

    @Test
    void concertScheduled_SalesIsZeroForThatConcert() {
        EventLog eventLog = new EventLog();
        ConcertSales concertSales = new ConcertSales();
        eventLog.register(concertSales);
        List<Event> events = events(concertScheduled("conc:ab-12-cd")
                .withArtist("The Beatles"));
        eventLog.appendAll(events);

        assertThat(concertSales.lastSequenceNumber())
                .isEqualTo(1);
        assertThat(concertSales.sales())
                .containsExactly(
                        new SalesInfo("conc:ab-12-cd",
                        "The Beatles",
                        0, 0));
    }

    @Test
    void ticketsSold_Sales() {
        EventLog eventLog = new EventLog();
        ConcertSales concertSales = new ConcertSales();
        eventLog.register(concertSales);
        String concertId = "conc:ab-34-cd";
        List<Event> events = events(
                customerRegistered("cust:123"),
                concertScheduled(concertId)
                        .withTicketPrice(250)
                        .withArtist("Rolling Stones"),
                ticketsSold(concertId)
                        .withQuantity(3)
                        .withTotalCost(3 * 250)
                        .toCustomer("cust:123")
        );
        eventLog.appendAll(events);

        assertThat(concertSales.sales())
                .containsExactly(new SalesInfo(
                        concertId,
                        "Rolling Stones",
                        3,
                        3 * 250
                ));
    }

    @Test
    void multipleTicketsSoldForSpecificConcert() {
        
    }
}
