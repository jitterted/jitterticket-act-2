package dev.ted.jitterticket.act2.command;

/**
 * Decides what happens: given a request (what the user asked for)
 * and a decision context (what the command needs to know to decide),
 * it produces a result. Commands never touch the event log themselves:
 * the CommandExecutor does that.
 */
public interface Command<REQUEST, CONTEXT> {

    CommandResult execute(REQUEST request, CONTEXT context);
}
