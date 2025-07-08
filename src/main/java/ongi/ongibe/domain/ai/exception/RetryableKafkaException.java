package ongi.ongibe.domain.ai.exception;

public class RetryableKafkaException extends RuntimeException {
    public RetryableKafkaException(String message) {
        super(message);
    }
}
