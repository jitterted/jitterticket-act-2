package dev.ted.jitterticket.act2.command;

import dev.ted.jitterticket.act2.event.ConcertRescheduled;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RescheduleConcertDeciderTest {

    @Test
    void newDateFree_GeneratesConcertRescheduled() {
        // GIVEN: ct-1 is on 2026-11-14, and 2026-11-21 is free
        BookedDatesSnapshot snapshot = new BookedDatesSnapshot(Map.of(
                "ct-1", LocalDate.of(2026, 11, 14)));
        RescheduleConcertCommand command = new RescheduleConcertCommand(
                "ct-1",
                LocalDateTime.of(2026, 11, 21, 19, 30), LocalTime.of(18, 30));

        // WHEN
        CommandResult result = RescheduleConcertDecider.decide(command, snapshot);

        // THEN
        assertThat(result.event())
                .isEqualTo(new ConcertRescheduled(
                        "ct-1",
                        LocalDateTime.of(2026, 11, 21, 19, 30), LocalTime.of(18, 30)));
    }

    @Test
    void newDateIsSameDayAsItsOwn_GeneratesConcertRescheduled() {
        // GIVEN: ct-1 itself holds 2026-11-14
        BookedDatesSnapshot snapshot = new BookedDatesSnapshot(Map.of(
                "ct-1", LocalDate.of(2026, 11, 14)));
        RescheduleConcertCommand command = new RescheduleConcertCommand(
                "ct-1",
                LocalDateTime.of(2026, 11, 14, 21, 0), LocalTime.of(20, 0));

        // WHEN
        CommandResult result = RescheduleConcertDecider.decide(command, snapshot);

        // THEN
        assertThat(result.event())
                .isEqualTo(new ConcertRescheduled(
                        "ct-1",
                        LocalDateTime.of(2026, 11, 14, 21, 0), LocalTime.of(20, 0)));
    }

    @Test
    void newDateBookedByAnotherConcert_ThrowsDateAlreadyBookedException() {
        // GIVEN: ct-2 holds 2026-11-21
        LocalDate bookedDate = LocalDate.of(2026, 11, 21);
        BookedDatesSnapshot snapshot = new BookedDatesSnapshot(Map.of(
                "ct-1", LocalDate.of(2026, 11, 14),
                "ct-2", bookedDate));
        RescheduleConcertCommand command = new RescheduleConcertCommand(
                "ct-1",
                LocalDateTime.of(2026, 11, 21, 19, 30), LocalTime.of(18, 30));

        // WHEN / THEN
        assertThatThrownBy(() -> RescheduleConcertDecider.decide(command, snapshot))
                .isInstanceOf(DateAlreadyBookedException.class)
                .hasMessageContaining(bookedDate.toString());
    }
}
