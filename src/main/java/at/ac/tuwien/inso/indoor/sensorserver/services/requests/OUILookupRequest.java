package at.ac.tuwien.inso.indoor.sensorserver.services.requests;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.ObjectMapperManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.OUIMacInfo;
import at.ac.tuwien.inso.indoor.sensorserver.services.exceptions.SensorRequestException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Created by PatrickF on 06.10.2014.
 */
public class OUILookupRequest extends ARequest<OUIMacInfo> {
    private static final String OUI_URL = "http://www.macvendorlookup.com/api/v2/";

    private String macAddress;
    private ObjectMapper objectMapper;

    public OUILookupRequest(String macAddress) {
        this.macAddress = macAddress;
        this.objectMapper = ObjectMapperManager.createMapperForAPI();
    }

    @Override
    public OUIMacInfo startRequest() throws SensorRequestException {
        try {
            ResponseWrapper responseWrapper = runRequest("GET", OUI_URL + convertMacToSearchableString(macAddress), "", true);
            if (responseWrapper.getResponse().getStatus() == Response.Status.OK.getStatusCode()) {
                try {
                    return objectMapper.readValue(responseWrapper.getBody(), OUIMacInfo.class);
                } catch (IOException e) {
                    log.error("Could not parse oui lookup result", e);
                }
            }
            return null;
        } catch (Exception e) {
            log.error("error while OUI lookup",e);
            return null;
        }
    }

    private String convertMacToSearchableString(String macAddress) {
        return macAddress.replace(":","-").substring(0,8);
    }
}
