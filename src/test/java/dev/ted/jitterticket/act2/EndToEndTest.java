package dev.ted.jitterticket.act2;

import dev.ted.jitterticket.act2.command.CommandExecutor;
import dev.ted.jitterticket.act2.command.DateAlreadyBookedException;
import dev.ted.jitterticket.act2.command.ExecutionResult;
import dev.ted.jitterticket.act2.command.PurchaseTicketsCommand;
import dev.ted.jitterticket.act2.command.RegisterCustomerCommand;
import dev.ted.jitterticket.act2.command.RescheduleConcertCommand;
import dev.ted.jitterticket.act2.command.ScheduleConcertCommand;
import dev.ted.jitterticket.act2.event.ConcertRescheduled;
import dev.ted.jitterticket.act2.readmodel.AvailableConcerts;
import dev.ted.jitterticket.act2.readmodel.BookedDates;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The whole loop, the way the board plays it:
 * a command is executed, its event is appended to the log,
 * the log hands it to every read model, and the read models show the result.
 */
class EndToEndTest {

    private final EventLog eventLog = new EventLog();
    private final AvailableConcerts availableConcerts = new AvailableConcerts();
    private final BookedDates bookedDates = new BookedDates();
    private final CommandExecutor commandExecutor = new CommandExecutor(eventLog, bookedDates, availableConcerts);

    EndToEndTest() {
        eventLog.register(availableConcerts);
        eventLog.register(bookedDates);
    }

    @Test
    void scheduleConcert_ShowsUpInAvailableConcertsAndBookedDates() {
        // WHEN
        scheduleCosmicDrift();

        // THEN
        assertThat(availableConcerts.availableConcerts())
                .containsExactly(new AvailableConcerts.Concert(
                        "ct-1", "Cosmic Drift",
                        LocalDateTime.of(2026, 11, 14, 20, 0), LocalTime.of(19, 0),
                        45));
        assertThat(bookedDates.bookedDates())
                .containsExactly(new BookedDates.BookedDate(
                        LocalDate.of(2026, 11, 14), "Cosmic Drift"));
    }

    @Test
    void rescheduleConcert_BothReadModelsShowTheNewDate() {
        // GIVEN
        scheduleCosmicDrift();

        // WHEN
        assertAppended(commandExecutor.execute(new RescheduleConcertCommand(
                "ct-1", LocalDateTime.of(2026, 11, 21, 19, 30), LocalTime.of(18, 30))));

        // THEN
        assertThat(availableConcerts.availableConcerts())
                .extracting(AvailableConcerts.Concert::showDateTime)
                .containsExactly(LocalDateTime.of(2026, 11, 21, 19, 30));
        assertThat(bookedDates.bookedDates())
                .containsExactly(new BookedDates.BookedDate(
                        LocalDate.of(2026, 11, 21), "Cosmic Drift"));
    }

    @Test
    void rescheduleConcertToLaterTimeOnSameDay_ConcertRescheduledAppended() {
        // GIVEN: Cosmic Drift is on 2026-11-14 at 20:00
        scheduleCosmicDrift();

        // WHEN: it moves to 21:00 on the same day
        ExecutionResult result = commandExecutor.execute(new RescheduleConcertCommand(
                "ct-1", LocalDateTime.of(2026, 11, 14, 21, 0), LocalTime.of(20, 0)));

        // THEN: its own booking doesn't block it
        assertThat(result)
                .isEqualTo(new ExecutionResult.Appended(new SequencedEvent(
                        2, new ConcertRescheduled(
                                "ct-1", LocalDateTime.of(2026, 11, 14, 21, 0), LocalTime.of(20, 0)))));
    }

