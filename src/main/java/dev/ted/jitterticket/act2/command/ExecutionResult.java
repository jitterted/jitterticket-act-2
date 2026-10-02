package dev.ted.jitterticket.act2.command;

import dev.ted.jitterticket.act2.SequencedEvent;

/**
 * What happened when the CommandExecutor ran a command.
 */
public sealed interface ExecutionResult {

    /**
     * The command's event is now in the log, at this sequence number.
     */
    record Appended(SequencedEvent sequencedEvent) implements ExecutionResult {
    }

    /**
     * The screen was built from the log as of expectedLastSequenceNumber,
     * but the log has moved on since, so nothing was appended.
     */
    record Conflict(long expectedLastSequenceNumber,
                    long actualLastSequenceNumber) implements ExecutionResult {
    }
}
