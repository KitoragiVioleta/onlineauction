package onlineauction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Moderator extends AuctionParticipant implements ITradingRules {
    private final List<Auction> reviewedAuctions = new ArrayList<>();

    public Moderator(String participantId, String displayName) {
        super(participantId, displayName);
    }

    @Override
    public String getRole() {
        return "Модератор";
    }

    @Override
    public boolean canPerform(Lot lot) {
        return lot != null;
    }

    public void approve(Auction auction) {
        requireReviewableAuction(auction);
        reviewedAuctions.add(auction);
        auction.approve();
    }

    public void reject(Auction auction) {
        reject(auction, "лот не відповідає вимогам модерації");
    }

    public void reject(Auction auction, String reason) {
        requireReviewableAuction(auction);
        reviewedAuctions.add(auction);
        auction.reject(reason);
    }

    public List<Auction> getReviewedAuctions() {
        return Collections.unmodifiableList(reviewedAuctions);
    }

    private void requireReviewableAuction(Auction auction) {
        if (auction == null || !canPerform(auction.getLot())) {
            throw new IllegalArgumentException("Модератор не може перевірити відсутній лот");
        }
    }
}