    @Test
    void rescheduleConcertOntoAnotherConcertsDate_ThrowsDateAlreadyBookedAndNothingAppended() {
        // GIVEN: Cosmic Drift on 2026-11-14, Lyrical Storm on 2026-12-05
        scheduleCosmicDrift();
        scheduleLyricalStorm();

        // WHEN / THEN
        assertThatThrownBy(() -> commandExecutor.execute(new RescheduleConcertCommand(
                "ct-1", LocalDateTime.of(2026, 12, 5, 19, 0), LocalTime.of(18, 0))))
                .isInstanceOf(DateAlreadyBookedException.class);
        assertThat(eventLog.lastSequenceNumber())
                .isEqualTo(2);
    }

    @Test
    void purchaseSomeTickets_ConcertStillAvailable() {
        // GIVEN
        scheduleCosmicDrift();
        registerKit();

        // WHEN
        purchase("ct-1", "cu-1", "t-1", 6);

        // THEN
        assertThat(availableConcerts.availableConcerts())
                .extracting(AvailableConcerts.Concert::concertId)
                .containsExactly("ct-1");
    }

    @Test
    void purchaseTheLastTickets_ConcertIsNoLongerAvailable() {
        // GIVEN: Lyrical Storm has a capacity of 20
        scheduleLyricalStorm();
        registerKit();

        // WHEN
        purchase("ct-2", "cu-1", "t-1", 20);

        // THEN
        assertThat(availableConcerts.availableConcerts())
                .isEmpty();
    }

    @Test
    void scriptedRunSeq1To8PlayedAsCommands_LogMatchesTheBoard() {
        // WHEN: the Command Desk plays seq 1 to 8
        scheduleCosmicDrift();
        assertAppended(commandExecutor.execute(new RescheduleConcertCommand(
                "ct-1", LocalDateTime.of(2026, 11, 21, 19, 30), LocalTime.of(18, 30))));
        scheduleLyricalStorm();
        registerKit();
        purchase("ct-1", "cu-1", "t-1", 6);
        register("cu-2", "ted@example.com");
        purchase("ct-2", "cu-2", "t-2", 8);
        register("cu-3", "mara@example.com");

        // THEN: the log is the Scripted Run...
        assertThat(eventLog.events())
                .extracting(SequencedEvent::event)
                .containsExactlyElementsOf(ScriptedRun.upToSeq(8));
        // ...and the read models match "After seq 7" and "After seq 8" in the guide
        assertThat(availableConcerts.availableConcerts())
                .extracting(AvailableConcerts.Concert::concertId)
                .containsExactly("ct-1", "ct-2");
        assertThat(bookedDates.bookedDates())
                .containsExactly(
                        new BookedDates.BookedDate(LocalDate.of(2026, 11, 21), "Cosmic Drift"),
                        new BookedDates.BookedDate(LocalDate.of(2026, 12, 5), "Lyrical Storm"));
        assertThat(bookedDates.lastSequenceNumber())
                .isEqualTo(8);
    }

    // Each helper executes a command and fails the test straight away
    // if the event wasn't appended.

    private static void assertAppended(ExecutionResult result) {
        assertThat(result)
                .isInstanceOf(ExecutionResult.Appended.class);
    }

    private void scheduleCosmicDrift() {
        assertAppended(commandExecutor.execute(new ScheduleConcertCommand(
                "ct-1",
                "Cosmic Drift",
                LocalDateTime.of(2026, 11, 14, 20, 0), LocalTime.of(19, 0),
                200, 45, 6)));
    }

    private void scheduleLyricalStorm() {
        assertAppended(commandExecutor.execute(new ScheduleConcertCommand(
                "ct-2",
                "Lyrical Storm",
                LocalDateTime.of(2026, 12, 5, 21, 0), LocalTime.of(20, 0),
                20, 500, 8)));
    }

    private void registerKit() {
        register("cu-1", "kit@example.com");
    }

    private void register(String customerId, String email) {
        assertAppended(commandExecutor.execute(new RegisterCustomerCommand(customerId, email)));
    }

    private void purchase(String concertId, String customerId, String ticketId, int quantity) {
        assertAppended(commandExecutor.execute(
                new PurchaseTicketsCommand(concertId, customerId, ticketId, quantity)));
    }
}
