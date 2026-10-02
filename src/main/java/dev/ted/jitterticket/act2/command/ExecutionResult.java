package dev.ted.jitterticket.act2.command;

import dev.ted.jitterticket.act2.SequencedEvent;

/**
 * What happened when the CommandExecutor executed a command.
 */
public sealed interface ExecutionResult {

    /**
     * The command's event is now in the log, at this sequence number.
     */
    record Appended(SequencedEvent sequencedEvent) implements ExecutionResult {
    }

    /**
     * The snapshot was built from a read model as of expectedLastSequenceNumber,
     * but the log had moved past it, so nothing was appended.
     */
    record Conflict(long expectedLastSequenceNumber,
                    long actualLastSequenceNumber) implements ExecutionResult {
    }
}
