package dev.ted.jitterticket.act2;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * The Scripted Run from the Miro board, seq 1 to 15. Use these as test fixtures.
 */
public final class ScriptedRun {

    public static final Event SEQ_1 = new ConcertScheduled(
            "ct-1", "Cosmic Drift",
            LocalDateTime.of(2026, 11, 14, 20, 0), LocalTime.of(19, 0),
            200, 45, 6);
    public static final Event SEQ_2 = new ConcertRescheduled(
            "ct-1",
            LocalDateTime.of(2026, 11, 21, 19, 30), LocalTime.of(18, 30));
    public static final Event SEQ_3 = new ConcertScheduled(
            "ct-2", "Lyrical Storm",
            LocalDateTime.of(2026, 12, 5, 21, 0), LocalTime.of(20, 0),
            20, 500, 8);
    public static final Event SEQ_4 = new CustomerRegistered("cu-1", "kit@example.com");
    public static final Event SEQ_5 = new TicketsSold("ct-1", "cu-1", "t-1", 6, 270);
    public static final Event SEQ_6 = new CustomerRegistered("cu-2", "ted@example.com");
    public static final Event SEQ_7 = new TicketsSold("ct-2", "cu-2", "t-2", 8, 4000);
    public static final Event SEQ_8 = new CustomerRegistered("cu-3", "mara@example.com");
    public static final Event SEQ_9 = new TicketsSold("ct-2", "cu-3", "t-3", 2, 1000);
    public static final Event SEQ_10 = new TicketsSold("ct-2", "cu-3", "t-4", 4, 2000);
    public static final Event SEQ_11 = new TicketsSold("ct-2", "cu-1", "t-5", 6, 3000);
    public static final Event SEQ_12 = new TicketSalesStopped("ct-2", "sold out");
    public static final Event SEQ_13 = new ConcertScheduled(
            "ct-3", "Jagged Arrays",
            LocalDateTime.of(2026, 12, 20, 20, 0), LocalTime.of(19, 0),
            120, 60, 4);
    public static final Event SEQ_14 = new TicketSalesScheduled(
            "ct-3", LocalDateTime.of(2026, 12, 1, 9, 0));
    public static final Event SEQ_15 = new TicketSalesStarted("ct-3", "scheduled time");

    private static final List<Event> ALL = List.of(
            SEQ_1, SEQ_2, SEQ_3, SEQ_4, SEQ_5, SEQ_6, SEQ_7, SEQ_8,
            SEQ_9, SEQ_10, SEQ_11, SEQ_12, SEQ_13, SEQ_14, SEQ_15);

    /**
     * Seq 1 up to and including the given seq.
     */
    public static List<Event> upToSeq(int seq) {
        return ALL.subList(0, seq);
    }

    private ScriptedRun() {
    }
}
