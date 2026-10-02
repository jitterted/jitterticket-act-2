package dev.ted.jitterticket.act2;

import dev.ted.jitterticket.act2.event.ConcertRescheduled;
import dev.ted.jitterticket.act2.event.ConcertScheduled;
import dev.ted.jitterticket.act2.event.CustomerRegistered;
import dev.ted.jitterticket.act2.event.Event;
import dev.ted.jitterticket.act2.event.TicketsSold;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/**
 * Test data builders for events. A test only spells out the values its
 * assertions depend on; everything else gets a sensible default.
 * <p>
 * Usage: {@code import static dev.ted.jitterticket.act2.EventBuilders.*;}
 * <pre>
 * given(concertScheduled("ct-1").withCapacity(20),
 *       ticketsSold("ct-1").withQuantity(20));
 * </pre>
 */
public final class EventBuilders {

    public interface EventBuilder {
        Event build();
    }

    public static List<Event> events(EventBuilder... builders) {
        return Arrays.stream(builders).map(EventBuilder::build).toList();
    }

    public static ConcertScheduledBuilder concertScheduled(String concertId) {
        return new ConcertScheduledBuilder(concertId);
    }

    public static ConcertRescheduledBuilder concertRescheduled(String concertId) {
        return new ConcertRescheduledBuilder(concertId);
    }

    public static CustomerRegisteredBuilder customerRegistered(String customerId) {
        return new CustomerRegisteredBuilder(customerId);
    }

    public static TicketsSoldBuilder ticketsSold(String concertId) {
        return new TicketsSoldBuilder(concertId);
    }

    public static final class ConcertScheduledBuilder implements EventBuilder {
        private final String concertId;
        private String artist = "Some Artist";
        private LocalDateTime showDateTime = LocalDateTime.of(2026, 11, 14, 20, 0);
        private LocalTime doorsTime = LocalTime.of(19, 0);
        private int capacity = 100;
        private int ticketPrice = 50;
        private int maxTicketsPerPurchase = 8;

        private ConcertScheduledBuilder(String concertId) {
            this.concertId = concertId;
        }

        public ConcertScheduledBuilder withArtist(String artist) {
            this.artist = artist;
            return this;
        }

        public ConcertScheduledBuilder withShowDateTime(LocalDateTime showDateTime) {
            this.showDateTime = showDateTime;
            return this;
        }

        public ConcertScheduledBuilder withDoorsTime(LocalTime doorsTime) {
            this.doorsTime = doorsTime;
            return this;
        }

        public ConcertScheduledBuilder withCapacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        public ConcertScheduledBuilder withTicketPrice(int ticketPrice) {
            this.ticketPrice = ticketPrice;
            return this;
        }

        public ConcertScheduledBuilder withMaxTicketsPerPurchase(int maxTicketsPerPurchase) {
            this.maxTicketsPerPurchase = maxTicketsPerPurchase;
            return this;
        }

        @Override
        public ConcertScheduled build() {
            return new ConcertScheduled(concertId, artist, showDateTime, doorsTime,
                                        capacity, ticketPrice, maxTicketsPerPurchase);
        }
    }

    public static final class ConcertRescheduledBuilder implements EventBuilder {
        private final String concertId;
        private LocalDateTime newShowDateTime = LocalDateTime.of(2026, 11, 21, 20, 0);
        private LocalTime newDoorsTime = LocalTime.of(19, 0);

        private ConcertRescheduledBuilder(String concertId) {
            this.concertId = concertId;
        }

        public ConcertRescheduledBuilder withNewShowDateTime(LocalDateTime newShowDateTime) {
            this.newShowDateTime = newShowDateTime;
            return this;
        }

        public ConcertRescheduledBuilder withNewDoorsTime(LocalTime newDoorsTime) {
            this.newDoorsTime = newDoorsTime;
            return this;
        }

        @Override
        public ConcertRescheduled build() {
            return new ConcertRescheduled(concertId, newShowDateTime, newDoorsTime);
        }
    }

    public static final class CustomerRegisteredBuilder implements EventBuilder {
        private final String customerId;
        private String email;

        private CustomerRegisteredBuilder(String customerId) {
            this.customerId = customerId;
            this.email = customerId + "@example.com";
        }

        public CustomerRegisteredBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        @Override
        public CustomerRegistered build() {
            return new CustomerRegistered(customerId, email);
        }
    }

    public static final class TicketsSoldBuilder implements EventBuilder {
        private final String concertId;
        private String customerId = "cu-1";
        private String ticketId = "t-1";
        private int quantity = 1;
        private int totalCost = 50;

        private TicketsSoldBuilder(String concertId) {
            this.concertId = concertId;
        }

        public TicketsSoldBuilder toCustomer(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public TicketsSoldBuilder withTicketId(String ticketId) {
            this.ticketId = ticketId;
            return this;
        }

        public TicketsSoldBuilder withQuantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public TicketsSoldBuilder withTotalCost(int totalCost) {
            this.totalCost = totalCost;
            return this;
        }

        @Override
        public TicketsSold build() {
            return new TicketsSold(concertId, customerId, ticketId, quantity, totalCost);
        }
    }

    private EventBuilders() {
    }
}
