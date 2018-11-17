package at.ac.tuwien.inso.indoor.sensorserver.services.requests;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.network.SensorNode;
import at.ac.tuwien.inso.indoor.sensorserver.services.exceptions.SensorRequestException;
import at.ac.tuwien.inso.indoor.sensorserver.util.ServerUtil;
import org.apache.log4j.Logger;
import org.glassfish.jersey.client.ClientProperties;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by PatrickF on 13.09.2014.
 */
public abstract class ARequest<T> {
    protected static Logger log = Logger.getLogger(ARequest.class);

    private Client client;
    private SensorNode node;

    public ARequest(SensorNode node) {
        this();
        this.node = node;
    }

    protected ARequest() {
        client = ClientBuilder.newClient();
        client.property(ClientProperties.CONNECT_TIMEOUT, 1000 * 20);
        client.property(ClientProperties.READ_TIMEOUT, 1000 * 20);
    }

    public abstract T startRequest() throws SensorRequestException;

    protected ResponseWrapper runRequest(String method, String uri, String requestBody, boolean shouldLogBody) {

        long startTime = new Date().getTime();
        WebTarget target = client.target(uri);
        log.debug("Request: " + method + " " + target.getUri().toString());
        Map<String, String> headerMap = new HashMap<String, String>();
        log.debug("\tHeader: " + headerMap);

        if (requestBody != null && !requestBody.isEmpty()) {
            log.debug("\tBody: " + ServerUtil.foldTooLongString(requestBody, 6000));
        }

        ResponseWrapper response;
        Invocation.Builder builder = target.request().acceptEncoding("");

        for (String s : headerMap.keySet()) {
            builder.header(s, headerMap.get(s));
        }

        Invocation invocation = null;
        if (method.equalsIgnoreCase("GET")) {
            invocation = builder.buildGet();
        } else if (method.equalsIgnoreCase("POST")) {
            invocation = builder.buildPost(Entity.entity(requestBody, MediaType.APPLICATION_JSON_TYPE));
        } else if (method.equalsIgnoreCase("PUT")) {
            invocation = builder.buildPut(Entity.entity(requestBody, MediaType.APPLICATION_JSON_TYPE));
        } else if (method.equalsIgnoreCase("DELETE")) {
            invocation = builder.buildDelete();
        } else {
            throw new IllegalArgumentException("Wrong method " + method);
        }

        response = new ResponseWrapper(invocation.invoke());

        long responseTime = new Date().getTime() - startTime;

        log.debug("Response: " + response.getResponse().getStatus() + " (" + responseTime + "ms)");
        log.debug("\tHeader:" + response.getResponse().getHeaders().toString());
        if (shouldLogBody) log.debug("\tBody:" + ServerUtil.foldTooLongString(response.getBody(), 6000) + "\n");

        return response;
    }

    public SensorNode getNode() {
        return node;
    }
}
