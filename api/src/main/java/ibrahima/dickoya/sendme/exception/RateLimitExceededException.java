package ibrahima.dickoya.sendme.exception;

/**
 * Exception levée lorsqu'une limite de taux est dépassée (rate limiting).
 * Provoque une réponse HTTP 429 Too Many Requests.
 */
public class RateLimitExceededException extends RuntimeException {

    private final long retryAfterSeconds;

    public RateLimitExceededException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
