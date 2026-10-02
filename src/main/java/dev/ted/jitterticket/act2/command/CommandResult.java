package dev.ted.jitterticket.act2.command;

import dev.ted.jitterticket.act2.event.Event;

/**
 * What executing a command produced. For now, commands always succeed,
 * so a result always holds exactly one event.
 */
public record CommandResult(Event event) {
}
