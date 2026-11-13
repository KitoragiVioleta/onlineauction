package onlineauction;

import java.math.BigDecimal;

/** Абстракція симульованої оплати переможцем. */
public interface IAcquiringService {
    /** Виконує консольну імітацію списання за переможну ставку. */
    void charge(Bidder bidder, BigDecimal amount);
}
