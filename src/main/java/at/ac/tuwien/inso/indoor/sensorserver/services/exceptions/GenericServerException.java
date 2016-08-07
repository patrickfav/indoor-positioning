package at.ac.tuwien.inso.indoor.sensorserver.services.exceptions;

/**
 * Created by PatrickF on 16.09.2014.
 */
public class GenericServerException extends RuntimeException {
    public GenericServerException() {
        super();
    }

    public GenericServerException(String message) {
        super(message);
    }

    public GenericServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public GenericServerException(Throwable cause) {
        super(cause);
    }
}
