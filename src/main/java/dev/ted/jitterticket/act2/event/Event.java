package dev.ted.jitterticket.act2.event;

/**
 * Something that happened. Events are facts in the past tense, and they never change.
 */
public sealed interface Event permits
        ConcertScheduled,
        ConcertRescheduled,
        CustomerRegistered,
        TicketsSold,
        TicketSalesStopped,
        TicketSalesScheduled,
        TicketSalesStarted {
}
