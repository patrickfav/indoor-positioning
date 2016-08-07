package at.ac.tuwien.inso.indoor.sensorserver.services.rest;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.ObjectMapperManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Created by PatrickF on 04.03.14.
 */

@Provider
public class JerseyMapperProvider implements ContextResolver<ObjectMapper> {
    private static ObjectMapper apiMapper = ObjectMapperManager.createMapperForAPI();
    @Override
    public ObjectMapper getContext(Class<?> type)
    {
        return apiMapper;
    }
}