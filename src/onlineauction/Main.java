package onlineauction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Композиційний корінь детермінованої консольної демонстрації. */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        // Послідовно запускає детерміновані основний та альтернативні сценарії.
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
        System.out.printf("Перевірка ролей: продавець=%s, учасник торгів=%s, модератор=%s%n",
                seller.canPerform(lot), firstBidder.canPerform(lot), moderator.canPerform(lot));
        seller.submitForModeration(auction);
        moderator.approve(auction);
        firstBidder.placeBid(auction, new BigDecimal("100.00"), start);
        secondBidder.placeBid(auction, new BigDecimal("110.00"), end);
        secondBidder.placeBid(auction, new BigDecimal("115.00"), end.plusMinutes(1));
        secondBidder.placeBid(auction, new BigDecimal("115.00"), start.plusMinutes(20));
        firstBidder.placeBid(auction, new BigDecimal("120.00"), start.plusMinutes(20));
        auction.close();
        secondBidder.placeBid(auction, new BigDecimal("130.00"), end);

        System.out.println("=== Однакові суми ставок ===");
        Lot tieLot = seller.createLot("lot-tie", "Лот для однакових сум ставок", new BigDecimal("1.00"));
        Auction tieAuction = seller.createAuction("auction-tie", tieLot, start, end,
                new BigDecimal("0.00"), acquiringService);
        seller.submitForModeration(tieAuction);
        moderator.approve(tieAuction);
        firstBidder.placeBid(tieAuction, new BigDecimal("200.00"), start.plusMinutes(5));
        secondBidder.placeBid(tieAuction, new BigDecimal("200.00"), start.plusMinutes(6));
        tieAuction.close();
        System.out.printf("Однакові суми ставок: переможець %s за ранішою ставкою%n",
                tieAuction.getWinner().getDisplayName());

        System.out.println("=== Відхилення та повторне подання ===");
        Lot rejectedLot = seller.createLot("lot-2", "Навчальний лот", new BigDecimal("50.00"));
        Auction rejectedAuction = seller.createAuction("auction-2", rejectedLot, start, end,
                new BigDecimal("5.00"), acquiringService);
        seller.submitForModeration(rejectedAuction);
        moderator.reject(rejectedAuction, "опис потребує уточнення");
        System.out.printf("Повідомлення продавцю: аукціон %s відхилено; причина: %s%n",
                rejectedAuction.getAuctionId(), rejectedAuction.getRejectionReason());
        seller.submitForModeration(rejectedAuction);

        Lot moderationLot = seller.createLot("lot-2b", "Лот на модерації", new BigDecimal("55.00"));
        Auction moderationAuction = seller.createAuction("auction-2b", moderationLot, start, end,
                new BigDecimal("5.00"), acquiringService);
        seller.submitForModeration(moderationAuction);
        seller.withdraw(moderationAuction);

        Lot rejectedWithdrawLot = seller.createLot("lot-2c", "Лот після відхилення", new BigDecimal("60.00"));
        Auction rejectedWithdrawAuction = seller.createAuction("auction-2c", rejectedWithdrawLot, start, end,
                new BigDecimal("5.00"), acquiringService);
        seller.submitForModeration(rejectedWithdrawAuction);
        moderator.reject(rejectedWithdrawAuction, "потрібні додаткові характеристики");
        seller.withdraw(rejectedWithdrawAuction);

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
