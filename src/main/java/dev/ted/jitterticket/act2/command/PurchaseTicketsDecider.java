package dev.ted.jitterticket.act2.command;

import dev.ted.jitterticket.act2.event.TicketsSold;

public final class PurchaseTicketsDecider {

    public static CommandResult decide(PurchaseTicketsCommand command, TicketPriceDecisionContext decisionContext) {
        return new CommandResult(new TicketsSold(
                command.concertId(),
                command.customerId(),
                command.ticketId(),
                command.quantity(),
                decisionContext.ticketPrice() * command.quantity()));
    }

    private PurchaseTicketsDecider() {
    }
}
