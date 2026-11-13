package onlineauction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        IAcquiringService acquiringService = new AcquiringService("DEMO-MERCHANT");
        Seller seller = new Seller("seller-1", "Олена-продавець");
        Moderator moderator = new Moderator("moderator-1", "Ірина-модератор");
        Bidder firstBidder = new Bidder("bidder-1", "Андрій");
        Bidder secondBidder = new Bidder("bidder-2", "Марія");
        LocalDateTime start = LocalDateTime.of(2026, 11, 13, 10, 0);
        LocalDateTime end = start.plusHours(1);

        System.out.println("=== Основний сценарій ===");
        Lot lot = seller.createLot("lot-1", "Вінтажний годинник", new BigDecimal("100.00"));
        Auction auction = seller.createAuction("auction-1", lot, start, end,
                new BigDecimal("10.00"), acquiringService);
        seller.submitForModeration(auction);
        moderator.approve(auction);
        auction.placeBid(firstBidder, new BigDecimal("100.00"), start);
        auction.placeBid(secondBidder, new BigDecimal("110.00"), end);
        auction.placeBid(secondBidder, new BigDecimal("115.00"), end.plusMinutes(1));
        auction.placeBid(secondBidder, new BigDecimal("115.00"), start.plusMinutes(20));
        auction.placeBid(firstBidder, new BigDecimal("120.00"), start.plusMinutes(20));
        auction.close();

        System.out.println("=== Відхилення та повторне подання ===");
        Lot rejectedLot = seller.createLot("lot-2", "Навчальний лот", new BigDecimal("50.00"));
        Auction rejectedAuction = seller.createAuction("auction-2", rejectedLot, start, end,
                new BigDecimal("5.00"), acquiringService);
        seller.submitForModeration(rejectedAuction);
        moderator.reject(rejectedAuction, "опис потребує уточнення");
        System.out.printf("Повідомлення продавцю: аукціон %s відхилено; причина: %s%n",
                rejectedAuction.getAuctionId(), rejectedAuction.getRejectionReason());
        seller.submitForModeration(rejectedAuction);

        System.out.println("=== Альтернативні завершення ===");
        Lot withdrawnLot = seller.createLot("lot-3", "Лот для зняття", new BigDecimal("20.00"));
        Auction withdrawnAuction = seller.createAuction("auction-3", withdrawnLot, start, end,
                new BigDecimal("2.00"), acquiringService);
        seller.withdraw(withdrawnAuction);

        Lot activeLot = seller.createLot("lot-4", "Активний лот", new BigDecimal("30.00"));
        Auction activeAuction = seller.createAuction("auction-4", activeLot, start, end,
                new BigDecimal("3.00"), acquiringService);
        seller.submitForModeration(activeAuction);
        moderator.approve(activeAuction);
        seller.withdraw(activeAuction);

        Lot noWinnerLot = seller.createLot("lot-5", "Лот без ставок", new BigDecimal("40.00"));
        Auction noWinnerAuction = seller.createAuction("auction-5", noWinnerLot, start, end,
                new BigDecimal("4.00"), acquiringService);
        seller.submitForModeration(noWinnerAuction);
        moderator.approve(noWinnerAuction);
        noWinnerAuction.close();
    }
}
