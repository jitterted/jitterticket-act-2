package dev.ted.jitterticket.act2.readmodel;

import dev.ted.jitterticket.act2.SequencedEvent;

/**
 * A read model is a projection: it folds events, in order, into whatever
 * shape its UI or decision-maker needs.
 * <p>
 * In this codebase there are no subscriptions: the EventLog hands
 * EVERY event to EVERY read model. Each read model decides what to ignore,
 * and keeps track of the last sequence number it has handled (its LSN).
 */
public interface ReadModel {

    void apply(SequencedEvent sequencedEvent);

    long lastSequenceNumber();
}
