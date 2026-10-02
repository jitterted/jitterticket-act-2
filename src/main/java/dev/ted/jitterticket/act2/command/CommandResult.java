package dev.ted.jitterticket.act2.command;

import dev.ted.jitterticket.act2.event.Event;

/**
 * What a decider decided: the one event the command generates.
 * A decider that rejects a command throws instead (for example, DateAlreadyBookedException).
 */
public record CommandResult(Event event) {
}
