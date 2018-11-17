package at.ac.tuwien.inso.indoor.sensorserver.services.requests;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNode;
import at.ac.tuwien.inso.indoor.sensorserver.services.ApiConst;
import at.ac.tuwien.inso.indoor.sensorserver.services.exceptions.SensorRequestException;

import javax.ws.rs.core.Response;

/**
 * Created by PatrickF on 12.09.2014.
 */
public class RouterRebootRequest extends ARequest<Boolean> {

    public RouterRebootRequest(SensorNode node) {
        super(node);
    }

    @Override
    public Boolean startRequest() throws SensorRequestException {
        try {
            ResponseWrapper responseWrapper = runRequest("GET", getNode().getFullUrl() + ApiConst.ROUTER_SERVICE_REBOOT, "", false);
            return responseWrapper.getResponse().getStatus() == Response.Status.OK.getStatusCode();
        } catch (Exception e) {
            throw new SensorRequestException("Could not complete " + getClass().getSimpleName(), e);
        }
    }
}
