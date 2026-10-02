package dev.ted.jitterticket.act2.command;

import dev.ted.jitterticket.act2.EventLog;
import dev.ted.jitterticket.act2.ScriptedRun;
import dev.ted.jitterticket.act2.event.Event;
import dev.ted.jitterticket.act2.readmodel.BookedDates;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static dev.ted.jitterticket.act2.EventBuilders.*;
import static org.assertj.core.api.Assertions.assertThat;

class LiveBookedDatesQueryTest {

    @Test
    void noEvents_SnapshotIsEmptyAndLastSequenceNumberIsZero() {
        EventLog eventLog = new EventLog();
        LiveBookedDatesQuery query = new LiveBookedDatesQuery(eventLog);

        assertThat(query.snapshot().bookedDates())
                .isEmpty();
        assertThat(query.lastSequenceNumber())
                .isZero();
    }

    @Test
    void concertScheduled_DateIsBooked() {
        EventLog eventLog = given(
                concertScheduled("ct-1")
                        .withShowDateTime(LocalDateTime.of(2026, 11, 14, 20, 0)));
        LiveBookedDatesQuery query = new LiveBookedDatesQuery(eventLog);

        assertThat(query.snapshot().bookedDates())
                .containsExactly(LocalDate.of(2026, 11, 14));
        assertThat(query.lastSequenceNumber())
                .isEqualTo(1);
    }

    @Test
    void concertRescheduled_OldDateIsFreedNewDateIsBooked() {
        EventLog eventLog = given(
                concertScheduled("ct-1")
                        .withShowDateTime(LocalDateTime.of(2026, 11, 14, 20, 0)),
                concertRescheduled("ct-1")
                        .withNewShowDateTime(LocalDateTime.of(2026, 11, 21, 19, 30)));
        LiveBookedDatesQuery query = new LiveBookedDatesQuery(eventLog);

        assertThat(query.snapshot().bookedDates())
                .containsExactly(LocalDate.of(2026, 11, 21));
        assertThat(query.lastSequenceNumber())
                .isEqualTo(2);
    }

    @Test
    void concertRescheduledForUnknownConcert_NothingIsBooked() {
        EventLog eventLog = given(
                concertRescheduled("ct-99")
                        .withNewShowDateTime(LocalDateTime.of(2026, 11, 21, 19, 30)));
        LiveBookedDatesQuery query = new LiveBookedDatesQuery(eventLog);

        assertThat(query.snapshot().bookedDates())
                .isEmpty();
    }

    @Test
    void customerAndTicketEvents_DontBookAnythingButLastSequenceNumberMoves() {
        EventLog eventLog = given(
                concertScheduled("ct-1")
                        .withShowDateTime(LocalDateTime.of(2026, 11, 14, 20, 0)),
                customerRegistered("cu-1"),
                ticketsSold("ct-1"));
        LiveBookedDatesQuery query = new LiveBookedDatesQuery(eventLog);

        assertThat(query.snapshot().bookedDates())
                .containsExactly(LocalDate.of(2026, 11, 14));
        assertThat(query.lastSequenceNumber())
                .isEqualTo(3);
    }

    @Test
    void scriptedRunUpToSeq8_TwoDatesBookedAndLastSequenceNumberIs8() {
        EventLog eventLog = given(ScriptedRun.upToSeq(8));
        LiveBookedDatesQuery query = new LiveBookedDatesQuery(eventLog);

        assertThat(query.snapshot().bookedDates())
                .containsExactlyInAnyOrder(
                        LocalDate.of(2026, 11, 21),
                        LocalDate.of(2026, 12, 5));
        assertThat(query.lastSequenceNumber())
                .isEqualTo(8);
    }

    // The live query and the BookedDates read model must agree on the same log

    @Test
    void concertRescheduledForUnknownConcert_AgreesWithBookedDatesReadModel() {
        assertLiveQueryAgreesWithReadModel(events(
                concertScheduled("ct-1")
                        .withShowDateTime(LocalDateTime.of(2026, 11, 14, 20, 0)),
                concertRescheduled("ct-99")
                        .withNewShowDateTime(LocalDateTime.of(2026, 11, 21, 19, 30))));
    }

    @Test
    void scriptedRunUpToSeq15_AgreesWithBookedDatesReadModel() {
        assertLiveQueryAgreesWithReadModel(ScriptedRun.upToSeq(15));
    }

    // Why the live query exists: a read model can be behind the log, the live query can't

    @Test
    void readModelBehindTheLog_ReadModelMissesTheLatestBookingButLiveQuerySeesIt() {
        // GIVEN: the log has two concerts scheduled...
        EventLog eventLog = given(
                concertScheduled("ct-1")
                        .withShowDateTime(LocalDateTime.of(2026, 11, 14, 20, 0)),
                concertScheduled("ct-2")
                        .withShowDateTime(LocalDateTime.of(2026, 12, 5, 21, 0)));
        // ...but Booked Dates has only caught up to the first
        BookedDates bookedDates = new BookedDates();
        bookedDates.apply(eventLog.events().getFirst());
        LiveBookedDatesQuery query = new LiveBookedDatesQuery(eventLog);

        // THEN: the read model's snapshot is stale...
        assertThat(bookedDates.snapshot().isBooked(LocalDate.of(2026, 12, 5)))
                .isFalse();
        assertThat(bookedDates.lastSequenceNumber())
                .isEqualTo(1);
        // ...while the live query is as of the latest event
        assertThat(query.snapshot().isBooked(LocalDate.of(2026, 12, 5)))
                .isTrue();
        assertThat(query.lastSequenceNumber())
                .isEqualTo(2);
    }

    private static void assertLiveQueryAgreesWithReadModel(List<Event> events) {
        EventLog eventLog = new EventLog();
        BookedDates bookedDates = new BookedDates();
        eventLog.register(bookedDates);
        eventLog.appendAll(events);
        LiveBookedDatesQuery query = new LiveBookedDatesQuery(eventLog);

        assertThat(query.snapshot())
                .isEqualTo(bookedDates.snapshot());
        assertThat(query.lastSequenceNumber())
                .isEqualTo(bookedDates.lastSequenceNumber());
    }

    private static EventLog given(EventBuilder... events) {
        return given(events(events));
    }

    private static EventLog given(List<Event> events) {
        EventLog eventLog = new EventLog();
        eventLog.appendAll(events);
        return eventLog;
    }
}
