package dev.ted.jitterticket.act2;

import dev.ted.jitterticket.act2.event.Event;

/**
 * An event as stored in the log: its slot in the log, plus the event itself.
 */
public record SequencedEvent(long sequenceNumber, Event event) {
}
