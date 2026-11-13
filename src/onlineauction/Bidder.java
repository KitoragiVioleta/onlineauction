package onlineauction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    void recordBid(Bid bid) {
        submittedBids.add(bid);
    }

    public List<Bid> getSubmittedBids() {
        return Collections.unmodifiableList(submittedBids);
    }
}
