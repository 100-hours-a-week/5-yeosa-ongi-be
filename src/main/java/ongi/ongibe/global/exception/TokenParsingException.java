package ongi.ongibe.global.exception;

public class TokenParsingException extends RuntimeException {

    public TokenParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public TokenParsingException(String message) {
        super(message);
    }
}
