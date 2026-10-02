package dev.ted.jitterticket.act2.command;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * The data a "Reschedule Concert" form would POST.
 */
public record RescheduleConcertRequest(String concertId,
                                       LocalDateTime newShowDateTime,
                                       LocalTime newDoorsTime) {
}
