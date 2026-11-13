package onlineauction;

import java.math.BigDecimal;

/** Імітує списання коштів повідомленням у консолі. */
public final class AcquiringService implements IAcquiringService {
    private final String merchantId;

    public AcquiringService(String merchantId) {
        this.merchantId = requireText(merchantId, "Ідентифікатор мерчанта");
    }

    @Override
    public void charge(Bidder bidder, BigDecimal amount) {
        // Виводить симульовану оплату без реального фінансового списання.
        System.out.printf("Симульована оплата: учасник %s, сума %s, мерчант %s%n",
                bidder.getDisplayName(), amount, merchantId);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " не може бути порожнім");
        }
        return value;
    }
}
