package onlineauction;

public enum AuctionStatus {
    DRAFT("Чернетка"),
    ON_MODERATION("На модерації"),
    REJECTED("Відхилено"),
    ACTIVE("Активні торги"),
    FINISHED_WITH_WINNER("Завершено з переможцем"),
    FINISHED_WITHOUT_WINNER("Завершено без переможця"),
    WITHDRAWN("Знято");

    private final String label;

    AuctionStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
