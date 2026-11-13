package onlineauction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Роль учасника торгів, який подає ставки та може стати переможцем. */
public final class Bidder extends AuctionParticipant implements ITradingRules {
    private final List<Bid> submittedBids = new ArrayList<>();

    public Bidder(String participantId, String displayName) {
        super(participantId, displayName);
    }

    @Override
    public String getRole() {
        return "Учасник торгів";
    }

    @Override
    public boolean canPerform(Lot lot) {
        return lot != null;
    }

    public boolean placeBid(Auction auction, BigDecimal amount, LocalDateTime submittedAt) {
        // Делегує аукціону перевірку та приймання ставки від цього учасника.
        if (auction == null) {
            throw new IllegalArgumentException("Аукціон не може бути null");
        }
        if (!canPerform(auction.getLot())) {
            return false;
        }
        return auction.placeBid(this, amount, submittedAt);
    }

    void recordBid(Bid bid) {
        // Зберігає прийняту ставку в історії учасника.
        submittedBids.add(bid);
    }

    public List<Bid> getSubmittedBids() {
        return Collections.unmodifiableList(submittedBids);
    }
}
