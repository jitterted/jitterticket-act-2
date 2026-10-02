package dev.ted.jitterticket.act2;

import dev.ted.jitterticket.act2.event.Event;
import org.junit.jupiter.api.Test;

import static dev.ted.jitterticket.act2.EventBuilders.*;
import static org.assertj.core.api.Assertions.assertThat;

class EventLogTest {

    private final Event concertScheduled = concertScheduled("ct-1").build();
    private final Event customerRegistered = customerRegistered("cu-1").build();
    private final Event ticketsSold = ticketsSold("ct-1").build();

    @Test
    void eventsAppended_SequenceNumbersStartAt1() {
        EventLog eventLog = new EventLog();

        eventLog.append(concertScheduled);
        eventLog.append(customerRegistered);

        assertThat(eventLog.events())
                .containsExactly(
                        new SequencedEvent(1, concertScheduled),
                        new SequencedEvent(2, customerRegistered));
    }

    @Test
    void expectedLastSequenceNumberMatches_EventAppended() {
        EventLog eventLog = new EventLog();
        eventLog.append(concertScheduled);

        assertThat(eventLog.append(customerRegistered, 1))
                .contains(new SequencedEvent(2, customerRegistered));
    }

    @Test
    void logHasMovedOn_EventNotAppended() {
        EventLog eventLog = new EventLog();
        eventLog.append(concertScheduled);
        eventLog.append(customerRegistered);

        assertThat(eventLog.append(ticketsSold, 1))
                .isEmpty();
        assertThat(eventLog.lastSequenceNumber())
                .isEqualTo(2);
    }
}
