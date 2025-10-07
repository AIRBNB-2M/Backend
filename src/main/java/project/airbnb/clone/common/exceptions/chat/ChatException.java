package project.airbnb.clone.common.exceptions.chat;

public abstract class ChatException extends RuntimeException {
    public ChatException(String message) {
        super(message);
    }

    public ChatException(String message, Throwable cause) {
        super(message, cause);
    }
}
