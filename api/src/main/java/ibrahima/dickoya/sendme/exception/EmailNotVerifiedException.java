package ibrahima.dickoya.sendme.exception;

import lombok.Getter;

/**
 * Exception levée lorsqu'un utilisateur tente de se connecter
 * sans avoir vérifié son adresse email.
 */
@Getter
public class EmailNotVerifiedException extends RuntimeException {

    private final String email;

    public EmailNotVerifiedException(String message, String email) {
        super(message);
        this.email = email;
    }
}
