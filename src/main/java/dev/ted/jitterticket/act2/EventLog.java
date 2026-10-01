package dev.ted.jitterticket.act2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An append-only, ordered log of events. Sequence numbers start at 1.
 * <p>
 * Every registered read model is handed every event as it is appended:
 * this is "everyone subscribes to EVERYTHING" from Act 2, Round 1.
 */
public class EventLog {

    private final List<Event> events = new ArrayList<>();
    private final List<ReadModel> readModels = new ArrayList<>();

    public void register(ReadModel readModel) {
        readModels.add(readModel);
    }

    public long append(Event event) {
        events.add(event);
        readModels.forEach(readModel -> readModel.apply(event));
        return events.size();
    }

    public void appendAll(List<Event> eventsToAppend) {
        eventsToAppend.forEach(this::append);
    }

    public List<Event> events() {
        return Collections.unmodifiableList(events);
    }
}
