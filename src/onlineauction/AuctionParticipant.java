package onlineauction;

import java.util.Objects;

public abstract class AuctionParticipant {
    private final String participantId;
    private final String displayName;

    protected AuctionParticipant(String participantId, String displayName) {
        this.participantId = requireText(participantId, "Ідентифікатор учасника");
        this.displayName = requireText(displayName, "Ім'я учасника");
    }

    public String getParticipantId() {
        return participantId;
    }

    public abstract String getRole();

    public final String getDisplayName() {
        return displayName;
    }

    protected static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " не може бути порожнім");
        }
        return value;
    }

    protected static <T> T requireNonNull(T value, String field) {
        return Objects.requireNonNull(value, field + " не може бути null");
    }
}
