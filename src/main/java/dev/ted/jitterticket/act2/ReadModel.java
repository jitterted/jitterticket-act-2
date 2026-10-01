package dev.ted.jitterticket.act2;

/**
 * A read model is a projection: it folds events, in order, into whatever
 * shape its UI or decision-maker needs.
 * <p>
 * In this codebase there are no subscriptions: the EventLog hands
 * EVERY event to EVERY read model. Each read model decides what to ignore.
 */
public interface ReadModel {

    void apply(Event event);
}
