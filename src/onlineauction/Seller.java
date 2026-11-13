package onlineauction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Seller extends AuctionParticipant implements ITradingRules {
    private final List<Lot> lots = new ArrayList<>();

    public Seller(String participantId, String displayName) {
        super(participantId, displayName);
    }

    @Override
    public String getRole() {
        return "Продавець";
    }

    @Override
    public boolean canPerform(Lot lot) {
        return lot != null;
    }

    public Lot createLot(String lotId, String description, BigDecimal startingPrice) {
        Lot lot = new Lot(lotId, description, startingPrice);
        lots.add(lot);
        System.out.printf("Створення лоту: %s (%s), стартова ціна %s%n",
                lot.getLotId(), lot.getDescription(), lot.getStartingPrice());
        return lot;
    }

    public Auction createAuction(String auctionId, Lot lot, LocalDateTime startTime,
                                 LocalDateTime endTime, BigDecimal minimumIncrement,
                                 IAcquiringService acquiringService) {
        if (!canPerform(lot)) {
            throw new IllegalArgumentException("Продавець не може створити аукціон без лоту");
        }
        return new Auction(auctionId, lot, startTime, endTime, minimumIncrement, acquiringService);
    }

    public void submitForModeration(Auction auction) {
        auction.submitForModeration();
    }

    public void withdraw(Auction auction) {
        auction.withdraw();
    }

    public List<Lot> getLots() {
        return Collections.unmodifiableList(lots);
    }
}
