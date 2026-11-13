package dev.ted.jitterticket.act2.readmodel;

import dev.ted.jitterticket.act2.SequencedEvent;
import dev.ted.jitterticket.act2.event.ConcertScheduled;
import dev.ted.jitterticket.act2.event.TicketsSold;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConcertSales implements ReadModel {
    private long lastSequenceNumber;
    private Map<String, SalesInfo> salesInfos = new HashMap<>();

    public List<SalesInfo> sales() {
        return salesInfos.values()
                .stream().toList();
    }

    @Override
    public void apply(SequencedEvent sequencedEvent) {
        lastSequenceNumber = sequencedEvent.sequenceNumber();
        switch (sequencedEvent.event()) {
            case ConcertScheduled cs ->
                    salesInfos.put(cs.concertId(),
                            new SalesInfo(
                                    cs.concertId(),
                                    cs.artist(), 0, 0));
            case TicketsSold ts -> {
                SalesInfo salesInfo = salesInfos.get(ts.concertId());
                SalesInfo newSalesInfo = new SalesInfo(ts.concertId(),
                        salesInfo.artist(),
                        ts.quantity(),
                        ts.totalCost());
                salesInfos.put(ts.concertId(), newSalesInfo);
            }
            default -> {} // don't care
        }
    }

    @Override
    public long lastSequenceNumber() {
        return lastSequenceNumber;
    }
}

record SalesInfo(String concertId, String artist, int ticketsSold, long revenue) {
}
