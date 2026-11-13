package onlineauction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class Bid {
    private final BigDecimal amount;
    private final LocalDateTime submittedAt;
    private final Bidder bidder;

    public Bid(BigDecimal amount, LocalDateTime submittedAt, Bidder bidder) {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("Сума ставки не може бути від'ємною");
        }
        this.amount = amount;
        this.submittedAt = AuctionParticipant.requireNonNull(submittedAt, "Час ставки");
        this.bidder = AuctionParticipant.requireNonNull(bidder, "Учасник ставки");
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public Bidder getBidder() {
        return bidder;
    }
}
