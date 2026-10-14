package dev.ted.jitterticket.act2.command;

import dev.ted.jitterticket.act2.event.ConcertRescheduled;

import java.time.LocalDate;

public final class RescheduleConcertDecider {

    public static CommandResult decide(RescheduleConcertCommand command, BookedDatesDecisionContext decisionContext) {
        LocalDate newShowDate = command.newShowDateTime().toLocalDate();
        if (decisionContext.isBookedByAnotherConcert(newShowDate, command.concertId())) {
            throw new DateAlreadyBookedException(newShowDate);
        }
        return new CommandResult(new ConcertRescheduled(
                command.concertId(),
                command.newShowDateTime(),
                command.newDoorsTime()));
    }

    private RescheduleConcertDecider() {
    }
}
