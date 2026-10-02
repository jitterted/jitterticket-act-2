package dev.ted.jitterticket.act2.command;

import dev.ted.jitterticket.act2.event.ConcertScheduled;

public class ScheduleConcert implements Command<ScheduleConcertRequest, NoDecisionContext> {

    private final String concertId;

    public ScheduleConcert(String concertId) {
        this.concertId = concertId;
    }

    @Override
    public CommandResult execute(ScheduleConcertRequest request,
                                 NoDecisionContext noDecisionContext) {
        return new CommandResult(new ConcertScheduled(
                concertId,
                request.artist(),
                request.showDateTime(),
                request.doorsTime(),
                request.capacity(),
                request.ticketPrice(),
                request.maxTicketsPerPurchase()));
    }
}
