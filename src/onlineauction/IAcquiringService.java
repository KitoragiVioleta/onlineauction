package onlineauction;

import java.math.BigDecimal;

public interface IAcquiringService {
    void charge(Bidder bidder, BigDecimal amount);
}
