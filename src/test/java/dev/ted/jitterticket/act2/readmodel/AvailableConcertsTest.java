package dev.ted.jitterticket.act2.readmodel;

import dev.ted.jitterticket.act2.EventLog;
import dev.ted.jitterticket.act2.ScriptedRun;
import dev.ted.jitterticket.act2.event.Event;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static dev.ted.jitterticket.act2.EventBuilders.*;
import static org.assertj.core.api.Assertions.assertThat;

class AvailableConcertsTest {

    @Test
    void noEvents_NoConcerts() {
        AvailableConcerts availableConcerts = new AvailableConcerts();

        assertThat(availableConcerts.availableConcerts())
                .isEmpty();
    }

    @Test
    void concertScheduled_ShowsArtistDateDoorsAndPrice() {
        // GIVEN
        AvailableConcerts availableConcerts = given(
                concertScheduled("ct-1")
                        .withArtist("Cosmic Drift")
                        .withShowDateTime(LocalDateTime.of(2026, 11, 14, 20, 0))
                        .withDoorsTime(LocalTime.of(19, 0))
                        .withTicketPrice(45));

        // THEN
        assertThat(availableConcerts.availableConcerts())
                .containsExactly(new AvailableConcerts.Concert(
                        "ct-1", "Cosmic Drift",
                        LocalDateTime.of(2026, 11, 14, 20, 0), LocalTime.of(19, 0),
                        45));
    }

    @Test
    void concertRescheduled_ShowsNewDateAndDoorsTime() {
        AvailableConcerts availableConcerts = given(
                concertScheduled("ct-1")
                        .withShowDateTime(LocalDateTime.of(2026, 11, 14, 20, 0))
                        .withDoorsTime(LocalTime.of(19, 0)),
                concertRescheduled("ct-1")
                        .withNewShowDateTime(LocalDateTime.of(2026, 11, 21, 19, 30))
                        .withNewDoorsTime(LocalTime.of(18, 30)));

        AvailableConcerts.Concert concert = availableConcerts.findConcert("ct-1").orElseThrow();
        assertThat(concert.showDateTime())
                .isEqualTo(LocalDateTime.of(2026, 11, 21, 19, 30));
        assertThat(concert.doorsTime())
                .isEqualTo(LocalTime.of(18, 30));
    }

    @Test
    void someTicketsSold_ConcertStillAvailable() {
        AvailableConcerts availableConcerts = given(
                concertScheduled("ct-1").withCapacity(20),
                ticketsSold("ct-1").withQuantity(19));

        assertThat(availableConcerts.availableConcerts())
                .extracting(AvailableConcerts.Concert::concertId)
                .containsExactly("ct-1");
    }

    @Test
    void lastTicketsSold_ConcertNoLongerAvailable() {
        AvailableConcerts availableConcerts = given(
                concertScheduled("ct-1").withCapacity(20),
                ticketsSold("ct-1").withQuantity(12),
                ticketsSold("ct-1").withQuantity(8));

        assertThat(availableConcerts.availableConcerts())
                .isEmpty();
        assertThat(availableConcerts.findConcert("ct-1"))
                .isEmpty();
    }

    @Test
    void twoConcerts_ListedInShowDateOrder() {
        AvailableConcerts availableConcerts = given(
                concertScheduled("ct-later").withShowDateTime(LocalDateTime.of(2026, 12, 5, 21, 0)),
                concertScheduled("ct-sooner").withShowDateTime(LocalDateTime.of(2026, 11, 14, 20, 0)));

        assertThat(availableConcerts.availableConcerts())
                .extracting(AvailableConcerts.Concert::concertId)
                .containsExactly("ct-sooner", "ct-later");
    }

    // The board's checkpoints (facilitator's guide, 3.7)

    @Test
    void scriptedRunUpToSeq7_BothConcertsAvailable() {
        AvailableConcerts availableConcerts = given(ScriptedRun.upToSeq(7));

        assertThat(availableConcerts.availableConcerts())
                .extracting(AvailableConcerts.Concert::concertId)
                .containsExactly("ct-1", "ct-2");
    }

    @Test
    void scriptedRunUpToSeq11_SoldOutLyricalStormIsNoLongerAvailable() {
        AvailableConcerts availableConcerts = given(ScriptedRun.upToSeq(11));

        assertThat(availableConcerts.availableConcerts())
                .extracting(AvailableConcerts.Concert::concertId)
                .containsExactly("ct-1");
    }

    private static AvailableConcerts given(EventBuilder... events) {
        return given(events(events));
    }

    private static AvailableConcerts given(List<Event> events) {
        AvailableConcerts availableConcerts = new AvailableConcerts();
        EventLog eventLog = new EventLog();
        eventLog.register(availableConcerts);
        eventLog.appendAll(events);
        return availableConcerts;
    }
}
