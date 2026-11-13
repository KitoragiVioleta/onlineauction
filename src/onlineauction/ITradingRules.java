package onlineauction;

/** Контракт перевірки можливості рольової операції для лоту. */
public interface ITradingRules {
    /** Перевіряє, чи може роль працювати з указаним лотом. */
    boolean canPerform(Lot lot);
}
