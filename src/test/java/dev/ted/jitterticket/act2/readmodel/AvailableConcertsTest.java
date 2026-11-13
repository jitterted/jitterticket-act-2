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

        assertThat(availableConcerts.findConcert("ct-1"))
                .as("No Concert with id was found: ct-1")
                .isPresent()
                .map(AvailableConcerts.Concert::showDateTime)
                .as("Should have the new show date-time")
                .hasValue(LocalDateTime.of(2026, 11, 21, 19, 30));
        assertThat(availableConcerts.findConcert("ct-1"))
                .as("No Concert with id was found: ct-1")
                .isPresent()
                .map(AvailableConcerts.Concert::doorsTime)
                .as("Should have the new doors time")
                .hasValue(LocalTime.of(18, 30));
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

    @Test
    void concertAvailable_TicketPriceForReturnsItsPrice() {
        AvailableConcerts availableConcerts = given(
                concertScheduled("ct-1").withCapacity(20).withTicketPrice(45));

        assertThat(availableConcerts.ticketPriceFor("ct-1"))
                .contains(new dev.ted.jitterticket.act2.command.TicketPriceDecisionContext(45));
    }

    @Test
    void concertSoldOutOrUnknown_TicketPriceForIsEmpty() {
        AvailableConcerts availableConcerts = given(
                concertScheduled("ct-1").withCapacity(5),
                ticketsSold("ct-1").withQuantity(5));

        assertThat(availableConcerts.ticketPriceFor("ct-1"))
                .isEmpty();
        assertThat(availableConcerts.ticketPriceFor("ct-unknown"))
                .isEmpty();
    }

    @Test
    void upcomingConcerts_ExcludesConcertsOnEarlierDays() {
        AvailableConcerts availableConcerts = given(
                concertScheduled("ct-yesterday").withShowDateTime(LocalDateTime.of(2026, 11, 13, 20, 0)),
                concertScheduled("ct-tomorrow").withShowDateTime(LocalDateTime.of(2026, 11, 15, 20, 0)));

        LocalDateTime now = LocalDateTime.of(2026, 11, 14, 12, 0);

        assertThat(availableConcerts.upcomingConcerts(now))
                .extracting(AvailableConcerts.Concert::concertId)
                .containsExactly("ct-tomorrow");
    }

    @Test
    void upcomingConcerts_IncludesConcertLaterTheSameDay() {
        AvailableConcerts availableConcerts = given(
                concertScheduled("ct-tonight").withShowDateTime(LocalDateTime.of(2026, 11, 14, 20, 0)));

        LocalDateTime now = LocalDateTime.of(2026, 11, 14, 9, 0);

        assertThat(availableConcerts.upcomingConcerts(now))
                .extracting(AvailableConcerts.Concert::concertId)
                .containsExactly("ct-tonight");
    }

    @Test
    void upcomingConcerts_IncludesConcertEarlierTheSameDay() {
        AvailableConcerts availableConcerts = given(
                concertScheduled("ct-matinee").withShowDateTime(LocalDateTime.of(2026, 11, 14, 14, 0)));

        LocalDateTime now = LocalDateTime.of(2026, 11, 14, 23, 0);

        assertThat(availableConcerts.upcomingConcerts(now))
                .as("Concert on the same day should still be listed, even after its show time")
                .extracting(AvailableConcerts.Concert::concertId)
                .containsExactly("ct-matinee");
    }

    @Test
    void upcomingConcerts_ExcludesSoldOutConcerts() {
        AvailableConcerts availableConcerts = given(
                concertScheduled("ct-1").withCapacity(5)
                                        .withShowDateTime(LocalDateTime.of(2026, 12, 5, 21, 0)),
                ticketsSold("ct-1").withQuantity(5));

        LocalDateTime now = LocalDateTime.of(2026, 11, 14, 12, 0);

        assertThat(availableConcerts.upcomingConcerts(now))
                .isEmpty();
    }

    @Test
    void upcomingConcerts_ListedInShowDateOrder() {
        AvailableConcerts availableConcerts = given(
                concertScheduled("ct-later").withShowDateTime(LocalDateTime.of(2026, 12, 5, 21, 0)),
                concertScheduled("ct-sooner").withShowDateTime(LocalDateTime.of(2026, 11, 20, 20, 0)));

        LocalDateTime now = LocalDateTime.of(2026, 11, 14, 12, 0);

        assertThat(availableConcerts.upcomingConcerts(now))
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
