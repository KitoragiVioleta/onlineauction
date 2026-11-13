package onlineauction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Роль продавця, яка створює, подає та знімає власні лоти. */
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
        return lot != null && lots.contains(lot);
    }

    public Lot createLot(String lotId, String description, BigDecimal startingPrice) {
        // Створює лот і додає його до власності продавця.
        Lot lot = new Lot(lotId, description, startingPrice);
        lots.add(lot);
        System.out.printf("Створення лоту: %s (%s), стартова ціна %s%n",
                lot.getLotId(), lot.getDescription(), lot.getStartingPrice());
        return lot;
    }

    public Auction createAuction(String auctionId, Lot lot, LocalDateTime startTime,
                                 LocalDateTime endTime, BigDecimal minimumIncrement,
                                 IAcquiringService acquiringService) {
        // Створює аукціон лише для лоту цього продавця.
        if (!canPerform(lot)) {
            throw new IllegalArgumentException("Продавець не може створити аукціон без лоту");
        }
        return new Auction(auctionId, lot, startTime, endTime, minimumIncrement, acquiringService);
    }

    public void submitForModeration(Auction auction) {
        // Передає власний аукціон на модерацію.
        requireOwnedAuction(auction);
        auction.submitForModeration();
    }

    public void withdraw(Auction auction) {
        // Знімає власний аукціон, якщо його стан це дозволяє.
        requireOwnedAuction(auction);
        auction.withdraw();
    }

    public List<Lot> getLots() {
        return Collections.unmodifiableList(lots);
    }

    private void requireOwnedAuction(Auction auction) {
        if (auction == null || !canPerform(auction.getLot())) {
            throw new IllegalArgumentException("Продавець не може виконати операцію для чужого або відсутнього лоту");
        }
    }
}
