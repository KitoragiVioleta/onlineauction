package onlineauction;

import java.math.BigDecimal;

public final class AcquiringService implements IAcquiringService {
    private final String merchantId;

    public AcquiringService(String merchantId) {
        this.merchantId = AuctionParticipant.requireText(merchantId, "Ідентифікатор продавця");
    }

    @Override
    public void charge(Bidder bidder, BigDecimal amount) {
        System.out.printf("Симульована оплата: учасник %s, сума %s, мерчант %s%n",
                bidder.getDisplayName(), amount, merchantId);
    }
}
