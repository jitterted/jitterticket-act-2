package dev.ted.jitterticket.act2.command;

import dev.ted.jitterticket.act2.event.TicketsSold;
import dev.ted.jitterticket.act2.readmodel.AvailableConcerts;

public class PurchaseTickets implements Command<PurchaseTicketsRequest, AvailableConcerts> {

    private final String ticketId;

    public PurchaseTickets(String ticketId) {
        this.ticketId = ticketId;
    }

    /**
     * Gets the ticket price from the Available Concerts read model.
     * What if that read model is behind the log?
     */
    @Override
    public CommandResult execute(PurchaseTicketsRequest request,
                                 AvailableConcerts availableConcerts) {
        int ticketPrice = availableConcerts.findConcert(request.concertId())
                                           .orElseThrow()
                                           .ticketPrice();
        return new CommandResult(new TicketsSold(
                request.concertId(),
                request.customerId(),
                ticketId,
                request.quantity(),
                ticketPrice * request.quantity()));
    }
}
