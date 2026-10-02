package dev.ted.jitterticket.act2.command;

import dev.ted.jitterticket.act2.event.ConcertRescheduled;

public class RescheduleConcert implements Command<RescheduleConcertRequest, NoDecisionContext> {

    @Override
    public CommandResult execute(RescheduleConcertRequest request,
                                 NoDecisionContext noDecisionContext) {
        return new CommandResult(new ConcertRescheduled(
                request.concertId(),
                request.newShowDateTime(),
                request.newDoorsTime()));
    }
}
