package onlineauction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/** Незмінна модель прийнятої ставки. */
public final class Bid {
    private final BigDecimal amount;
    private final LocalDateTime submittedAt;
    private final Bidder bidder;

    public Bid(BigDecimal amount, LocalDateTime submittedAt, Bidder bidder) {
        this.amount = requireNonNegative(amount, "Сума ставки");
        this.submittedAt = Objects.requireNonNull(submittedAt, "Час ставки не може бути null");
        this.bidder = Objects.requireNonNull(bidder, "Учасник ставки не може бути null");
    }

    public BigDecimal getAmount() {
        // Повертає суму ставки.
        return amount;
    }

    public LocalDateTime getSubmittedAt() {
        // Повертає час подання ставки.
        return submittedAt;
    }

    public Bidder getBidder() {
        // Повертає учасника, який подав ставку.
        return bidder;
    }

    private static BigDecimal requireNonNegative(BigDecimal value, String field) {
        Objects.requireNonNull(value, field + " не може бути null");
        if (value.signum() < 0) {
            throw new IllegalArgumentException(field + " не може бути від'ємною");
        }
        return value;
    }
}
