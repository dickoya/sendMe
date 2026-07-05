package ibrahima.dickoya.sendme.exception;

/**
 * Exception levée au démarrage de l'application lorsqu'un {@code ChannelType}
 * n'a pas de stratégie de transfert associée ({@code TransferStrategy}).
 * <p>
 * Il s'agit d'une erreur de configuration fatale qui empêche le démarrage
 * de l'application, évitant ainsi des échecs silencieux à l'exécution.
 */
public class MissingStrategyException extends RuntimeException {

    public MissingStrategyException(String message) {
        super(message);
    }
}
