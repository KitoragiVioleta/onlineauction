package onlineauction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Описує лот і зберігає прийняті ставки в порядку додавання. */
public final class Lot {
    private final String lotId;
    private final String description;
    private final BigDecimal startingPrice;
    private final List<Bid> bids = new ArrayList<>();

    public Lot(String lotId, String description, BigDecimal startingPrice) {
        this.lotId = requireText(lotId, "Ідентифікатор лоту");
        this.description = requireText(description, "Опис лоту");
        this.startingPrice = requireNonNegative(startingPrice, "Стартова ціна");
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
        // Створює та зберігає прийняту ставку.
        Bid bid = new Bid(amount, submittedAt, bidder);
        bids.add(bid);
        return bid;
    }

    public Bid getHighestBid() {
        // Визначає найвищу ставку з детермінованим правилом однакових сум.
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

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " не може бути порожнім");
        }
        return value;
    }

    private static BigDecimal requireNonNegative(BigDecimal value, String field) {
        Objects.requireNonNull(value, field + " не може бути null");
        if (value.signum() < 0) {
            throw new IllegalArgumentException(field + " не може бути від'ємною");
        }
        return value;
    }
}
