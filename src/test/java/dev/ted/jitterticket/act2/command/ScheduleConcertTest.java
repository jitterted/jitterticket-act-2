package dev.ted.jitterticket.act2.command;

import dev.ted.jitterticket.act2.event.ConcertScheduled;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduleConcertTest {

    @Test
    void scheduleConcert_GeneratesConcertScheduled() {
        // GIVEN: no events
        ScheduleConcert scheduleConcert = new ScheduleConcert("ct-1");

        // WHEN
        CommandResult result = scheduleConcert.execute(new ScheduleConcertRequest(
                "Cosmic Drift",
                LocalDateTime.of(2026, 11, 14, 20, 0), LocalTime.of(19, 0),
                200, 45, 6),
                NoDecisionContext.NONE);

        // THEN
        assertThat(result.event())
                .isEqualTo(new ConcertScheduled(
                        "ct-1", "Cosmic Drift",
                        LocalDateTime.of(2026, 11, 14, 20, 0), LocalTime.of(19, 0),
                        200, 45, 6));
    }
}
