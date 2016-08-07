package at.ac.tuwien.inso.indoor.sensorserver.services.exceptions;

/**
 * Created by PatrickF on 13.09.2014.
 */
public class SensorRequestException extends Exception {
    public SensorRequestException() {
        super();
    }

    public SensorRequestException(String message) {
        super(message);
    }

    public SensorRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public SensorRequestException(Throwable cause) {
        super(cause);
    }

}
