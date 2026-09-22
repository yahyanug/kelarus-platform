package id.com.flare.kelarus.component.exception;

public class AuthException extends RuntimeException {
    private final AuthError error;

    public AuthException(AuthError error) {
        super(error.message());
        this.error = error;
    }

    public AuthError getError() { return error; }
}
