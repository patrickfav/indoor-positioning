package at.ac.tuwien.inso.indoor.sensorserver.services.requests;

import at.ac.tuwien.inso.indoor.sensorserver.services.exceptions.SensorRequestException;

/**
 * Created by PatrickF on 05.10.2014.
 */
public interface IPingRequest {
    public Boolean startRequest() throws SensorRequestException;
}
