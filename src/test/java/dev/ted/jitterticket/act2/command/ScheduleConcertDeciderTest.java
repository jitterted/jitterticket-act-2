package dev.ted.jitterticket.act2.command;

import dev.ted.jitterticket.act2.event.ConcertScheduled;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScheduleConcertDeciderTest {

    @Test
    void dateFree_GeneratesConcertScheduled() {
        // GIVEN: date is not booked
        BookedDatesDecisionContext decisionContext = new BookedDatesDecisionContext(Map.of());
        ScheduleConcertCommand command = new ScheduleConcertCommand(
                "ct-1",
                "Cosmic Drift",
                LocalDateTime.of(2026, 11, 14, 20, 0), LocalTime.of(19, 0),
                200, 45, 6);

        // WHEN
        CommandResult result = ScheduleConcertDecider.decide(command, decisionContext);

        // THEN
        assertThat(result.event())
                .isEqualTo(new ConcertScheduled(
                        "ct-1", "Cosmic Drift",
                        LocalDateTime.of(2026, 11, 14, 20, 0), LocalTime.of(19, 0),
                        200, 45, 6));
    }

    @Test
    void dateAlreadyBooked_ThrowsDateAlreadyBookedException() {
        // GIVEN: 2026-11-14 is already booked
        LocalDate bookedDate = LocalDate.of(2026, 11, 14);
        BookedDatesDecisionContext decisionContext = new BookedDatesDecisionContext(Map.of("ct-2", bookedDate));
        ScheduleConcertCommand command = new ScheduleConcertCommand(
                "ct-1",
                "Cosmic Drift",
                LocalDateTime.of(2026, 11, 14, 20, 0), LocalTime.of(19, 0),
                200, 45, 6);

        // WHEN / THEN
        assertThatThrownBy(() -> ScheduleConcertDecider.decide(command, decisionContext))
                .isInstanceOf(DateAlreadyBookedException.class)
                .hasMessageContaining(bookedDate.toString());
    }
}
