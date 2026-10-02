package dev.ted.jitterticket.act2.command;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record RescheduleConcertCommand(
        String concertId,
        LocalDateTime newShowDateTime,
        LocalTime newDoorsTime
) {
}
