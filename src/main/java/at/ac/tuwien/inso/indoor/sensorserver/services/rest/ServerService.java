package at.ac.tuwien.inso.indoor.sensorserver.services.rest;

import at.ac.tuwien.inso.indoor.sensorserver.persistence.manager.EtagManager;
import at.ac.tuwien.inso.indoor.sensorserver.persistence.model.responsewrapper.BaseResponse;
import at.ac.tuwien.inso.indoor.sensorserver.services.ApiConst;
import at.ac.tuwien.inso.indoor.sensorserver.services.ServerConfig;
import at.ac.tuwien.inso.indoor.sensorserver.services.exceptions.ExceptionHandler;
import at.ac.tuwien.inso.indoor.sensorserver.util.CacheUtil;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;

/**
 * Created by PatrickF on 08.10.2014.
 */
@Path("/config")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ServerService {

    @GET
    public Response getServerConfig(@Context Request request) {
        Response.ResponseBuilder builder = null;

        try {
            builder = request.evaluatePreconditions(CacheUtil.getEtag(EtagManager.getInstance().getETag(ServerConfig.class)));
            CacheControl cc = CacheUtil.getCacheControl(ApiConst.CacheControl.SERVER_CONFIG);
            if (builder == null) {
                builder = Response.ok(ServerConfig.getInstance()).cacheControl(cc).tag(CacheUtil.getEtag(EtagManager.getInstance().getETag(ServerConfig.class)));
            }
            return builder.cacheControl(cc).build();
        } catch (Exception e) {
            ExceptionHandler.handle(new BaseResponse(), e);
        }
        return null;
    }

    @POST
    public ServerConfig updateServerConfig(ServerConfig config) {
        try {
            ServerConfig.getInstance().set(config);
            ServerConfig.getInstance().saveToDb();
            return ServerConfig.getInstance();
        } catch (Exception e) {
            ExceptionHandler.handle(new BaseResponse(), e);
        }
        return null;
    }
}
