package dev.ted.jitterticket.act2.command;

import java.time.LocalDate;

public class DateAlreadyBookedException extends RuntimeException {

    private final LocalDate date;

    public DateAlreadyBookedException(LocalDate date) {
        super("Date is already booked: " + date);
        this.date = date;
    }

    public LocalDate date() {
        return date;
    }
}
