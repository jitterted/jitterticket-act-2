package dev.ted.jitterticket.act2.command;

import dev.ted.jitterticket.act2.event.ConcertRescheduled;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class RescheduleConcertTest {

    @Test
    void rescheduleConcert_GeneratesConcertRescheduled() {
        // GIVEN: no events (this command doesn't look at anything yet)
        RescheduleConcert rescheduleConcert = new RescheduleConcert();

        // WHEN
        CommandResult result = rescheduleConcert.execute(new RescheduleConcertRequest(
                "ct-1",
                LocalDateTime.of(2026, 11, 21, 19, 30), LocalTime.of(18, 30)),
                NoDecisionContext.NONE);

        // THEN
        assertThat(result.event())
                .isEqualTo(new ConcertRescheduled(
                        "ct-1",
                        LocalDateTime.of(2026, 11, 21, 19, 30), LocalTime.of(18, 30)));
    }
}
