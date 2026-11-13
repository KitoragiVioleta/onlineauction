package onlineauction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Lot {
    private final String lotId;
    private final String description;
    private final BigDecimal startingPrice;
    private final List<Bid> bids = new ArrayList<>();

    public Lot(String lotId, String description, BigDecimal startingPrice) {
        this.lotId = AuctionParticipant.requireText(lotId, "Ідентифікатор лоту");
        this.description = AuctionParticipant.requireText(description, "Опис лоту");
        if (startingPrice == null || startingPrice.signum() < 0) {
            throw new IllegalArgumentException("Стартова ціна не може бути від'ємною");
        }
        this.startingPrice = startingPrice;
    }

    public String getLotId() {
        return lotId;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    public Bid addBid(BigDecimal amount, LocalDateTime submittedAt, Bidder bidder) {
        Bid bid = new Bid(amount, submittedAt, bidder);
        bids.add(bid);
        return bid;
    }

    public Bid getHighestBid() {
        return bids.stream()
                .max((left, right) -> {
                    int amountComparison = left.getAmount().compareTo(right.getAmount());
                    if (amountComparison != 0) {
                        return amountComparison;
                    }
                    int timeComparison = right.getSubmittedAt().compareTo(left.getSubmittedAt());
                    return timeComparison != 0 ? timeComparison : Integer.compare(bids.indexOf(right), bids.indexOf(left));
                })
                .orElse(null);
    }

    public List<Bid> getBids() {
        return Collections.unmodifiableList(bids);
    }
}
