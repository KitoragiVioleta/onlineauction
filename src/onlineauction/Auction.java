package onlineauction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class Auction {
    private final String auctionId;
    private final Lot lot;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final BigDecimal minimumIncrement;
    private final IAcquiringService acquiringService;
    private AuctionStatus status = AuctionStatus.DRAFT;
    private BigDecimal finalPrice;
    private Bidder winner;
    private String rejectionReason;

    public Auction(String auctionId, Lot lot, LocalDateTime startTime, LocalDateTime endTime,
                   BigDecimal minimumIncrement, IAcquiringService acquiringService) {
        this.auctionId = AuctionParticipant.requireText(auctionId, "Ідентифікатор аукціону");
        this.lot = AuctionParticipant.requireNonNull(lot, "Лот");
        this.startTime = AuctionParticipant.requireNonNull(startTime, "Час початку");
        this.endTime = AuctionParticipant.requireNonNull(endTime, "Час завершення");
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Час початку має бути раніше часу завершення");
        }
        if (minimumIncrement == null || minimumIncrement.signum() <= 0) {
            throw new IllegalArgumentException("Мінімальний крок має бути додатним");
        }
        this.minimumIncrement = minimumIncrement;
        this.acquiringService = AuctionParticipant.requireNonNull(acquiringService, "Сервіс еквайрингу");
    }

    public String getAuctionId() {
        return auctionId;
    }

    public Lot getLot() {
        return lot;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public Bidder getWinner() {
        return winner;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void submitForModeration() {
        if (status != AuctionStatus.DRAFT && status != AuctionStatus.REJECTED) {
            rejectOperation("Подання на модерацію", "поточний стан не дозволяє подання");
            return;
        }
        AuctionStatus previous = status;
        status = AuctionStatus.ON_MODERATION;
        System.out.printf("Подання на модерацію: %s -> %s%n", previous.getLabel(), status.getLabel());
    }

    public void approve() {
        if (status != AuctionStatus.ON_MODERATION) {
            rejectOperation("Схвалення", "поточний стан не На модерації");
            return;
        }
        status = AuctionStatus.ACTIVE;
        System.out.printf("Схвалення: На модерації -> %s%n", status.getLabel());
    }

    public void reject(String reason) {
        if (status != AuctionStatus.ON_MODERATION) {
            rejectOperation("Відхилення", "поточний стан не На модерації");
            return;
        }
        rejectionReason = AuctionParticipant.requireText(reason, "Причина відхилення");
        status = AuctionStatus.REJECTED;
        System.out.printf("Відхилення: На модерації -> %s; причина: %s%n",
                status.getLabel(), rejectionReason);
    }

    public void withdraw() {
        if (status != AuctionStatus.DRAFT && status != AuctionStatus.ON_MODERATION
                && status != AuctionStatus.REJECTED) {
            rejectOperation("Зняття", "поточний стан не дозволяє зняття");
            return;
        }
        AuctionStatus previous = status;
        status = AuctionStatus.WITHDRAWN;
        System.out.printf("Зняття: %s -> %s%n", previous.getLabel(), status.getLabel());
    }

    public boolean placeBid(Bidder bidder, BigDecimal amount) {
        return placeBid(bidder, amount, LocalDateTime.now());
    }

    public boolean placeBid(Bidder bidder, BigDecimal amount, LocalDateTime submittedAt) {
        if (bidder == null) {
            rejectOperation("Подання ставки", "учасник ставки не визначений");
            return false;
        }
        if (status != AuctionStatus.ACTIVE) {
            rejectOperation("Подання ставки", "аукціон неактивний");
            return false;
        }
        if (submittedAt == null || submittedAt.isBefore(startTime) || submittedAt.isAfter(endTime)) {
            rejectOperation("Подання ставки", "ставка поза часовими межами");
            return false;
        }
        BigDecimal minimumAmount = lot.getBids().isEmpty()
                ? lot.getStartingPrice()
                : lot.getHighestBid().getAmount().add(minimumIncrement);
        if (amount == null || amount.compareTo(minimumAmount) < 0) {
            rejectOperation("Подання ставки", "сума нижча за мінімально допустиму");
            return false;
        }
        Bid acceptedBid = lot.addBid(amount, submittedAt, bidder);
        bidder.recordBid(acceptedBid);
        System.out.printf("Ставка прийнята: %s, сума %s, прийнятих ставок %d%n",
                bidder.getDisplayName(), amount, lot.getBids().size());
        return true;
    }

    public void close() {
        if (status != AuctionStatus.ACTIVE) {
            rejectOperation("Завершення", "поточний стан не Активні торги");
            return;
        }
        Bid highestBid = lot.getHighestBid();
        if (highestBid == null) {
            status = AuctionStatus.FINISHED_WITHOUT_WINNER;
            System.out.printf("Завершення без переможця: статус %s%n", status.getLabel());
            return;
        }
        winner = determineWinner();
        finalPrice = highestBid.getAmount();
        status = AuctionStatus.FINISHED_WITH_WINNER;
        System.out.printf("Завершення з переможцем: %s, підсумкова ціна %s, статус %s%n",
                winner.getDisplayName(), finalPrice, status.getLabel());
        acquiringService.charge(winner, finalPrice);
    }

    public Bidder determineWinner() {
        Bid highestBid = lot.getHighestBid();
        return highestBid == null ? null : highestBid.getBidder();
    }

    private void rejectOperation(String operation, String reason) {
        String bidCount = operation.equals("Подання ставки")
                ? "; прийнятих ставок не змінилося: " + lot.getBids().size()
                : "";
        System.out.printf("Операцію відхилено: %s; стан: %s; причина: %s%s%n",
                operation, status.getLabel(), reason, bidCount);
    }
}
