package dev.ted.jitterticket.act2.readmodel;

import dev.ted.jitterticket.act2.EventLog;
import dev.ted.jitterticket.act2.ScriptedRun;
import dev.ted.jitterticket.act2.event.Event;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static dev.ted.jitterticket.act2.EventBuilders.*;
import static org.assertj.core.api.Assertions.assertThat;

class BookedDatesTest {

    @Test
    void concertScheduled_DateIsBooked() {
        BookedDates bookedDates = given(
                concertScheduled("ct-1")
                        .withArtist("Cosmic Drift")
                        .withShowDateTime(LocalDateTime.of(2026, 11, 14, 20, 0)));

        assertThat(bookedDates.bookedDates())
                .containsExactly(new BookedDates.BookedDate(
                        LocalDate.of(2026, 11, 14), "Cosmic Drift"));
    }

    @Test
    void concertRescheduled_OldDateIsFreedNewDateIsBooked() {
        BookedDates bookedDates = given(
                concertScheduled("ct-1")
                        .withArtist("Cosmic Drift")
                        .withShowDateTime(LocalDateTime.of(2026, 11, 14, 20, 0)),
                concertRescheduled("ct-1")
                        .withNewShowDateTime(LocalDateTime.of(2026, 11, 21, 19, 30)));

        assertThat(bookedDates.bookedDates())
                .containsExactly(new BookedDates.BookedDate(
                        LocalDate.of(2026, 11, 21), "Cosmic Drift"));
    }

    @Test
    void customerAndTicketEvents_DontBookAnything() {
        BookedDates bookedDates = given(
                customerRegistered("cu-1"),
                ticketsSold("ct-1")); // invalid test setup

        assertThat(bookedDates.bookedDates())
                .isEmpty();
    }

    @Test
    void noEvents_LastSequenceNumberIsZero() {
        BookedDates bookedDates = new BookedDates();

        assertThat(bookedDates.lastSequenceNumber())
                .isZero();
    }

    @Test
    void lastEventIgnored_LastSequenceNumberStillMovesToIt() {
        BookedDates bookedDates = given(
                concertScheduled("ct-1"),
                customerRegistered("cu-1"));

        assertThat(bookedDates.lastSequenceNumber())
                .isEqualTo(2);
    }

    @Test
    void concertsScheduled_DecisionContextHoldsEachConcertsDate() {
        BookedDates bookedDates = given(
                concertScheduled("ct-1")
                        .withShowDateTime(LocalDateTime.of(2026, 11, 14, 20, 0)),
                concertScheduled("ct-2")
                        .withShowDateTime(LocalDateTime.of(2026, 12, 5, 21, 0)));

        assertThat(bookedDates.decisionContext().bookedDates())
                .containsExactlyInAnyOrder(
                        LocalDate.of(2026, 11, 14),
                        LocalDate.of(2026, 12, 5));
    }

    // The board's checkpoint (facilitator's guide, 3.7)

    @Test
    void scriptedRunUpToSeq8_TwoDatesBookedAndLastSequenceNumberIs8() {
        BookedDates bookedDates = given(ScriptedRun.upToSeq(8));

        assertThat(bookedDates.bookedDates())
                .containsExactly(
                        new BookedDates.BookedDate(LocalDate.of(2026, 11, 21), "Cosmic Drift"),
                        new BookedDates.BookedDate(LocalDate.of(2026, 12, 5), "Lyrical Storm"));
        assertThat(bookedDates.lastSequenceNumber())
                .isEqualTo(8);
    }

    private static BookedDates given(EventBuilder... events) {
        return given(events(events));
    }

    private static BookedDates given(List<Event> events) {
        BookedDates bookedDates = new BookedDates();
        EventLog eventLog = new EventLog();
        eventLog.register(bookedDates);
        eventLog.appendAll(events);
        return bookedDates;
    }
}
