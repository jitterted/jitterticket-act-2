package dev.ted.jitterticket.act2;

import dev.ted.jitterticket.act2.event.Event;
import dev.ted.jitterticket.act2.readmodel.ReadModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An append-only, ordered log of events. The log assigns each event
 * its sequence number as it is appended, starting at 1.
 * <p>
 * Every registered read model is handed every event as it is appended:
 * this is "everyone subscribes to EVERYTHING".
 */
public class EventLog {

    private final AtomicInteger nextSequenceNumber = new AtomicInteger(1);
    private final List<SequencedEvent> sequencedEvents = new ArrayList<>();
    private final List<ReadModel> readModels = new ArrayList<>();

    public void register(ReadModel readModel) {
        readModels.add(readModel);
    }

    public SequencedEvent append(Event event) {
        SequencedEvent sequencedEvent = new SequencedEvent(nextSequenceNumber.getAndIncrement(), event);
        sequencedEvents.add(sequencedEvent);
        readModels.forEach(readModel -> readModel.apply(sequencedEvent));
        return sequencedEvent;
    }

    /**
     * Appends only if nothing has been appended since the caller last looked:
     * the log's last sequence number must still be the one the caller expects.
     * Empty if it isn't, and nothing is appended.
     */
    public Optional<SequencedEvent> append(Event event, long expectedLastSequenceNumber) {
        if (lastSequenceNumber() != expectedLastSequenceNumber) {
            return Optional.empty();
        }
        return Optional.of(append(event));
    }

    public void appendAll(List<Event> eventsToAppend) {
        eventsToAppend.forEach(this::append);
    }

    public long lastSequenceNumber() {
        return nextSequenceNumber.get() - 1;
    }

    public List<SequencedEvent> events() {
        return Collections.unmodifiableList(sequencedEvents);
    }
}
