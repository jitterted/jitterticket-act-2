package dev.ted.jitterticket.act2;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class AvailableConcertsTest {

    @Test
    void noEvents_NoConcerts() {
        AvailableConcerts availableConcerts = new AvailableConcerts();

        assertThat(availableConcerts.availableConcerts())
                .isEmpty();
    }

    @Test
    void concertScheduledThenRescheduled_ShowsNewDateAndDoorsTime() {
        // GIVEN
        AvailableConcerts availableConcerts = given(List.of(
                ScriptedRun.SEQ_1,
                ScriptedRun.SEQ_2));

        // THEN
        assertThat(availableConcerts.availableConcerts())
                .containsExactly(new AvailableConcerts.Concert(
                        "ct-1", "Cosmic Drift",
                        LocalDateTime.of(2026, 11, 21, 19, 30), LocalTime.of(18, 30),
                        45, 200));
    }

    @Test
    void ticketsSold_ReducesTicketsLeft() {
        AvailableConcerts availableConcerts = given(List.of(
                ScriptedRun.SEQ_1,
                ScriptedRun.SEQ_5));

        assertThat(availableConcerts.availableConcerts())
                .extracting(AvailableConcerts.Concert::ticketsLeft)
                .containsExactly(194);
    }

    @Test
    void scriptedRunUpToSeq7_TwoConcertsInShowDateOrder() {
        AvailableConcerts availableConcerts = given(ScriptedRun.upToSeq(7));

        assertThat(availableConcerts.availableConcerts())
                .extracting(AvailableConcerts.Concert::concertId,
                            AvailableConcerts.Concert::ticketsLeft)
                .containsExactly(
                        tuple("ct-1", 194),
                        tuple("ct-2", 12));
    }

    private static AvailableConcerts given(List<Event> events) {
        AvailableConcerts availableConcerts = new AvailableConcerts();
        EventLog eventLog = new EventLog();
        eventLog.register(availableConcerts);
        eventLog.appendAll(events);
        return availableConcerts;
    }
}
