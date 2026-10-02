package dev.ted.jitterticket.act2;

import dev.ted.jitterticket.act2.command.CommandExecutor;
import dev.ted.jitterticket.act2.command.ExecutionResult;
import dev.ted.jitterticket.act2.command.PurchaseTickets;
import dev.ted.jitterticket.act2.command.PurchaseTicketsRequest;
import dev.ted.jitterticket.act2.command.RegisterCustomer;
import dev.ted.jitterticket.act2.command.RegisterCustomerRequest;
import dev.ted.jitterticket.act2.command.RescheduleConcert;
import dev.ted.jitterticket.act2.command.RescheduleConcertRequest;
import dev.ted.jitterticket.act2.command.ScheduleConcert;
import dev.ted.jitterticket.act2.command.ScheduleConcertRequest;
import dev.ted.jitterticket.act2.event.TicketsSold;
import dev.ted.jitterticket.act2.readmodel.AvailableConcerts;
import dev.ted.jitterticket.act2.readmodel.BookedDates;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The whole loop, the way the board plays it:
 * a command runs, its event is appended to the log,
 * the log hands it to every read model, and the read models show the result.
 */
class EndToEndTest {

    private final EventLog eventLog = new EventLog();
    private final AvailableConcerts availableConcerts = new AvailableConcerts();
    private final BookedDates bookedDates = new BookedDates();
    private final CommandExecutor commandExecutor = new CommandExecutor(eventLog);

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
        commandExecutor.execute(
                new RescheduleConcert(),
                new RescheduleConcertRequest(
                        "ct-1", LocalDateTime.of(2026, 11, 21, 19, 30), LocalTime.of(18, 30)),
                bookedDates.lastSequenceNumber());

        // THEN
        assertThat(availableConcerts.availableConcerts())
                .extracting(AvailableConcerts.Concert::showDateTime)
                .containsExactly(LocalDateTime.of(2026, 11, 21, 19, 30));
        assertThat(bookedDates.bookedDates())
                .containsExactly(new BookedDates.BookedDate(
                        LocalDate.of(2026, 11, 21), "Cosmic Drift"));
    }

    @Test
    void purchaseSomeTickets_ConcertStillAvailable() {
        // GIVEN
        scheduleCosmicDrift();
        registerKit();

        // WHEN: the Buy Tickets screen was built from Available Concerts
        ExecutionResult result = commandExecutor.execute(
                new PurchaseTickets("t-1"),
                new PurchaseTicketsRequest("ct-1", "cu-1", 6),
                availableConcerts,
                availableConcerts.lastSequenceNumber());

        // THEN
        assertThat(result)
                .isInstanceOf(ExecutionResult.Appended.class);
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
        purchase("t-1", "ct-2", "cu-1", 20);

        // THEN
        assertThat(availableConcerts.availableConcerts())
                .isEmpty();
    }

    @Test
    void purchaseFromOutOfDateScreen_ConflictAndNoTicketsSold() {
        // GIVEN: the Buy Tickets screen is built...
        scheduleCosmicDrift();
        long screenBuiltAt = availableConcerts.lastSequenceNumber();
        // ...then someone registers before the purchase is submitted
        registerKit();

        // WHEN
        ExecutionResult result = commandExecutor.execute(
                new PurchaseTickets("t-1"),
                new PurchaseTicketsRequest("ct-1", "cu-1", 6),
                availableConcerts,
                screenBuiltAt);

        // THEN
        assertThat(result)
                .isEqualTo(new ExecutionResult.Conflict(1, 2));
        assertThat(eventLog.events())
                .extracting(SequencedEvent::event)
                .noneMatch(event -> event instanceof TicketsSold);
    }

    @Test
    void scriptedRunSeq1To8PlayedAsCommands_LogMatchesTheBoard() {
        // WHEN: the Command Desk plays seq 1 to 8
        scheduleCosmicDrift();
        assertAppended(commandExecutor.execute(
                new RescheduleConcert(),
                new RescheduleConcertRequest(
                        "ct-1", LocalDateTime.of(2026, 11, 21, 19, 30), LocalTime.of(18, 30)),
                eventLog.lastSequenceNumber()));
        scheduleLyricalStorm();
        registerKit();
        purchase("t-1", "ct-1", "cu-1", 6);
        register("cu-2", "ted@example.com");
        purchase("t-2", "ct-2", "cu-2", 8);
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

    // Each helper submits from a screen that is up to date with the log,
    // and fails the test straight away if the event wasn't appended.

    private static void assertAppended(ExecutionResult result) {
        assertThat(result)
                .isInstanceOf(ExecutionResult.Appended.class);
    }

    private void scheduleCosmicDrift() {
        assertAppended(commandExecutor.execute(
                new ScheduleConcert("ct-1"),
                new ScheduleConcertRequest(
                        "Cosmic Drift",
                        LocalDateTime.of(2026, 11, 14, 20, 0), LocalTime.of(19, 0),
                        200, 45, 6),
                eventLog.lastSequenceNumber()));
    }

    private void scheduleLyricalStorm() {
        assertAppended(commandExecutor.execute(
                new ScheduleConcert("ct-2"),
                new ScheduleConcertRequest(
                        "Lyrical Storm",
                        LocalDateTime.of(2026, 12, 5, 21, 0), LocalTime.of(20, 0),
                        20, 500, 8),
                eventLog.lastSequenceNumber()));
    }

    private void registerKit() {
        register("cu-1", "kit@example.com");
    }

    private void register(String customerId, String email) {
        assertAppended(commandExecutor.execute(
                new RegisterCustomer(customerId),
                new RegisterCustomerRequest(email),
                eventLog.lastSequenceNumber()));
    }

    private void purchase(String ticketId, String concertId, String customerId, int quantity) {
        assertAppended(commandExecutor.execute(
                new PurchaseTickets(ticketId),
                new PurchaseTicketsRequest(concertId, customerId, quantity),
                availableConcerts,
                availableConcerts.lastSequenceNumber()));
    }
}
