package dev.ted.jitterticket.act2;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookedDatesTest {

    @Test
    void concertScheduled_DateIsBooked() {
        BookedDates bookedDates = given(List.of(
                ScriptedRun.SEQ_1));

        assertThat(bookedDates.bookedDates())
                .containsExactly(new BookedDates.BookedDate(
                        LocalDate.of(2026, 11, 14), "Cosmic Drift"));
    }

    @Test
    void concertRescheduled_OldDateIsFreedNewDateIsBooked() {
        BookedDates bookedDates = given(List.of(
                ScriptedRun.SEQ_1,
                ScriptedRun.SEQ_2));

        assertThat(bookedDates.bookedDates())
                .containsExactly(new BookedDates.BookedDate(
                        LocalDate.of(2026, 11, 21), "Cosmic Drift"));
    }

    @Test
    void scriptedRunUpToSeq8_IgnoresEverythingButConcertEvents() {
        BookedDates bookedDates = given(ScriptedRun.upToSeq(8));

        assertThat(bookedDates.bookedDates())
                .containsExactly(
                        new BookedDates.BookedDate(LocalDate.of(2026, 11, 21), "Cosmic Drift"),
                        new BookedDates.BookedDate(LocalDate.of(2026, 12, 5), "Lyrical Storm"));
    }

    private static BookedDates given(List<Event> events) {
        BookedDates bookedDates = new BookedDates();
        EventLog eventLog = new EventLog();
        eventLog.register(bookedDates);
        eventLog.appendAll(events);
        return bookedDates;
    }
}
