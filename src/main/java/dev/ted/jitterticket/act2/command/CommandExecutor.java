package dev.ted.jitterticket.act2.command;

import dev.ted.jitterticket.act2.EventLog;

/**
 * Runs a command, then asks the event log to append the event it generated.
 * <p>
 * expectedLastSequenceNumber is the last sequence number the screen was built from.
 * If the log has moved past it, the append is refused.
 */
public class CommandExecutor {

    private final EventLog eventLog;

    public CommandExecutor(EventLog eventLog) {
        this.eventLog = eventLog;
    }

    public <REQUEST> ExecutionResult execute(Command<REQUEST, NoDecisionContext> command,
                                             REQUEST request,
                                             long expectedLastSequenceNumber) {
        return execute(command, request, NoDecisionContext.NONE, expectedLastSequenceNumber);
    }

    public <REQUEST, CONTEXT> ExecutionResult execute(Command<REQUEST, CONTEXT> command,
                                                      REQUEST request,
                                                      CONTEXT context,
                                                      long expectedLastSequenceNumber) {
        CommandResult result = command.execute(request, context);
        return eventLog.append(result.event(), expectedLastSequenceNumber)
                       .<ExecutionResult>map(ExecutionResult.Appended::new)
                       .orElseGet(() -> new ExecutionResult.Conflict(
                               expectedLastSequenceNumber,
                               eventLog.lastSequenceNumber()));
    }
}
