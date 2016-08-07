package at.ac.tuwien.inso.indoor.sensorserver.services.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * @author PatrickF
 * @since 13.01.14
 * Time: 16:57
 */
public class InvalidAPICallException extends WebApplicationException {
    public InvalidAPICallException() {
    }

    public InvalidAPICallException(String message) {
        super(message, Response.Status.BAD_REQUEST);
    }
}
