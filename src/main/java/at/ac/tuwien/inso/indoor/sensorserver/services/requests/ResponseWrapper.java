package at.ac.tuwien.inso.indoor.sensorserver.services.requests;

import at.ac.tuwien.inso.indoor.sensorserver.util.ServerUtil;

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Map;

/**
 * Created by PatrickF on 12.09.2014.
 */
public class ResponseWrapper {
    private final Response response;
    private String body;

    public ResponseWrapper(Response response) {
        this.response = response;
    }

    public String getBody() {
        if (body == null) {
            body = ServerUtil.getStringFromInputStream((InputStream) response.getEntity(), true);
        }
        return body;
    }

    public Response getResponse() {
        return response;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, NewCookie> getCookie() {
        return response.getCookies();
    }
}
