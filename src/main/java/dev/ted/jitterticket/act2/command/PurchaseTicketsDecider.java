package dev.ted.jitterticket.act2.command;

import dev.ted.jitterticket.act2.event.TicketsSold;

public final class PurchaseTicketsDecider {

    public static CommandResult decide(PurchaseTicketsCommand command, TicketPriceSnapshot snapshot) {
        return new CommandResult(new TicketsSold(
                command.concertId(),
                command.customerId(),
                command.ticketId(),
                command.quantity(),
                snapshot.ticketPrice() * command.quantity()));
    }

    private PurchaseTicketsDecider() {
    }
}
