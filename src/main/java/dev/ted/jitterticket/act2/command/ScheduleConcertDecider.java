package dev.ted.jitterticket.act2.command;

import dev.ted.jitterticket.act2.event.ConcertScheduled;

import java.time.LocalDate;

public final class ScheduleConcertDecider {

    public static CommandResult decide(ScheduleConcertCommand command, BookedDatesSnapshot snapshot) {
        LocalDate showDate = command.showDateTime().toLocalDate();
        if (snapshot.isBooked(showDate)) {
            throw new DateAlreadyBookedException(showDate);
        }
        return new CommandResult(new ConcertScheduled(
                command.concertId(),
                command.artist(),
                command.showDateTime(),
                command.doorsTime(),
                command.capacity(),
                command.ticketPrice(),
                command.maxTicketsPerPurchase()));
    }

    private ScheduleConcertDecider() {
    }
}
