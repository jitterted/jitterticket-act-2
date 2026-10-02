package dev.ted.jitterticket.act2.event;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record ConcertRescheduled(String concertId,
                                 LocalDateTime newShowDateTime,
                                 LocalTime newDoorsTime) implements Event {
}
