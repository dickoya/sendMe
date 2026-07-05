package ibrahima.dickoya.sendme.exception;

/**
 * Exception levée lorsqu'une opération liée au parrainage échoue.
 * 
 * Utilisée dans TransferService pour les appels à ReferralService
 * (rewardFirstTransfer, useCoupon) afin de ne pas interrompre
 * le flux de transfert tout en traçant l'erreur.
 */
public class ReferralException extends RuntimeException {
    public ReferralException(String message, Throwable cause) {
        super(message, cause);
    }
}
